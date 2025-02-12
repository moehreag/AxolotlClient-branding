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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.modules.freelook.Freelook;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	protected abstract float getMaxZoom(float distance);

	@Shadow
	private float yRot;

	@Shadow
	private float xRot;

	@Inject(method = "setup", at = @At(value = "INVOKE", target = "net/minecraft/client/Camera.move(FFF)V",
		ordinal = 0))
	private void axolotlclient$perspectiveUpdatePitchYaw(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		this.xRot = Freelook.getInstance().pitch(xRot)
					* (inverseView && Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive() ? -1 : 1);
		this.yRot = Freelook.getInstance().yaw(yRot)
					+ (inverseView && Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive() ? 180 : 0);
	}

	@WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation" +
																		 "(FF)V", ordinal = 0))
	private void axolotlclient$perspectiveFixRotation(Camera instance, float yaw, float pitch, Operation<Void> original) {
		yaw = Freelook.getInstance().yaw(yaw);
		pitch = Freelook.getInstance().pitch(pitch);
		original.call(instance, yaw, pitch);
	}

	@WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation" +
																		 "(FF)V", ordinal = 1))
	private void axolotlclient$perspectiveFixRotation2(Camera instance, float yaw, float pitch,
													   Operation<Void> original) {
		axolotlclient$perspectiveFixRotation(instance, yaw, pitch, original);
	}

	@ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;move(FFF)V",
		ordinal = 0), index = 0)
	private float axolotlclient$correctDistance(float x, @Local(argsOnly = true) Entity focused) {
		if (Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive()
			&& Minecraft.getInstance().options.getCameraType().isMirrored()) {
			return -getMaxZoom(4 * (focused instanceof LivingEntity e ? e.getScale() : 1.0f));
		}
		return x;
	}
}
