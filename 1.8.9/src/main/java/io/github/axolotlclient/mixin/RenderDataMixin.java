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
import net.minecraft.client.render.Camera;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class RenderDataMixin {

	@Redirect(method = "setup", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;yaw:F"))
	private static float axolotlclient$freelook$getYaw(PlayerEntity entity) {
		return Freelook.getInstance().yaw(entity.yaw);
	}

	@Redirect(method = "setup", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/living/player/PlayerEntity;pitch:F"))
	private static float axolotlclient$freelook$getPitch(PlayerEntity entity) {
		return Freelook.getInstance().pitch(entity.pitch);
	}
}
