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

package io.github.axolotlclient.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.FriendsScreen;
import io.github.axolotlclient.api.NewsScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.auth.AccountsScreen;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.AuthWidget;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	public abstract void render(int par1, int par2, float par3);

	@Inject(method = "initWidgetsNormal", at = @At("TAIL"))
	private void axolotlclient$replaceRealmsButton(int i, int j, CallbackInfo ci) {
		List<ButtonWidget> buttons = new ArrayList<>();
		int leftButtonY = 10;
		if (Auth.getInstance().showButton.get()) {
			buttons.add(new AuthWidget(10, leftButtonY));
			leftButtonY += 25;
		}
		this.buttons.addAll(buttons);
		if (APIOptions.getInstance().addShortcutButtons.get()) {
			int y = leftButtonY;
			Runnable addApiButtons = () -> {
				ButtonWidget friends = new ButtonWidget(142, 10, y, 50, 20, I18n.translate("api.friends"));
				this.buttons.add(friends);
				buttons.add(friends);
				ButtonWidget chats = new ButtonWidget(42, 10, y + 25, 50, 20, I18n.translate("api.chats"));
				this.buttons.add(chats);
				buttons.add(chats);
			};
			if (API.getInstance().isSocketConnected()) {
				addApiButtons.run();
			} else {
				API.addStartupListener(() -> minecraft.submit(addApiButtons), API.ListenerType.ONCE);
			}
		}
		GlobalDataRequest.get().thenAccept(data -> {
			int buttonY = 10;
			if (APIOptions.getInstance().updateNotifications.get() &&
				data.success() &&
				data.latestVersion().isNewerThan(AxolotlClient.VERSION)) {
				ButtonWidget newVersion = new ButtonWidget(182, width - 90, buttonY, 80, 20, I18n.translate("api.new_version_available"));
				this.buttons.add(newVersion);
				buttons.add(newVersion);
				buttonY += 22;
			}
			if (APIOptions.getInstance().displayNotes.get() &&
				data.success() && !data.notes().isEmpty()) {
				ButtonWidget notes = new ButtonWidget(253, width - 90, buttonY, 80, 20,
					I18n.translate("api.notes"));
				this.buttons.add(notes);
				buttons.add(notes);
			}
		});

		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			try {
				Class<?> booleanConfigOpt = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.option.BooleanConfigOption");
				Class<?> enumConfigOpt = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.option.EnumConfigOption");
				Class<?> titleMenuButtonStyle = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.ModMenuConfig$TitleMenuButtonStyle");
				Class<?> modmenuConfig = MethodHandles.lookup().findClass("com.terraformersmc.modmenu.config.ModMenuConfig");
				MethodHandle modifyTitleScreenHandle = MethodHandles.lookup().findStaticGetter(modmenuConfig, "MODIFY_TITLE_SCREEN", booleanConfigOpt);
				MethodHandle getValueB = MethodHandles.lookup().findVirtual(booleanConfigOpt, "getValue", MethodType.methodType(boolean.class));
				MethodHandle getValueE = MethodHandles.lookup().findVirtual(enumConfigOpt, "getValue", MethodType.methodType(Enum.class));
				var modifyTitleScreen = modifyTitleScreenHandle.invoke();
				boolean isModifyTitleScreen = (boolean) getValueB.invoke(modifyTitleScreen);
				MethodHandle modsButtonStyleHandle = MethodHandles.lookup().findStaticGetter(modmenuConfig, "MODS_BUTTON_STYLE", enumConfigOpt);
				var modsButtonStyle = getValueE.invoke(modsButtonStyleHandle.invoke());
				var classic = titleMenuButtonStyle.getEnumConstants()[0];
				if (isModifyTitleScreen && modsButtonStyle == classic) {
					buttons.forEach(r -> r.y -= 24 / 2);
				}
			} catch (Throwable ignored) {
			}
		}
	}

	@Unique
	private boolean axolotlclient$alternateLayout() {
		return !FabricLoader.getInstance().isModLoaded("modmenu") || FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "initWidgetsNormal", at = @At("TAIL"))
	private void axolotlclient$addOptionsButton(int y, int spacingY, CallbackInfo ci) {
		if (axolotlclient$alternateLayout()) {
			buttons.add(new ButtonWidget(192, this.width / 2 - 100, y + spacingY * 3, I18n.translate("config") + "..."));
		}
	}

	@ModifyConstant(method = "init", constant = @Constant(intValue = 72))
	private int axolotlclient$moveButtons(int constant) {
		if (axolotlclient$alternateLayout()) {
			return constant + 25;
		}
		return constant;
	}

	@Inject(method = "buttonClicked", at = @At("TAIL"))
	public void axolotlclient$onClick(ButtonWidget button, CallbackInfo ci) {
		if (button.id == 192)
			Minecraft.getInstance().openScreen(new HudEditScreen(this));
		else if (button.id == 242)
			Minecraft.getInstance().openScreen(new AccountsScreen(Minecraft.getInstance().screen));
		else if (button.id == 182)
			Minecraft.getInstance().openScreen(new ConfirmChatLinkScreen((bl, i) -> {
				if (bl && i == 353) {
					OSUtil.getOS().open(URI.create("https://modrinth.com/mod/axolotlclient/versions"));
				}
				Minecraft.getInstance().openScreen(this);
			}, "https://modrinth.com/mod/axolotlclient/versions", 353, true));
		else if (button.id == 253)
			Minecraft.getInstance().openScreen(new NewsScreen(this));
		else if (button.id == 142) minecraft.openScreen(new FriendsScreen(this));
		else if (button.id == 42) minecraft.openScreen(new ChatListScreen(this));
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	public void axolotlclient$customBranding(TitleScreen instance, TextRenderer textRenderer, String s, int x, int y, int color) {
		if (FabricLoader.getInstance().getModContainer("axolotlclient").isPresent()) {
			instance.drawString(textRenderer,
				"Minecraft 1.8.9/" + ClientBrandRetriever.getClientModName() + " " + AxolotlClient.VERSION,
				x, y, color);
		} else {
			instance.drawString(textRenderer, s, x, y, color);
		}
	}

	@Inject(method = "<init>",
		at = @At(value = "INVOKE",
			target = "Ljava/util/List;isEmpty()Z", remap = false))
	private void axolotlclient$customSplashTexts(CallbackInfo ci, @Local List<String> list) throws IOException {
		try (InputStream input = Minecraft.getInstance().getResourceManager()
			.getResource(new Identifier("axolotlclient", "texts/splashes.txt")).asStream()) {
			list.addAll(IOUtils.readLines(input));
		}
	}
}
