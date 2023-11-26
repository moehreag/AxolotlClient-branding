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
import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.blur.MotionBlur;
import io.github.axolotlclient.modules.zoom.Zoom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Final
	@Shadow
	private MinecraftClient client;

	@Inject(method = "getFov", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	public void axolotlclient$setZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		Zoom.update();
		double returnValue = cir.getReturnValue();

		if (!AxolotlClient.CONFIG.dynamicFOV.get()) {
			Entity entity = this.client.getCameraEntity();
			double f = changingFov ? client.options.fov : 70F;
			if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() <= 0.0F) {
				float g = (float) ((LivingEntity) entity).deathTime + tickDelta;
				f /= (1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F;
			}

			FluidState fluidState = camera.getSubmergedFluidState();
			if (!fluidState.isEmpty()) {
				f = f * 60.0 / 70.0;
			}
			returnValue = f;
		}
		returnValue = Zoom.getFov(returnValue, tickDelta);

		cir.setReturnValue(returnValue);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getFramebuffer()Lnet/minecraft/client/gl/Framebuffer;"))
	public void axolotlclient$worldMotionBlur(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		MenuBlur.getInstance().updateBlur();
		axolotlclient$motionBlur(tickDelta, startTime, tick, null);
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void axolotlclient$motionBlur(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (ci != null && !MotionBlur.getInstance().inGuis.get()) {
			return;
		}

		this.client.getProfiler().push("Motion Blur");

		if (MotionBlur.getInstance().enabled.get()) {
			MotionBlur blur = MotionBlur.getInstance();
			blur.onUpdate();
			blur.shader.render(tickDelta);
			RenderSystem.enableTexture();
		}

		this.client.getProfiler().pop();
	}

	@Inject(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void axolotlclient$minimalViewBob(MatrixStack matrixStack, float f, CallbackInfo ci, PlayerEntity playerEntity, float g, float h, float i) {
		if (AxolotlClient.CONFIG.minimalViewBob.get()) {
			h /= 2;
			i /= 2;
			matrixStack.translate((double) (MathHelper.sin(h * (float) Math.PI) * i * 0.5F), (double) (-Math.abs(MathHelper.cos(h * (float) Math.PI) * i)), 0.0);
			matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(h * (float) Math.PI) * i * 3.0F));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(Math.abs(MathHelper.cos(h * (float) Math.PI - 0.2F) * i) * 5.0F));
			ci.cancel();
		}
	}

	@Inject(method = "bobViewWhenHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;", ordinal = 1), cancellable = true)
	private void axolotlclient$noHurtCam(MatrixStack matrixStack, float f, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.noHurtCam.get()) {
			ci.cancel();
		}
	}
}
