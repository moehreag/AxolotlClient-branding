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
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.ThreadExecuter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.handler.ClientQueryPacketHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.NetworkProtocol;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.query.PingC2SPacket;
import net.minecraft.network.packet.c2s.query.ServerStatusC2SPacket;
import net.minecraft.network.packet.s2c.query.PingS2CPacket;
import net.minecraft.network.packet.s2c.query.ServerStatusS2CPacket;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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
		if (Minecraft.getInstance().getCurrentServerEntry() != null) {
			ServerAddress address = ServerAddress
				.parse(Minecraft.getInstance().getCurrentServerEntry().address);
			getRealTimeServerPing(address.getAddress(), address.getPort());
		} else if (((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress() != null) {
			getRealTimeServerPing(((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress(),
				((MinecraftClientAccessor) Minecraft.getInstance()).getServerPort());
		} else if (Minecraft.getInstance().isIntegratedServerRunning()) {
			currentServerPing = 1;
		}
	}

	//Indicatia removed this feature...
	//We still need it :(
	private void getRealTimeServerPing(String address, int port) {
		ThreadExecuter.scheduleTask(() -> {
			try {
				final Connection manager = Connection.connect(InetAddress.getByName(address), port, false);

				manager.setListener(new ClientQueryPacketHandler() {

					private long currentSystemTime = 0L;

					@Override
					public void onDisconnect(Text text) {

					}

					@Override
					public void handleServerStatus(ServerStatusS2CPacket serverStatusS2CPacket) {
						this.currentSystemTime = Minecraft.getTime();
						manager.send(new PingC2SPacket(this.currentSystemTime));
					}

					@Override
					public void handlePing(PingS2CPacket pingS2CPacket) {
						long time = this.currentSystemTime;
						long latency = Minecraft.getTime();
						currentServerPing = (int) (latency - time);
						manager.disconnect(new LiteralText(""));
					}
				});
				manager.send(new HandshakeC2SPacket(47, address, port, NetworkProtocol.STATUS));
				manager.send(new ServerStatusC2SPacket());
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
