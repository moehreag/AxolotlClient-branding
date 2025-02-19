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

import java.util.function.IntSupplier;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.modules.auth.Auth;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoadingOverlay.class, priority = 1100)
public abstract class SplashOverlayMixin {

	@Shadow
	@Final
	@Mutable
	private static IntSupplier BRAND_BACKGROUND;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void axolotlclient$customBackgroundColor(CallbackInfo ci) {
		if (!FabricLoader.getInstance().isModLoaded("dark-loading-screen")) {
			if (AxolotlClient.CONFIG != null) {
				Color color = AxolotlClient.CONFIG.loadingScreenColor.get();
				BRAND_BACKGROUND = color::toInt;
				//ColorUtil.Argb32.of(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
			}
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V"))
	private void onReloadFinish(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (!API.getInstance().isSocketConnected() && !Auth.getInstance().getCurrent().isOffline()) {
			API.getInstance().startup(Auth.getInstance().getCurrent());
		}
	}
}
