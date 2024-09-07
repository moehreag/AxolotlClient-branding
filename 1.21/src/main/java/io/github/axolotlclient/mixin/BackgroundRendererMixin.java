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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.modules.sky.SkyboxManager;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

	@Inject(method = "applyFog", at = @At("TAIL"))
	private static void axolotlclient$applyNoFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance,
												 boolean thickFog, float tickDelta, CallbackInfo ci) {
		if (camera.getSubmersionType() == CameraSubmersionType.NONE
			&& (thickFog || fogType == BackgroundRenderer.FogType.FOG_TERRAIN)) {
			if (SkyboxManager.getInstance().hasSkyBoxes()) {
				RenderSystem.setShaderFogStart(Short.MAX_VALUE - 1);
				RenderSystem.setShaderFogEnd(Short.MAX_VALUE);
			}
		}
	}
}
