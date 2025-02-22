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

package io.github.axolotlclient.modules.rpc;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;

public class DiscordRPC extends RPCCommon {
	private static DiscordRPC Instance;
	private String currentWorld = "";

	public DiscordRPC() {
		super(AxolotlClient.LOGGER);
	}

	public static DiscordRPC getInstance() {
		if (Instance == null)
			Instance = new DiscordRPC();
		return Instance;
	}

	public void setWorld(String world) {
		currentWorld = world;
	}

	@Override
	protected void createRichPresence() {

		String state = switch (showServerNameMode.get()) {
			case "showIp" -> MinecraftClient.getInstance().world == null ? "In the menu"
				: (MinecraftClient.getInstance().getCurrentServerEntry() == null ? "Singleplayer" : MinecraftClient.getInstance().getCurrentServerEntry().address);
			case "showName" -> MinecraftClient.getInstance().world == null ? "In the menu"
				: (MinecraftClient.getInstance().getCurrentServerEntry() == null
				? (MinecraftClient.getInstance().getCurrentServerEntry() == null ? "Singleplayer"
				: MinecraftClient.getInstance().getCurrentServerEntry().address)
				: MinecraftClient.getInstance().getCurrentServerEntry().name);
			default -> "";
		};

		String details;
		if (showActivity.get() && MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			details = (Util.getGame());
		} else if (showActivity.get() && !currentWorld.isEmpty()) {
			details = (currentWorld);
			currentWorld = "";
		} else {
			details = "";
		}

		setRichPresence(createRichPresence(AxolotlClient.VERSION, state, details));
	}

	public void init() {
		super.init();

		AxolotlClient.CONFIG.addCategory(category);
	}
}
