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

import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public float yaw;

	@Shadow
	public float pitch;

	@Inject(method = "updateLocalPlayerCamera", at = @At("HEAD"))
	private void axolotlclient$updateLookDirection(float yaw, float pitch, CallbackInfo ci) {
		if (yaw == 0 && pitch == 0) {
			return;
		}

		float prevPitch = this.pitch;
		float prevYaw = this.yaw;
		pitch = (float) ((double) prevPitch - (double) pitch * 0.15);
		yaw = (float) ((double) prevYaw + (double) yaw * 0.15);
		pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
		Events.PLAYER_DIRECTION_CHANGE.invoker().invoke(new PlayerDirectionChangeEvent(prevPitch, prevYaw, pitch, yaw));
	}
}
