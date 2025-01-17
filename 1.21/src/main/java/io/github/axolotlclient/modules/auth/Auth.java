/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.report.AbuseReportContext;
import net.minecraft.client.multiplayer.report.AbuseReportEnvironment;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.PlayerKeyPairManager;
import net.minecraft.client.util.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Auth extends Accounts implements Module {

	@Getter
	private final static Auth Instance = new Auth();
	public final BooleanOption showButton = new BooleanOption("auth.showButton", false);
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final GenericOption viewAccounts = new GenericOption("viewAccounts", "clickToOpen", () -> client.setScreen(new AccountsScreen(client.currentScreen)));
	private final Set<String> loadingTexture = new HashSet<>();
	private final Map<String, Identifier> textures = new WeakHashMap<>();

	@Override
	public void init() {
		load();
		this.auth = new MSAuth(AxolotlClient.LOGGER, this, () -> client.options.language);
		if (isContained(client.getSession().getSessionId())) {
			current = getAccounts().stream().filter(account -> account.getUuid()
				.equals(UndashedUuid.toString(client.getSession().getPlayerUuid()))).toList().getFirst();
			current.setAuthToken(client.getSession().getAccessToken());
			current.setName(client.getSession().getUsername());
			/*if (current.needsRefresh()) {
				current.refresh(auth).thenRun(this::save);
			}*/
		} else {
			current = new Account(client.getSession().getUsername(), UndashedUuid.toString(client.getSession().getPlayerUuid()), client.getSession().getAccessToken());
		}

		OptionCategory category = OptionCategory.create("auth");
		category.add(showButton, viewAccounts);
		AxolotlClient.CONFIG.general.add(category);
	}

	@Override
	protected Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected void login(Account account) {
		if (client.world != null) {
			return;
		}

		if (account.needsRefresh() && !account.isOffline()) {
			if (account.isExpired()) {
				Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.refreshing", account.getName()));
			}
			account.refresh(auth).thenAccept(res -> res.ifPresent(a -> {
				if (!a.isExpired()) {
					login(a);
				}
			})).thenRun(this::save);
		} else {
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
				}
				((MinecraftClientAccessor) client).axolotlclient$setUserApiService(service);
				((MinecraftClientAccessor) client).axolotlclient$setSocialInteractionsManager(new SocialInteractionsManager(client, service));
				((MinecraftClientAccessor) client).axolotlclient$setPlayerKeyPairManager(PlayerKeyPairManager.create(service, client.getSession(), client.runDirectory.toPath()));
				((MinecraftClientAccessor) client).axolotlclient$setChatReportingContext(AbuseReportContext.create(AbuseReportEnvironment.createLocal(), service));
				save();
				current = account;
				Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.login.successful", current.getName()));
				API.getInstance().startup(account);
			} catch (Exception e) {
				Notifications.getInstance().addStatus(Text.translatable("auth.notif.title"), Text.translatable("auth.notif.login.failed"));
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return AxolotlClient.LOGGER;
	}

	@Override
	void showAccountsExpiredScreen(Account account) {
		Screen current = client.currentScreen;
		client.execute(() -> client.setScreen(new ConfirmScreen((bl) -> {
			client.setScreen(current);
			if (bl) {
				auth.startDeviceAuth();
			}
		}, Text.translatable("auth"), Text.translatable("auth.accountExpiredNotice", account.getName()))));
	}

	@Override
	void displayDeviceCode(DeviceFlowData data) {
		client.execute(() -> client.setScreen(new DeviceCodeDisplayScreen(client.currentScreen, data)));
	}

	private void loadTexture(String uuid) {
		if (!loadingTexture.contains(uuid)) {
			loadingTexture.add(uuid);
			ThreadExecuter.scheduleTask(() -> {
				UUID uUID = UndashedUuid.fromString(uuid);
				ProfileResult profileResult = client.getSessionService().fetchProfile(uUID, false);
				if (profileResult != null) {
					client.getSkinProvider().fetchSkin(profileResult.profile())
						.thenAccept(playerSkin -> textures.put(uuid, playerSkin.texture()));
				}
				loadingTexture.remove(uuid);
			});
		}
	}

	public Identifier getSkinTexture(Account account) {
		return getSkinTexture(account.getUuid(), account.getName());
	}

	public Identifier getSkinTexture(String uuid, String name) {
		if (!textures.containsKey(uuid)) {
			loadTexture(uuid);
			return Objects.requireNonNullElseGet(textures.get(uuid),
				() -> DefaultSkinHelper.getSkin(UndashedUuid.fromString(uuid)).texture());
		}
		return textures.get(uuid);
	}
}
