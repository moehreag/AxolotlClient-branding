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

import io.github.axolotlclient.modules.freelook.Freelook;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

	@Shadow
	private float pitch;

	@Shadow
	private float yaw;

	@Inject(method = "update", at = @At(value = "INVOKE", target = "net/minecraft/client/render/Camera.moveBy(DDD)V", ordinal = 0))
	private void axolotlclient$perspectiveUpdatePitchYaw(BlockView area, Entity focusedEntity, boolean thirdPerson,
														 boolean inverseView, float tickDelta, CallbackInfo ci) {
		this.pitch = Freelook.getInstance().pitch(pitch)
					 * (inverseView && Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive() ? -1 : 1);
		this.yaw = Freelook.getInstance().yaw(yaw)
				   + (inverseView && Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive() ? 180 : 0);
	}

	@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "net/minecraft/client/render/Camera.setRotation(FF)V", ordinal = 0))
	private void axolotlclient$perspectiveFixRotation(Args args) {
		args.set(0, Freelook.getInstance().yaw(args.get(0)));
		args.set(1, Freelook.getInstance().pitch(args.get(1)));
	}

	@ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(DDD)V", ordinal = 0), index = 0)
	private double axolotlclient$correctDistance(double x) {
		if (Freelook.getInstance().enabled.get() && Freelook.getInstance().isActive()
			&& MinecraftClient.getInstance().options.getPerspective().isFrontView()) {
			return -clipToSpace(4);
		}
		return x;
	}

	@Shadow
	protected abstract double clipToSpace(double desiredCameraDistance);
}
