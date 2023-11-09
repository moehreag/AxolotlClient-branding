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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin() {
		super(Text.of(""));
	}

	@Inject(method = "initWidgetsNormal", at = @At("HEAD"))
	public void axolotlclient$inMenu(int y, int spacingY, CallbackInfo ci) {
		if (MinecraftClient.getInstance().options.keySaveToolbarActivator.equals(Zoom.keyBinding)) {
			MinecraftClient.getInstance().options.keySaveToolbarActivator.setBoundKey(InputUtil.UNKNOWN_KEY);
			AxolotlClient.LOGGER.info("Unbound \"Save Toolbar Activator\" to resolve conflict with the zoom key!");
		}
		if (Auth.getInstance().showButton.get()) {
			addButton(new AuthWidget());
		}
		if(APIOptions.getInstance().updateNotifications.get() &&
			GlobalDataRequest.get().isSuccess() &&
			GlobalDataRequest.get().getLatestVersion().isNewerThan(AxolotlClient.VERSION)){
			addButton(new ButtonWidget(width - 125, 10, 120, 20,
				new TranslatableText("api.new_version_available"), widget ->
				MinecraftClient.getInstance().openScreen(new ConfirmChatLinkScreen(r -> {
					if (r){
						OSUtil.getOS().open(URI.create("https://modrinth.com/mod/axolotlclient/versions"), AxolotlClient.LOGGER);
					}
				}, "https://modrinth.com/mod/axolotlclient/versions", true))));
		}
		if (APIOptions.getInstance().displayNotes.get() &&
			GlobalDataRequest.get().isSuccess() && !GlobalDataRequest.get().getNotes().isEmpty()) {
			addButton(new ButtonWidget(width-125, 25, 120, 20,
				new TranslatableText("api.notes"), buttonWidget ->
				MinecraftClient.getInstance().openScreen(new NewsScreen(this))));
		}
	}

	@ModifyArgs(method = "initWidgetsNormal", at =
	@At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;Lnet/minecraft/client/gui/widget/ButtonWidget$TooltipSupplier;)V",
		ordinal = 1))
	public void axolotlclient$noRealmsbutOptionsButton(Args args) {
		if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
			args.set(4, new TranslatableText("config"));
			args.set(5, (ButtonWidget.PressAction) buttonWidget -> MinecraftClient.getInstance()
				.openScreen(new HudEditScreen(this)));
		}
	}

	@Redirect(method = "initWidgetsNormal", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;active:Z", ordinal = 1))
	private void axolotlclient$activateIfModSettings(ButtonWidget instance, boolean value) {
		if (FabricLoader.getInstance().isModLoaded("modmenu")) {
			instance.active = value;
		}
	}


	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawStringWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"), index = 2)
	public String axolotlclient$setVersionText(String s) {
		return "Minecraft " + SharedConstants.getGameVersion().getName() + "/AxolotlClient "
			+ AxolotlClient.VERSION;
	}

	@Inject(method = "areRealmsNotificationsEnabled", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$noRealmsIcons(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@Inject(method = "init", at = @At("HEAD"))
	public void axolotlclient$showBadModsScreen(CallbackInfo ci) {
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

			MinecraftClient.getInstance().openScreen(new ConfirmScreen((boolean confirmed) -> {
				if (confirmed) {
					AxolotlClient.showWarning = false;
					AxolotlClient.titleDisclaimer = true;
					System.out.println("Proceed with Caution!");
					MinecraftClient.getInstance().openScreen(new TitleScreen());
				} else {
					MinecraftClient.getInstance().stop();
				}
			}, new LiteralText("Axolotlclient warning").formatted(Formatting.RED), new LiteralText("The mod ")
				.append(new LiteralText(AxolotlClient.badmod.name()).formatted(Formatting.BOLD, Formatting.DARK_RED))
				.append(" is known to ").append(description.toString())
				.append("AxolotlClient will not be responsible for any punishment or crashes you will encounter while using it.\n Proceed with Caution!"),
				new TranslatableText("gui.proceed"), new TranslatableText("menu.quit")));
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void axolotlclient$addDisclaimer(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (AxolotlClient.titleDisclaimer) {
			TitleScreen.drawCenteredText(matrices, this.textRenderer,
				"You are playing at your own risk with unsupported Mods", this.width / 2, 5, 0xFFCC8888);
			TitleScreen.drawCenteredText(matrices, this.textRenderer, "Things could break!", this.width / 2, 15,
				0xFFCC8888);
		}
	}
}
