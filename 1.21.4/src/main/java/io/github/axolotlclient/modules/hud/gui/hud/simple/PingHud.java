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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.ThreadExecuter;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.resources.ResourceLocation;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PingHud extends SimpleTextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "pinghud");
	private final IntegerOption refreshDelay = new IntegerOption("refreshTime", 4, 1, 15);
	private static final ServerStatusPinger pinger = new ServerStatusPinger();
	private long currentServerPing;
	private int second;

	public PingHud() {
		super();
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (second >= refreshDelay.get() * 20) {
			updatePing();
			second = 0;
		} else second++;
	}

	private void updatePing() {
		if (client.getCurrentServer() != null) {
			getRealTimeServerPing(client.getCurrentServer());
		} else if (client.hasSingleplayerServer()) {
			currentServerPing = 1;
		}
	}

	private void getRealTimeServerPing(ServerData server) {
		ThreadExecuter.scheduleTask(() -> {
			try {
				pinger.pingServer(server, () -> {}, () -> currentServerPing = server.ping);
			} catch (Exception ignored) {
			}
		});
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(refreshDelay);
		return options;
	}

	@Override
	public String getValue() {
		return currentServerPing + " ms";
	}

	@Override
	public String getPlaceholder() {
		return "68 ms";
	}
}
