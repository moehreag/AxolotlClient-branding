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

import java.net.URI;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.NewsScreen;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.AuthWidget;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	protected TitleScreenMixin() {
		super(Component.empty());
	}

	@Inject(method = "createNormalMenuOptions", at = @At("HEAD"))
	private void axolotlclient$inMenu(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
		if (minecraft.options.keySaveHotbarActivator.same(Zoom.key)) {
			minecraft.options.keySaveHotbarActivator.setKey(InputConstants.UNKNOWN);
			AxolotlClient.LOGGER.info("Unbound \"Save Toolbar Activator\" to resolve conflict with the zoom key!");
		}
		if (Auth.getInstance().showButton.get()) {
			addRenderableWidget(new AuthWidget());
		}
		if (APIOptions.getInstance().privacyAccepted.get().equals("accepted") && APIOptions.getInstance().updateNotifications.get() &&
			GlobalDataRequest.get().success() &&
			GlobalDataRequest.get().latestVersion().isNewerThan(AxolotlClient.VERSION)) {
			addRenderableWidget(Button.builder(Component.translatable("api.new_version_available"), widget ->
					minecraft.setScreen(new ConfirmLinkScreen(r -> {
						if (r) {
							OSUtil.getOS().open(URI.create("https://modrinth.com/mod/axolotlclient/versions"));
						}
					}, "https://modrinth.com/mod/axolotlclient/versions", true)))
				.bounds(width - 125, 10, 120, 20).build());
		}
		if (APIOptions.getInstance().privacyAccepted.get().equals("accepted") && APIOptions.getInstance().displayNotes.get() &&
			GlobalDataRequest.get().success() && !GlobalDataRequest.get().notes().isEmpty()) {
			addRenderableWidget(Button.builder(Component.translatable("api.notes"), buttonWidget ->
					minecraft.setScreen(new NewsScreen(this)))
				.bounds(width - 125, 25, 120, 20).build());
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
