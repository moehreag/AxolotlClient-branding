/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.auth;

import java.nio.file.Path;
import java.util.*;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.util.UndashedUuid;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.Notifications;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.multiplayer.report.ReportEnvironment;
import net.minecraft.client.multiplayer.report.chat.ChatReportingContext;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.PlayerKeyPairManager;
import net.minecraft.client.util.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.QuiltLoader;

public class Auth extends Accounts implements Module {

	@Getter
	private final static Auth Instance = new Auth();
	public final BooleanOption showButton = new BooleanOption("auth.showButton", false);
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final GenericOption viewAccounts = new GenericOption("viewAccounts", "clickToOpen", () -> client.setScreen(new AccountsScreen(client.currentScreen)));

	private final Map<String, Identifier> textures = new HashMap<>();
	private final Set<String> loadingTexture = new HashSet<>();
	private final Map<String, GameProfile> profileCache = new WeakHashMap<>();

	@Override
	public void init() {
		load();
		this.auth = new MSAuth(AxolotlClient.LOGGER, this);
		if (isContained(client.getSession().getSessionId())) {
			current = getAccounts().stream().filter(account -> account.getUuid().equals(client.getSession().getPlayerUuid())).toList().get(0);
			if (current.isExpired()) {
				current.refresh(auth, () -> {
				});
			}
		} else {
			current = new Account(client.getSession().getUsername(), client.getSession().getSessionId(), client.getSession().getAccessToken());
		}

		OptionCategory category = OptionCategory.create("auth");
		category.add(showButton, viewAccounts);
		AxolotlClient.CONFIG.general.add(category);
	}

	@Override
	protected Path getConfigDir() {
		return QuiltLoader.getConfigDir();
	}

	@Override
	protected void login(Account account) {
		if (client.world != null) {
			return;
		}

		Runnable runnable = () -> {
			try {
				API.getInstance().shutdown();
				((MinecraftClientAccessor) client).axolotlclient$setSession(new Session(account.getName(), UndashedUuid.fromString(account.getUuid()), account.getAuthToken(),
					Optional.empty(), Optional.empty(),
					Session.AccountType.MSA));
				UserApiService service;
				if (account.isOffline()) {
					service = UserApiService.OFFLINE;
				} else {
					service = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getAuthService().createUserApiService(client.getSession().getAccessToken());
					API.getInstance().startup(account);
				}
				((MinecraftClientAccessor) client).axolotlclient$setUserApiService(service);
				((MinecraftClientAccessor) client).axolotlclient$setSocialInteractionsManager(new SocialInteractionsManager(client, service));
				((MinecraftClientAccessor) client).axolotlclient$setPlayerKeyPairManager(PlayerKeyPairManager.create(service, client.getSession(), client.runDirectory.toPath()));
				((MinecraftClientAccessor) client).axolotlclient$setChatReportingContext(ChatReportingContext.create(ReportEnvironment.createLocal(), service));
				save();
				current = account;
				Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.login.successful", (Object) current.getName()));
			} catch (AuthenticationException e) {
				e.printStackTrace();
				Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.login.failed"));
			}
		};

		if (account.isExpired() && !account.isOffline()) {
			Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.refreshing", (Object) account.getName()));
			account.refresh(auth, runnable);
		} else {
			new Thread(runnable).start();
		}
	}

	@Override
	protected Logger getLogger() {
		return AxolotlClient.LOGGER;
	}

	@Override
	public void loadTextures(String uuid, String name) {
		if (!textures.containsKey(uuid) && !loadingTexture.contains(uuid)) {
			ThreadExecuter.scheduleTask(() -> {
				loadingTexture.add(uuid);
				GameProfile gameProfile;
				if (profileCache.containsKey(uuid)) {
					gameProfile = profileCache.get(uuid);
				} else {
					try {
						UUID uUID = UndashedUuid.fromString(uuid);
						gameProfile = new GameProfile(uUID, name);
						ProfileResult result = client.getSessionService().fetchProfile(uUID, false);
						if (result != null) {
							gameProfile = result.profile();
						}
					} catch (IllegalArgumentException var2) {
						gameProfile = new GameProfile(null, name);
					}
					profileCache.put(uuid, gameProfile);
				}
				PlayerSkin skin = client.getSkinProvider().getSkin(gameProfile);
				if (skin != null) {
					textures.put(uuid, skin.texture());
					loadingTexture.remove(uuid);
				}
			});
		}
	}

	public Identifier getSkinTexture(Account account) {
		return getSkinTexture(account.getUuid(), account.getName());
	}

	public Identifier getSkinTexture(String uuid, String name) {
		loadTextures(uuid, name);
		Identifier id;
		if ((id = textures.get(uuid)) != null) {
			return id;
		}
		try {
			UUID uUID = UndashedUuid.fromString(uuid);
			return DefaultSkinHelper.getSkin(uUID).texture();
		} catch (IllegalArgumentException ignored) {
			return DefaultSkinHelper.getDefaultTexture();
		}
	}
}
