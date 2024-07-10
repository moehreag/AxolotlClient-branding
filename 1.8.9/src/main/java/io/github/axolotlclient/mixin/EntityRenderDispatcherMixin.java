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
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;yaw:F"))
	public float axolotlclient$freelook$yaw(Entity entity) {
		return Freelook.getInstance().yaw(entity.yaw);
	}

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevYaw:F"))
	public float axolotlclient$freelook$prevYaw(Entity entity) {
		return Freelook.getInstance().yaw(entity.prevYaw);
	}

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pitch:F"))
	public float axolotlclient$freelook$pitch(Entity entity) {
		return Freelook.getInstance().pitch(entity.pitch);
	}

	@Redirect(method = "prepare", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPitch:F"))
	public float axolotlclient$freelook$prevPitch(Entity entity) {
		return Freelook.getInstance().pitch(entity.prevPitch);
	}
}
