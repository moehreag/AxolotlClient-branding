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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	@Nullable
	private RealmsNotificationsScreen realmsNotificationsScreen;

	@Unique
	private static final WidgetSprites MUTE_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("social_interactions/mute_button"), ResourceLocation.withDefaultNamespace("social_interactions/mute_button_highlighted"));

	protected TitleScreenMixin() {
		super(Component.empty());
	}

	@Inject(method = "createNormalMenuOptions", at = @At("TAIL"))
	private void axolotlclient$inMenu(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
		if (minecraft.options.keySaveHotbarActivator.same(Zoom.key)) {
			minecraft.options.keySaveHotbarActivator.setKey(InputConstants.UNKNOWN);
			AxolotlClient.LOGGER.info("Unbound \"Save Toolbar Activator\" to resolve conflict with the zoom key!");
		}
		List<AbstractWidget> buttons = Collections.synchronizedList(new ArrayList<>());
		int leftButtonY = 10;
		if (Auth.getInstance().showButton.get()) {
			var button = addRenderableWidget(new AuthWidget(10, leftButtonY));
			buttons.add(button);
			leftButtonY += button.getHeight() + 5;
		}
		if (APIOptions.getInstance().addShortcutButtons.get()) {
			int shortcutButtonY = leftButtonY;
			Runnable addApiButtons = () -> minecraft.submit(() -> {
				buttons.add(addRenderableWidget(Button.builder(Component.translatable("api.friends"),
					w -> minecraft.setScreen(new FriendsScreen(this))).bounds(10, shortcutButtonY, 50, 20).build()));
				buttons.add(addRenderableWidget(Button.builder(Component.translatable("api.chats"),
					w -> minecraft.setScreen(new ChatListScreen(this))).bounds(10, shortcutButtonY + 25, 50, 20).build()));
			});
			if (API.getInstance().isSocketConnected()) {
				addApiButtons.run();
			} else {
				API.addStartupListener(addApiButtons, API.ListenerType.ONCE);
			}
		}
		GlobalDataRequest.get().thenAccept(data -> {
			int buttonY = 10;
			if (APIOptions.getInstance().updateNotifications.get() &&
				data.success() &&
				data.latestVersion().isNewerThan(AxolotlClient.VERSION)) {
				buttons.add(addRenderableWidget(Button.builder(Component.translatable("api.new_version_available"),
						ConfirmLinkScreen.confirmLink(minecraft.screen, "https://modrinth.com/mod/axolotlclient/versions"))
					.bounds(width - 90, y, 80, 20).build()));
				buttonY += 22;
			}
			if (APIOptions.getInstance().displayNotes.get() &&
				data.success() && !data.notes().isEmpty()) {
				buttons.add(addRenderableWidget(Button.builder(Component.translatable("api.notes"), buttonWidget ->
						minecraft.setScreen(new NewsScreen(this)))
					.bounds(width - 90, buttonY, 80, 20).build()));
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

	@Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$disableRealmsNotifications(CallbackInfoReturnable<Boolean> cir) {
		this.realmsNotificationsScreen = null;
		cir.setReturnValue(false);
	}

	@ModifyArgs(method = "createNormalMenuOptions",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", ordinal = 2))
	private void axolotlclient$noRealmsbutOptionsButton(Args args) {
		if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
			args.set(0, Component.translatable("config"));
			args.set(1, (Button.OnPress) buttonWidget -> minecraft.setScreen(new HudEditScreen(this)));
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"), index = 1)
	private String axolotlclient$setVersionText(String s) {
		return "Minecraft " + SharedConstants.getCurrentVersion().getName() + "/AxolotlClient "
			+ AxolotlClient.VERSION;
	}

	@Inject(method = "realmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$noRealmsIcons(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}
}
