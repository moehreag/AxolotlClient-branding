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

import java.net.InetAddress;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.ThreadExecuter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.ServerAddress;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PingHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "pinghud");
	private final IntegerOption refreshDelay = new IntegerOption("refreshTime", 4, 1, 15);
	private int currentServerPing;
	private int second;

	public PingHud() {
		super();
	}

	@Override
	public Identifier getId() {
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
		} else
			second++;
	}

	private void updatePing() {
		if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
			getRealTimeServerPing(MinecraftClient.getInstance().getCurrentServerEntry());
		} else if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
			currentServerPing = 1;
		}
	}

	//Indicatia removed this feature...
	//We still need it :(
	private void getRealTimeServerPing(ServerInfo server) {
		ThreadExecuter.scheduleTask(() -> {
			try {
				ServerAddress address = ServerAddress.parse(server.address);

				final ClientConnection manager = ClientConnection.connect(InetAddress.getByName(address.getAddress()), address.getPort(), false);
				manager.setPacketListener(new ClientQueryPacketListener() {

					private long currentSystemTime = 0L;

					@Override
					public void onResponse(QueryResponseS2CPacket packet) {
						this.currentSystemTime = net.minecraft.util.Util.getMeasuringTimeMs();
						manager.send(new QueryPingC2SPacket(this.currentSystemTime));
					}

					@Override
					public void onPong(QueryPongS2CPacket packet) {
						long time = this.currentSystemTime;
						long latency = net.minecraft.util.Util.getMeasuringTimeMs();
						currentServerPing = (int) (latency - time);
						manager.disconnect(Text.of(""));
					}

					@Override
					public void onDisconnected(Text reason) {
					}

					@Override
					public ClientConnection getConnection() {
						return manager;
					}
				});
				manager.send(new HandshakeC2SPacket(address.getAddress(), address.getPort(), NetworkState.STATUS));
				manager.send(new QueryRequestC2SPacket());

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
