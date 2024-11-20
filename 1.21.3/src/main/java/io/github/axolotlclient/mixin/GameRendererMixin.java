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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.blur.MotionBlur;
import io.github.axolotlclient.modules.zoom.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Final @Shadow private Minecraft minecraft;

	@Shadow @Final private CrossFrameResourcePool resourcePool;

	@Inject(method = "getFov", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	public void axolotlclient$setZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
		Zoom.update();
		double returnValue = cir.getReturnValue();

		if (!AxolotlClient.CONFIG.dynamicFOV.get()) {
			Entity entity = this.minecraft.getCameraEntity();
			double f = changingFov ? minecraft.options.fov().get() : 70F;
			if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() <= 0.0F) {
				float g = (float) ((LivingEntity) entity).deathTime + tickDelta;
				f /= (1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F;
			}

			FogType cameraSubmersionType = camera.getFluidInCamera();
			if (cameraSubmersionType == FogType.LAVA || cameraSubmersionType == FogType.WATER) {
				f *= Mth.lerp(this.minecraft.options.fovEffectScale().get(), 1.0, 0.85714287F);
			}
			returnValue = f;
		}
		returnValue = Zoom.getFov(returnValue, tickDelta);

		cir.setReturnValue((float) returnValue);
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
		target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V"))
	public void axolotlclient$worldMotionBlur(DeltaTracker tracker, boolean tick, CallbackInfo ci) {
		axolotlclient$motionBlur(tracker, tick, null);
	}

	@SuppressWarnings("deprecation")
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V", ordinal = 1))
	public void axolotlclient$motionBlur(DeltaTracker tracker, boolean tick, CallbackInfo ci) {
		if (ci != null && !MotionBlur.getInstance().inGuis.get()) {
			return;
		}

		Profiler.get().push("Motion Blur");

		if (MotionBlur.getInstance().enabled.get()) {
			MotionBlur blur = MotionBlur.getInstance();
			blur.onUpdate();
			/*RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.resetTextureMatrix();*/
			blur.shader.process(minecraft.getMainRenderTarget(), this.resourcePool);
		}

		Profiler.get().pop();
	}

	@Inject(method = "bobView",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
		cancellable = true)
	private void axolotlclient$minimalViewBob(PoseStack matrices, float tickDelta, CallbackInfo ci,
											  @Local(ordinal = 2) float g, @Local(ordinal = 3) float h) {
		if (AxolotlClient.CONFIG.minimalViewBob.get()) {
			g /= 2;
			h /= 2;
			matrices.translate(Mth.sin(g * (float) Math.PI) * h * 0.5F,
							   -Math.abs(Mth.cos(g * (float) Math.PI) * h), 0.0F
							  );
			matrices.mulPose(
				Axis.ZP.rotationDegrees(Mth.sin(g * (float) Math.PI) * h * 3.0F).get(new Matrix4f()));
			matrices.mulPose(
				Axis.XP.rotationDegrees(Math.abs(Mth.cos(g * (float) Math.PI - 0.2F) * h) * 5.0F)
					.get(new Matrix4f()));
			ci.cancel();
		}
	}

	@Inject(method = "bobHurt", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"),
		cancellable = true)
	private void axolotlclient$noHurtCam(PoseStack matrices, float tickDelta, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.noHurtCam.get()) {
			ci.cancel();
		}
	}
}
