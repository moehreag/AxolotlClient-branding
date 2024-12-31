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

import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.simple.ComboHud;
import io.github.axolotlclient.modules.hud.gui.hud.simple.ReachHud;
import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
	private void axolotlclient$getReach(Entity entity, CallbackInfo ci) {
		if ((Object) this == Minecraft.getInstance().player
			|| entity.equals(Minecraft.getInstance().player)) {
			ReachHud reachDisplayHud = (ReachHud) HudManager.getInstance().get(ReachHud.ID);
			if (reachDisplayHud != null && reachDisplayHud.isEnabled()) {
				reachDisplayHud.updateDistance(this, entity);
			}

			ComboHud comboHud = (ComboHud) HudManager.getInstance().get(ComboHud.ID);
			comboHud.onEntityAttack(entity);
		}
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setLastHurtMob(Lnet/minecraft/world/entity/Entity;)V"))
	private void axolotlclient$alwaysCrit(Entity entity, CallbackInfo ci) {
		if (Particles.getInstance().getAlwaysOn(ParticleTypes.CRIT)) {
			Minecraft.getInstance().player.crit(entity);
		}
		if (Particles.getInstance().getAlwaysOn(ParticleTypes.ENCHANTED_HIT)) {
			Minecraft.getInstance().player.magicCrit(entity);
		}
	}

	// TODO this doesn't work currently
	/*@Inject(method = "hurtServer", at = @At("HEAD"))
	private void axolotlclient$damage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getEntity() != null && getUUID() == Minecraft.getInstance().player.getUUID()) {
			ReachHud reachDisplayHud = (ReachHud) HudManager.getInstance().get(ReachHud.ID);
			if (reachDisplayHud != null && reachDisplayHud.isEnabled()) {
				reachDisplayHud.updateDistance(source.getEntity(), this);
			}
		}

		if (source.getEntity() instanceof Player) {
			ComboHud comboHud = (ComboHud) HudManager.getInstance().get(ComboHud.ID);
			comboHud.onEntityDamage(this);
		}
	}*/
}
