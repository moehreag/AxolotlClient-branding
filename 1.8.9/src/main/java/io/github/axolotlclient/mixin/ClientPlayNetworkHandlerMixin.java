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
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.simple.TPSHud;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityTeleportS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.team.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

	@Shadow
	private ClientWorld world;

	@Inject(method = "handleWorldTime", at = @At("HEAD"))
	private void axolotlclient$onWorldUpdate(WorldTimeS2CPacket packet, CallbackInfo ci) {
		TPSHud tpsHud = (TPSHud) HudManager.getInstance().get(TPSHud.ID);
		tpsHud.updateTime(packet.getTime());
	}

	@Inject(method = "handleTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;removeTeam(Lnet/minecraft/scoreboard/team/Team;)V"), cancellable = true)
	private void noStackTraceOnNullTeam(TeamS2CPacket teamS2CPacket, CallbackInfo ci, @Local Team team){
		if (team == null) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "handleTeam", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;addTeam(Ljava/lang/String;)Lnet/minecraft/scoreboard/team/Team;"))
	private Team noStackTraceOnAlreadyExistingTeam(Scoreboard instance, String s, Operation<Team> original, @Local Scoreboard scoreboard){
		Team team = scoreboard.getTeam(s);
		if (team == null) {
			return original.call(instance, s);
		}
		return team;
	}

	@Inject(method = "handleScoreboardObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;removeObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;)V"), cancellable = true)
	private void noStackTraceOnNullScoreboardObjective(ScoreboardObjectiveS2CPacket scoreboardObjectiveS2CPacket, CallbackInfo ci, @Local ScoreboardObjective objective){
		if (objective == null) {
			ci.cancel();
		}
	}

	@Inject(method = "handleEntityTeleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntity(I)Lnet/minecraft/entity/Entity;"), cancellable = true)
	private void noStackTraceOnNullWorld(EntityTeleportS2CPacket entityTeleportS2CPacket, CallbackInfo ci) {
		if (this.world == null) {
			ci.cancel();
		}
	}
}
