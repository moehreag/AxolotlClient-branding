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

import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hypixel.skyblock.Skyblock;
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

	@Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$interceptMovement(double cursorDeltaX, double cursorDeltaY, CallbackInfo callback) {
		if (Freelook.getInstance().consumeRotation(cursorDeltaX, cursorDeltaY) || Skyblock.getInstance().rotationLocked.get()) {
			callback.cancel();
		}
	}

	@Inject(method = "changeLookDirection", at = @At("HEAD"))
	private void axolotlclient$updateLookDirection(double mouseDeltaX, double mouseDeltaY, CallbackInfo ci) {
		if (mouseDeltaX == 0 && mouseDeltaY == 0) {
			return;
		}

		float prevPitch = getPitch();
		float prevYaw = getYaw();
		float pitch = prevPitch + (float) (mouseDeltaY * .15);
		float yaw = prevYaw + (float) (mouseDeltaX * .15);
		pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
		Events.PLAYER_DIRECTION_CHANGE.invoker().invoke(new PlayerDirectionChangeEvent(prevPitch, prevYaw, pitch, yaw));
	}

	@Shadow
	public abstract float getPitch();

	@Shadow
	public abstract float getYaw();
}
