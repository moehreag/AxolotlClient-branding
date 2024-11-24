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
import io.github.axolotlclient.modules.hypixel.HypixelLocation;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.events.Events;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;

public class StatusUpdateProviderImpl implements StatusUpdateProvider {

	@Override
	public void initialize() {
		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(
			event -> event.setCancelled(HypixelLocation.waitingForResponse(event.getOriginalMessage())));
	}

	@Override
	public Request getStatus() {

		Minecraft mc = Minecraft.getInstance();
		Screen current = mc.screen;
		if (current instanceof TitleScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.MAIN_MENU);
		} else if (current instanceof JoinMultiplayerScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.SERVER_LIST);
		} else if (!(current instanceof AbstractContainerScreen<?>)) {
			return StatusUpdate.online(StatusUpdate.MenuId.SETTINGS);
		}

		ServerData entry = mc.getCurrentServer();
		if (entry != null) {

			if (!entry.isLan()) {
				Optional<StatusUpdate.SupportedServer> optional = Arrays.stream(StatusUpdate.SupportedServer.values())
					.filter(s -> s.getAdress().matcher(entry.ip).matches()).findFirst();
				if (optional.isPresent()) {
					StatusUpdate.SupportedServer server = optional.get();
					if (server.equals(StatusUpdate.SupportedServer.HYPIXEL)) {
						JsonObject object = HypixelLocation.get().thenApply(GsonHelper::fromJson).join();
						StatusUpdate.GameType gameType =
							StatusUpdate.GameType.valueOf(object.get("gametype").getAsString());
						String gameMode = getOrEmpty(object, "mode");
						String map = getOrEmpty(object, "map");
						int maxPlayers = mc.level.players().size();
						int players = mc.level.players().stream()
							.filter(e -> !(e.isCreative() || e.isSpectator())).toList().size();
						return StatusUpdate.inGame(server, gameType.toString(), gameMode, map, players, maxPlayers);
					}
				}
			}
			return StatusUpdate.inGameUnknown(entry.name);
		} else if (mc.getSingleplayerServer() != null) {
			return StatusUpdate.inGameUnknown(mc.getSingleplayerServer().getWorldData().getLevelName());
		}
		return null;
	}

	private String getOrEmpty(JsonObject object, String name) {
		return object.has(name) ? object.get(name).getAsString() : "";
	}
}
