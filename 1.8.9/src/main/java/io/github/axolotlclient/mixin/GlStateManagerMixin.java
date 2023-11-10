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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.Util;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {

	@Inject(method = "color4f", at = @At("HEAD"), cancellable = true)
	private static void axolotlclient$nightMode(float red, float green, float blue, float alpha, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.nightMode.get()) {
			if (red != Util.GlColor.red || green != Util.GlColor.green || blue != Util.GlColor.blue
				|| alpha != Util.GlColor.alpha) {
				Util.GlColor.red = red;
				Util.GlColor.green = green;
				Util.GlColor.blue = blue;
				Util.GlColor.alpha = alpha;

				GL11.glColor4f(red, green, blue / 2, alpha);
			}

			ci.cancel();
		}
	}
}
