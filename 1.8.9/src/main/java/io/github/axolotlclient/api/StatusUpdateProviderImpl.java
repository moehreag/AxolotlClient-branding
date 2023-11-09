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

package io.github.axolotlclient.api;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import io.github.axolotlclient.api.requests.StatusUpdate;
import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.modules.hypixel.HypixelLocation;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.events.Events;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.level.LevelInfo;

public class StatusUpdateProviderImpl implements StatusUpdateProvider {

	private final Instant time = Instant.now();

	@Override
	public void initialize() {
		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(event ->
			event.setCancelled(HypixelLocation.waitingForResponse(event.getOriginalMessage())));
	}

	@Override
	public Request getStatus() {

		Screen current = MinecraftClient.getInstance().currentScreen;
		if (current instanceof TitleScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.MAIN_MENU);
		} else if (current instanceof MultiplayerScreen) {
			return StatusUpdate.online(StatusUpdate.MenuId.SERVER_LIST);
		} else if (current != null) {
			return StatusUpdate.online(StatusUpdate.MenuId.SETTINGS);
		}

		ServerInfo entry = MinecraftClient.getInstance().getCurrentServerEntry();
		if (entry != null) {

			if (!entry.isLocal()) {
				Optional<StatusUpdate.SupportedServer> optional = Arrays.stream(StatusUpdate.SupportedServer.values()).filter(s -> s.getAdress().matcher(entry.address).matches()).findFirst();
				if (optional.isPresent()) {
					StatusUpdate.SupportedServer server = optional.get();
					if (server.equals(StatusUpdate.SupportedServer.HYPIXEL)) {
						AtomicReference<JsonObject> loc = new AtomicReference<>();
						HypixelLocation.get(s -> loc.set(GsonHelper.GSON.fromJson(s, JsonObject.class)));
						JsonObject object = loc.get();
						StatusUpdate.GameType gameType = StatusUpdate.GameType.valueOf(object.get("gametype").getAsString());
						String gameMode = getOrEmpty(object, "mode");
						String map = getOrEmpty(object, "map");
						int maxPlayers = MinecraftClient.getInstance().world.playerEntities.size();
						int players = MinecraftClient.getInstance().world.playerEntities.stream()
							.filter(e -> getGameMode(e) != LevelInfo.GameMode.CREATIVE && getGameMode(e) != LevelInfo.GameMode.SPECTATOR).mapToInt(value -> 1).reduce(0, Integer::sum);
						return StatusUpdate.inGame(server, gameType.toString(), gameMode, map, players, maxPlayers, Instant.now().getEpochSecond() - time.getEpochSecond());
					}
				}
			}

			String gamemode = getGameModeString(MinecraftClient.getInstance().player);
			return StatusUpdate.inGameUnknown(entry.address, "", entry.name, gamemode, Instant.now().getEpochSecond() - time.getEpochSecond());

		}

		return null;
	}

	private String getOrEmpty(JsonObject object, String name) {
		return object.has(name) ? object.get(name).getAsString() : "";
	}

	private LevelInfo.GameMode getGameMode(PlayerEntity entity) {
		return MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()).getGameMode();
	}

	private String getGameModeString(PlayerEntity entity) {
		PlayerListEntry entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid());
		switch (entry.getGameMode()) {
			case CREATIVE:
				return "Creative Mode";
			case SURVIVAL:
				return "Survival Mode";
			case SPECTATOR:
				return "Spectator Mode";
			case ADVENTURE:
				return "Adventure Mode";
			default:
				return "";
		}
	}
}
