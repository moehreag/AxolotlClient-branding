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

package io.github.axolotlclient.api;

import java.util.Arrays;
import java.util.Optional;

import com.google.gson.JsonObject;
import io.github.axolotlclient.api.requests.StatusUpdate;
import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.api.worldhost.WorldHostStatusProvider;
import io.github.axolotlclient.modules.hypixel.HypixelGameType;
import io.github.axolotlclient.modules.hypixel.HypixelLocation;
import io.github.axolotlclient.modules.mcci.MccIslandMods;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.events.Events;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.SelectServerScreen;
import net.minecraft.client.network.ServerInfo;

public class StatusUpdateProviderImpl implements StatusUpdateProvider {

	@Override
	public void initialize() {
		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(event ->
			event.setCancelled(HypixelLocation.waitingForResponse(event.getOriginalMessage())));
	}

	@Override
	public Request getStatus() {
		ServerInfo entry = MinecraftClient.getInstance().getCurrentServerEntry();
		if (entry != null) {

			if (!entry.isLocal()) {
				Optional<StatusUpdate.SupportedServer> optional = Arrays.stream(StatusUpdate.SupportedServer.values()).filter(s -> s.getAddress().matcher(entry.address).matches()).findFirst();
				if (optional.isPresent()) {
					StatusUpdate.SupportedServer server = optional.get();
					if (server.equals(StatusUpdate.SupportedServer.HYPIXEL)) {
						JsonObject object = HypixelLocation.get().thenApply(GsonHelper::fromJson).join();
						HypixelGameType gameType = HypixelGameType.valueOf(object.get("gametype").getAsString());
						String gameMode = getOrEmpty(object, "mode");
						String map = getOrEmpty(object, "map");
						return StatusUpdate.inGame(server, gameType.getName(), gameMode, map);
					} else if (server.equals(StatusUpdate.SupportedServer.MCC_ISLAND)) {
						return MccIslandMods.getInstance().getMccIStatus();
					}
				}
			}
			return StatusUpdate.inGameUnknown(entry.name);
		} else if (MinecraftClient.getInstance().getServer() != null) {
			if (WorldHostStatusProvider.getWHStatusDescription() != null) {
				return StatusUpdate.worldHostStatusUpdate(WorldHostStatusProvider.getWHStatusDescription());
			}
			return StatusUpdate.inGameUnknown(MinecraftClient.getInstance().getServer().getSaveProperties().getWorldName());
		}
		Screen current = MinecraftClient.getInstance().currentScreen;
		if (current instanceof TitleScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.MAIN_MENU);
		} else if (current instanceof SelectServerScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.SERVER_LIST);
		} else if (current != null) {
			return StatusUpdate.online(StatusUpdate.MenuId.SETTINGS);
		}
		return null;
	}

	private String getOrEmpty(JsonObject object, String name) {
		return object.has(name) ? object.get(name).getAsString() : "";
	}
}
