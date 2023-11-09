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

package io.github.axolotlclient.mixin;

import java.net.URI;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.NewsScreen;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.auth.AuthWidget;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.UnsupportedMod;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineScrollableWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.QuiltLoader;
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

	@Inject(method = "initWidgetsNormal", at = @At("HEAD"))
	private void axolotlclient$inMenu(int y, int spacingY, CallbackInfo ci) {
		if (MinecraftClient.getInstance().options.saveToolbarActivatorKey.keyEquals(Zoom.key.get())) {
			MinecraftClient.getInstance().options.saveToolbarActivatorKey.setBoundKey(InputUtil.UNKNOWN_KEY);
			AxolotlClient.LOGGER.info("Unbound \"Save Toolbar Activator\" to resolve conflict with the zoom key!");
		}
		if (Auth.getInstance().showButton.get()) {
			addDrawableChild(new AuthWidget());
		}
		if(APIOptions.getInstance().updateNotifications.get() &&
			GlobalDataRequest.get().isSuccess() &&
			GlobalDataRequest.get().getLatestVersion().isNewerThan(AxolotlClient.VERSION)){
			addDrawableChild(ButtonWidget.builder(Text.translatable("api.new_version_available"), widget ->
					MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen(r -> {
						if (r){
							OSUtil.getOS().open(URI.create("https://modrinth.com/mod/axolotlclient/versions"), AxolotlClient.LOGGER);
						}
					}, "https://modrinth.com/mod/axolotlclient/versions", true)))
				.positionAndSize(width - 125, 10, 120, 20).build());
		}
		if (APIOptions.getInstance().displayNotes.get() &&
			GlobalDataRequest.get().isSuccess() && !GlobalDataRequest.get().getNotes().isEmpty()) {
			addDrawableChild(ButtonWidget.builder(Text.translatable("api.notes"), buttonWidget ->
				MinecraftClient.getInstance().setScreen(new NewsScreen(this)))
				.positionAndSize(width-125, 25, 120, 20).build());
		}
	}

	@Inject(method = "areRealmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$disableRealmsNotifications(CallbackInfoReturnable<Boolean> cir) {
		this.realmsNotificationGui = null;
		cir.setReturnValue(false);
	}

	@ModifyArgs(method = "initWidgetsNormal",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 2))
	private void axolotlclient$noRealmsbutOptionsButton(Args args) {
		if (!QuiltLoader.isModLoaded("modmenu")) {
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

	@Inject(method = "init", at = @At("HEAD"))
	private void axolotlclient$showBadModsScreen(CallbackInfo ci) {
		if (AxolotlClient.showWarning) {
			StringBuilder description = new StringBuilder();
			for (int i = 0; i < AxolotlClient.badmod.reason().length; i++) {
				UnsupportedMod.UnsupportedReason reason = AxolotlClient.badmod.reason()[i];
				if (i > 0 && i < AxolotlClient.badmod.reason().length - 1) {
					description.append(", to ");
				} else if (i > 0) {
					description.append(" and to ");
				}
				description.append(reason);
			}
			description.append(". ");

			MinecraftClient.getInstance().setScreen(new ConfirmScreen((boolean confirmed) -> {
				if (confirmed) {
					AxolotlClient.showWarning = false;
					AxolotlClient.titleDisclaimer = true;
					System.out.println("Proceed with Caution!");
					MinecraftClient.getInstance().setScreen(new TitleScreen());
				} else {
					MinecraftClient.getInstance().stop();
				}
			}, Text.literal("Axolotlclient warning").formatted(Formatting.RED), Text.literal("The mod ")
				.append(Text.literal(AxolotlClient.badmod.name()).formatted(Formatting.BOLD, Formatting.DARK_RED))
				.append(" is known to ").append(description.toString())
				.append("AxolotlClient will not be responsible for any punishment or crashes you will encounter while using it.\n Proceed with Caution!"),
				Text.translatable("gui.proceed"), Text.translatable("menu.quit")));
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void axolotlclient$addDisclaimer(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (AxolotlClient.titleDisclaimer) {
			graphics.drawCenteredShadowedText(textRenderer,
				"You are playing at your own risk with unsupported Mods", this.width / 2, 5, 0xFFCC8888);
			graphics.drawCenteredShadowedText(this.textRenderer, "Things could break!", this.width / 2, 15,
				0xFFCC8888);
		}

	}
}
