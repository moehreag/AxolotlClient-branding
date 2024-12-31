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

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.rpc.DiscordRPC;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.WorldLoadEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

	/**
	 * @author meohreag
	 * @reason Customize Window title for use in AxolotlClient
	 */
	@Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$getWindowTitle(CallbackInfoReturnable<String> cir) {
		if (AxolotlClient.CONFIG.customWindowTitle.get()) {
			cir.setReturnValue("AxolotlClient " + SharedConstants.getCurrentVersion().getName());
		}
	}

	@Inject(method = "getVersionType", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$noVersionType(CallbackInfoReturnable<String> cir) {
		if (FabricLoader.getInstance().getModContainer("axolotlclient").isPresent()) {
			cir.setReturnValue(
				FabricLoader.getInstance().getModContainer("axolotlclient").get().getMetadata().getVersion()
					.getFriendlyString());
		}
	}

	@Inject(method = "stop", at = @At("HEAD"))
	private void axolotlclient$stop(CallbackInfo ci) {
		DiscordRPC.getInstance().shutdown();
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	private void axolotlclient$onWorldLoad(ClientLevel world, ReceivingLevelScreen.Reason type, CallbackInfo ci) {
		Events.WORLD_LOAD_EVENT.invoker().invoke(new WorldLoadEvent(world));
	}
}
