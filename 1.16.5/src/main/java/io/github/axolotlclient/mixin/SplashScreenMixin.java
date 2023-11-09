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

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.modules.auth.Auth;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public abstract class SplashScreenMixin {

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/SplashScreen;BRAND_RGB:I"))
	private int axolotlclient$customBackgroundColor() {
		return AxolotlClient.CONFIG.loadingScreenColor.get().getAsInt() & 16777215;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V"))
	private void onReloadFinish(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (!API.getInstance().isConnected() && !Auth.getInstance().getCurrent().isOffline()) {
			API.getInstance().startup(Auth.getInstance().getCurrent());
		}
	}
}
