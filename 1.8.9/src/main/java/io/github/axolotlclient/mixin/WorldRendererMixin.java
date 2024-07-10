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
import io.github.axolotlclient.modules.sky.SkyboxManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.HitResult;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Shadow
	private ClientWorld world;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCustomSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
		if (this.world.dimension.isOverworld()) {
			if (AxolotlClient.CONFIG.customSky.get() && SkyboxManager.getInstance().hasSkyBoxes()) {
				GlStateManager.depthMask(false);
				this.minecraft.profiler.push("Custom Skies");
				SkyboxManager.getInstance().renderSkyboxes(tickDelta, world.getRain(tickDelta));
				this.minecraft.profiler.pop();
				GlStateManager.depthMask(true);
				ci.cancel();
			}
		}
	}

	@Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/Dimension;getCloudHeight()F"))
	public float axolotlclient$getCloudHeight(Dimension instance) {
		return AxolotlClient.CONFIG.cloudHeight.get();
	}

	@ModifyArg(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glLineWidth(F)V"), remap = false)
	public float axolotlclient$OutlineWidth(float width) {
		if (AxolotlClient.CONFIG.enableCustomOutlines.get() && AxolotlClient.CONFIG.outlineWidth.get() > 1) {
			return 1.0F + AxolotlClient.CONFIG.outlineWidth.get();
		}
		return width;
	}

	@Inject(method = "renderBlockOutline", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;color4f(FFFF)V", shift = At.Shift.AFTER))
	public void axolotlclient$customOutlineColor(PlayerEntity playerEntity, HitResult hitResult, int i, float f, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.enableCustomOutlines.get()) {
			GlStateManager.clearColor();

			int color = AxolotlClient.CONFIG.outlineColor.get().toInt();
			float a = (float) (color >> 24 & 0xFF) / 255.0F;
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			GlStateManager.color4f(r, g, b, a);
		}
	}
}
