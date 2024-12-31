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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.FriendsScreen;
import io.github.axolotlclient.api.NewsScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.AuthWidget;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.realms.RealmsNotificationsScreen;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	@Nullable
	private RealmsNotificationsScreen realmsNotificationGui;

	protected TitleScreenMixin() {
		super(Text.of(""));
	}

	@Inject(method = "initWidgetsNormal", at = @At("TAIL"))
	private void axolotlclient$inMenu(int y, int spacingY, CallbackInfo ci) {
		if (MinecraftClient.getInstance().options.saveToolbarActivatorKey.keyEquals(Zoom.key)) {
			MinecraftClient.getInstance().options.saveToolbarActivatorKey.setBoundKey(InputUtil.UNKNOWN_KEY);
			AxolotlClient.LOGGER.info("Unbound \"Save Toolbar Activator\" to resolve conflict with the zoom key!");
		}
		List<PressableWidget> buttons = Collections.synchronizedList(new ArrayList<>());
		int leftButtonY = 10;
		if (Auth.getInstance().showButton.get()) {
			buttons.add(addDrawableSelectableElement(new AuthWidget(10, leftButtonY)));
			leftButtonY += 25;
		}
		if (APIOptions.getInstance().addShortcutButtons.get() && API.getInstance().isAuthenticated()) {
			buttons.add(addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.friends"),
				w -> client.setScreen(new FriendsScreen(this))).positionAndSize(10, leftButtonY, 50, 20).build()));
			leftButtonY += 25;
			buttons.add(addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.chats"),
				w -> client.setScreen(new ChatListScreen(this))).positionAndSize(10, leftButtonY, 50, 20).build()));
		}
		GlobalDataRequest.get().thenAccept(data -> {
			int buttonY = 10;
			if (APIOptions.getInstance().updateNotifications.get() &&
				data.success() &&
				data.latestVersion().isNewerThan(AxolotlClient.VERSION)) {
				buttons.add(addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.new_version_available"), widget ->
						MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen(r -> {
							if (r) {
								OSUtil.getOS().open(URI.create("https://modrinth.com/mod/axolotlclient/versions"));
							}
						}, "https://modrinth.com/mod/axolotlclient/versions", true)))
					.positionAndSize(width - 90, buttonY, 80, 20).build()));
				buttonY += 22;
			}
			if (APIOptions.getInstance().displayNotes.get() &&
				data.success() && !data.notes().isEmpty()) {
				buttons.add(addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.notes"), buttonWidget ->
						MinecraftClient.getInstance().setScreen(new NewsScreen(this)))
					.positionAndSize(width - 90, buttonY, 80, 20).build()));
			}
		});

		// Thanks modmenu.. >:3
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
					buttons.forEach(r -> r.setY(r.getY() - 24 / 2));
				}
			} catch (Throwable ignored) {
			}
		}
	}

	@Inject(method = "areRealmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$disableRealmsNotifications(CallbackInfoReturnable<Boolean> cir) {
		this.realmsNotificationGui = null;
		cir.setReturnValue(false);
	}

	@ModifyArgs(method = "initWidgetsNormal",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/widget/button/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/button/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/button/ButtonWidget$Builder;", ordinal = 2))
	private void axolotlclient$noRealmsbutOptionsButton(Args args) {
		if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
			args.set(0, Text.translatable("config"));
			args.set(1, (ButtonWidget.PressAction) buttonWidget -> MinecraftClient.getInstance()
				.setScreen(new HudEditScreen(this)));
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"), index = 1)
	private String axolotlclient$setVersionText(String s) {
		return "Minecraft " + SharedConstants.getGameVersion().getName() + "/AxolotlClient "
			+ AxolotlClient.VERSION;
	}

	@Inject(method = "areRealmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$noRealmsIcons(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
