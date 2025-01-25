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

import java.nio.FloatBuffer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.blur.MotionBlur;
import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.CrosshairHud;
import io.github.axolotlclient.modules.hypixel.skyblock.Skyblock;
import io.github.axolotlclient.modules.sky.SkyboxManager;
import io.github.axolotlclient.modules.unfocusedFpsLimiter.UnfocusedFpsLimiter;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Shadow
	private Minecraft minecraft;
	@Shadow
	private float viewDistance;
	@Shadow
	private float fogRed;
	@Shadow
	private float fogGreen;
	@Shadow
	private float fogBlue;
	@Shadow
	private boolean thiccFog;
	@Shadow
	private boolean debugCamera;

	@Shadow
	protected abstract FloatBuffer setFogColor(float par1, float par2, float par3, float par4);


	@Inject(method = "renderFog", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$noFog(int i, float tickDelta, CallbackInfo ci) {
		if (Minecraft.getInstance().world.dimension.isOverworld() && AxolotlClient.CONFIG.customSky.get()
			&& SkyboxManager.getInstance().hasSkyBoxes()) {
			this.viewDistance = (float) (this.viewDistance * 2 + Minecraft.getInstance().player.getSourcePos().y);
			Entity entity = this.minecraft.getCamera();

			GL11.glFog(2918, this.setFogColor(this.fogRed, this.fogGreen, this.fogBlue, 1.0F));
			GL11.glNormal3f(0.0F, -1.0F, 0.0F);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			Block block = Camera.getBlockInside(this.minecraft.world, entity, tickDelta);
			if (entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffect.BLINDNESS)) {
				float f = 5.0F;
				int j = ((LivingEntity) entity).getEffectInstance(StatusEffect.BLINDNESS).getDuration();
				if (j < 20) {
					f = 5.0F + (this.viewDistance - 5.0F) * (1.0F - (float) j / 20.0F);
				}

				GlStateManager.fogMode(9729);
				if (i == -1) {
					GlStateManager.fogStart(0.0F);
					GlStateManager.fogEnd(f * 0.8F);
				} else {
					GlStateManager.fogStart(f * 0.25F);
					GlStateManager.fogEnd(f);
				}

				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}
			} else if (this.thiccFog) {
				GlStateManager.fogMode(2048);
				GlStateManager.fogDensity(0.1F);
			} else if (block.getMaterial() == Material.WATER) {
				GlStateManager.fogMode(2048);
				if (entity instanceof LivingEntity
					&& ((LivingEntity) entity).hasStatusEffect(StatusEffect.WATER_BREATHING)) {
					GlStateManager.fogDensity(0.01F);
				} else {
					GlStateManager.fogDensity(0.1F - (float) EnchantmentHelper.getRespirationLevel(entity) * 0.03F);
				}
			} else if (block.getMaterial() == Material.LAVA) {
				GlStateManager.fogMode(2048);
				GlStateManager.fogDensity(2.0F);
			} else {
				float f = this.viewDistance;
				GlStateManager.fogMode(9729);
				GlStateManager.fogStart(f - 0.01F);
				GlStateManager.fogEnd(f);

				if (this.minecraft.world.dimension.isFogThick((int) entity.x, (int) entity.z)) {
					GlStateManager.fogStart(f * 0.05F);
					GlStateManager.fogEnd(Math.min(f, 192.0F) * 0.5F);
				}
			}

			GlStateManager.enableColorMaterial();
			GlStateManager.enableFog();
			GlStateManager.colorMaterial(1028, 4608);
			ci.cancel();
		}
	}

	@Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
	public void axolotlclient$setZoom(float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {

		if (debugCamera) {
			return;
		}

		Zoom.update();

		float returnValue = cir.getReturnValue();

		if (!AxolotlClient.CONFIG.dynamicFOV.get()) {
			Entity entity = this.minecraft.getCamera();
			float f = changingFov ? minecraft.options.fov : 70F;
			if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() <= 0.0F) {
				float g = (float) ((LivingEntity) entity).deathTime + tickDelta;
				f /= (1.0F - 500.0F / (g + 500.0F)) * 2.0F + 1.0F;
			}

			Block block = Camera.getBlockInside(this.minecraft.world, entity, tickDelta);
			if (block.getMaterial() == Material.WATER) {
				f = f * 60.0F / 70.0F;
			}
			returnValue = f;
		}

		returnValue = (float) Zoom.getFov(returnValue, tickDelta);

		cir.setReturnValue(returnValue);
	}

	@Redirect(method = "updateLightMap", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;gamma:F", opcode = Opcodes.GETFIELD))
	public float axolotlclient$setGamma(GameOptions instance) {
		if (AxolotlClient.CONFIG.fullBright.get())
			return 15F;
		return instance.gamma;
	}

	@Inject(method = "renderAxisIndicators", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customCrosshairF3(float tickDelta, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud.isEnabled() && this.minecraft.options.debugEnabled && !this.minecraft.options.hideGui
			&& hud.overridesF3()) {
			ci.cancel();
		}
	}

	@Inject(method = "render(FJ)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", shift = Shift.BEFORE))
	public void axolotlclient$worldMotionBlur(float tickDelta, long nanoTime, CallbackInfo ci) {
		MenuBlur.getInstance().updateBlur();
		axolotlclient$postRender(tickDelta, nanoTime, null);
	}

	@Inject(method = "render(FJ)V", at = @At("TAIL"))
	private void renderNotifications(float f, long l, CallbackInfo ci) {
		Notifications.getInstance().getToastManager().render();
	}

	@Inject(method = "render(FJ)V", at = @At("TAIL"))
	public void axolotlclient$postRender(float tickDelta, long nanoTime, CallbackInfo ci) {
		if ((ci == null) == MotionBlur.getInstance().inGuis.get()) {
			return;
		}

		this.minecraft.profiler.push("Motion Blur");

		if (MotionBlur.getInstance().enabled.get() && GLX.usePostProcess) {
			MotionBlur blur = MotionBlur.getInstance();
			blur.onUpdate();
			blur.shader.process(tickDelta);
		}

		this.minecraft.profiler.pop();
	}

	@WrapOperation(method = "render(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;updateLocalPlayerCamera(FF)V"))
	public void axolotlclient$updateRotation(LocalClientPlayerEntity instance, float yaw, float pitch, Operation<Void> original) {
		Freelook freelook = Freelook.getInstance();

		if (freelook.consumeRotation(yaw, pitch) || Skyblock.getInstance().rotationLocked.get())
			return;

		original.call(instance, yaw, pitch);
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"))
	public float axolotlclient$freelook$yaw(Entity entity) {
		return Freelook.getInstance().yaw(entity.yaw);
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F"))
	public float axolotlclient$freelook$prevYaw(Entity entity) {
		return Freelook.getInstance().yaw(entity.prevYaw);
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F"))
	public float axolotlclient$freelook$pitch(Entity entity) {
		return Freelook.getInstance().pitch(entity.pitch);
	}

	@Redirect(method = "transformCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPitch:F"))
	public float axolotlclient$freelook$prevPitch(Entity entity) {
		return Freelook.getInstance().pitch(entity.prevPitch);
	}

	@Inject(method = "render(FJ)V", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$limitFpsOnLostFocus(float tickDelta, long nanoTime, CallbackInfo ci) {
		if (!UnfocusedFpsLimiter.getInstance().checkForRender()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$changeWeather(float tickDelta, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.noRain.get()) {
			ci.cancel();
		}
	}

	@Inject(method = "applyViewBobbing", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;translatef(FFF)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void axolotlclient$minimalViewBob(float f, CallbackInfo ci, PlayerEntity entity, float g, float h, float i, float j) {
		if (AxolotlClient.CONFIG.minimalViewBob.get()) {
			h /= 2;
			i /= 2;
			j /= 2;
			GlStateManager.translatef(MathHelper.sin(h * (float) Math.PI) * i * 0.5F, -Math.abs(MathHelper.cos(h * (float) Math.PI) * i), 0.0F);
			GlStateManager.rotatef(MathHelper.sin(h * (float) Math.PI) * i * 3.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotatef(Math.abs(MathHelper.cos(h * (float) Math.PI - 0.2F) * i) * 5.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotatef(j, 1.0F, 0.0F, 0.0F);
			ci.cancel();
		}
	}

	@Inject(method = "applyHurtCam", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCamera()Lnet/minecraft/entity/Entity;", ordinal = 1), cancellable = true)
	private void axolotlclient$noHurtCam(float f, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.noHurtCam.get()) {
			ci.cancel();
		}
	}
}
