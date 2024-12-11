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
import java.net.URI;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.NewsScreen;
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
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	@Shadow
	public abstract void render(int par1, int par2, float par3);

	@ModifyArgs(method = "initWidgetsNormal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIILjava/lang/String;)V", ordinal = 2))
	private void axolotlclient$replaceRealmsButton(Args args) {
		if (!axolotlclient$alternateLayout()) {

			args.set(0, 192);
			args.set(3, I18n.translate("config") + "...");
		}
		if (Auth.getInstance().showButton.get()) {
			buttons.add(new AuthWidget());
		}
		if (APIOptions.getInstance().updateNotifications.get() &&
			GlobalDataRequest.get().success() &&
			GlobalDataRequest.get().latestVersion().isNewerThan(AxolotlClient.VERSION)) {
			buttons.add(new ButtonWidget(182, width - 125, 10, 120, 20, I18n.translate("api.new_version_available")));
		}
		if (APIOptions.getInstance().displayNotes.get() &&
			GlobalDataRequest.get().success() && !GlobalDataRequest.get().notes().isEmpty()) {
			buttons.add(new ButtonWidget(253, width - 125, 25, 120, 20,
				I18n.translate("api.notes")));
		}
	}

	@Unique
	private boolean axolotlclient$alternateLayout() {
		return FabricLoader.getInstance().isModLoaded("modmenu") && !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu");
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
