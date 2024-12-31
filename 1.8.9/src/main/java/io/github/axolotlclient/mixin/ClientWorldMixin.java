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

import java.util.Objects;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

	@Inject(method = "onEntityRemoved", at = @At("HEAD"))
	public void axolotlclient$onEntityRemoved(Entity entity, CallbackInfo ci) {
		if (entity instanceof PlayerEntity && Objects.equals(HypixelMods.getInstance().cacheMode.get(),
			HypixelMods.HypixelApiCacheMode.ON_PLAYER_DISCONNECT)) {
			HypixelAbstractionLayer.handleDisconnectEvents(entity.getUuid());
		}
	}

	@ModifyArg(method = "setTimeOfDay", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTimeOfDay(J)V"))
	public long axolotlclient$timeChanger(long time) {
		if (AxolotlClient.CONFIG.timeChangerEnabled.get()) {
			return AxolotlClient.CONFIG.customTime.get();
		}
		return time;
	}
}
