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
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.particle.ParticleType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

	public PlayerEntityMixin(World world) {
		super(world);
	}

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;getAttribute(Lnet/minecraft/entity/living/attribute/EntityAttribute;)Lnet/minecraft/entity/living/attribute/EntityAttributeInstance;"))
	public void axolotlclient$getReach(Entity entity, CallbackInfo ci) {
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

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;addCritParticles(Lnet/minecraft/entity/Entity;)V"))
	public void axolotlclient$alwaysCrit(Entity entity, CallbackInfo ci) {
		if (Particles.getInstance().getAlwaysOn(ParticleType.CRIT)) {
			Minecraft.getInstance().player.addCritParticles(entity);
		}
		if (Particles.getInstance().getAlwaysOn(ParticleType.CRIT_MAGIC)) {
			Minecraft.getInstance().player.addEnchantedCritParticles(entity);
		}
	}

	@Inject(method = "damage", at = @At("HEAD"))
	public void axolotlclient$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getAttacker() != null && getUuid() == Minecraft.getInstance().player.getUuid()) {
			ReachHud reachDisplayHud = (ReachHud) HudManager.getInstance().get(ReachHud.ID);
			if (reachDisplayHud != null && reachDisplayHud.isEnabled()) {
				reachDisplayHud.updateDistance(source.getAttacker(), this);
			}
		}

		if (source.getAttacker() instanceof PlayerEntity) {
			ComboHud comboHud = (ComboHud) HudManager.getInstance().get(ComboHud.ID);
			comboHud.onEntityDamage(this);
		}
	}

	@Inject(
		method = "getArmorProtection",
		at = @At(
			"HEAD"
		),
		cancellable = true
	)
	public void axolotlclient$disableArmor(CallbackInfoReturnable<Integer> ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && !BedwarsMod.getInstance().displayArmor.get()) {
			ci.setReturnValue(0);
		}
	}
}
