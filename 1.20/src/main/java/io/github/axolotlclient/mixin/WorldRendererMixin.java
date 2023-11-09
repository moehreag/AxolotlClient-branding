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
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.sky.SkyboxManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera preStep, boolean bl,
										 Runnable runnable, CallbackInfo ci) {
		runnable.run();
		if (AxolotlClient.CONFIG.customSky.get() && SkyboxManager.getInstance().hasSkyBoxes()
			&& !QuiltLoader.isModLoaded("fabricskyboxes")) {
			this.client.getProfiler().push("Custom Skies");

			RenderSystem.depthMask(false);
			SkyboxManager.getInstance().renderSkyboxes(matrices, projectionMatrix, tickDelta, runnable);
			RenderSystem.depthMask(true);
			this.client.getProfiler().pop();
			ci.cancel();
		}
	}

	@ModifyArgs(method = "drawBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawShapeOutline(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/util/shape/VoxelShape;DDDFFFF)V"))
	private void axolotlclient$customOutlineColor(Args args) {
		if (AxolotlClient.CONFIG.enableCustomOutlines.get()) {
			int color = AxolotlClient.CONFIG.outlineColor.get().getAsInt();
			float a = (float) (color >> 24 & 0xFF) / 255.0F;
			float r = (float) (color >> 16 & 0xFF) / 255.0F;
			float g = (float) (color >> 8 & 0xFF) / 255.0F;
			float b = (float) (color & 0xFF) / 255.0F;
			args.set(6, r);
			args.set(7, g);
			args.set(8, b);
			args.set(9, a);
		}
	}

	@Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$changeWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.noRain.get()) {
			ci.cancel();
		}
	}
}
