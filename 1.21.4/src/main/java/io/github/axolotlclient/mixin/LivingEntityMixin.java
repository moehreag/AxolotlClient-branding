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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}

	// TODO this doesn't work currently
	/*@Inject(method = "hurtServer", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;lastDamageStamp:J"))
	private void axolotlclient$onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		// The client doesn't really get any sort of information about why a person is damaged
		// Kinda sucks since that means combos can't be guarenteed (i.e. fall damage, or other person hits)
		// Possible fixes: Could wait for swing animation from a player to be played. Could then track eyes to see if hit, give or take
		// 2 ticks or so? Defintely not perfect tho
		if (source.getEntity() instanceof Player) {
			ComboHud comboHud = (ComboHud) HudManager.getInstance().get(ComboHud.ID);
			comboHud.onEntityDamage(this);
		}
	}*/
}
