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

package io.github.axolotlclient.modules.rpc;

import java.time.Instant;

import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.OSUtil;

public abstract class RPCCommon implements Module {

	private static final long CLIENT_ID = 875835666729152573L;
	private IPCClient client;

	private static boolean running;
	public final BooleanOption enabled = new BooleanOption("enabled", value -> {
		if (value) {
			initRPC();
		} else {
			shutdown();
		}
	}, false);
	public final OptionCategory category = new OptionCategory("rpc");
	public final BooleanOption showActivity = new BooleanOption("showActivity", true);
	public final EnumOption showServerNameMode = new EnumOption("showServerNameMode",
		new String[]{"showIp", "showName", "off"}, "off");
	public final BooleanOption showTime = new BooleanOption("showTime", true);
	private final Instant time = Instant.now();
	private final Logger logger;

	public RPCCommon(Logger logger) {
		this.logger = logger;
	}

	public void init() {
		category.add(enabled, showTime, showActivity, showServerNameMode);

		if (OSUtil.getOS() == OSUtil.OperatingSystem.OTHER) {
			enabled.setForceOff(true, "crash");
		}
	}

	public void tick() {
		if (!running && enabled.get()) {
			initRPC();
		}

		if (running) {
			updateRPC();
		}
	}

	public void shutdown() {
		if (running) {
			client.close();
			running = false;
		}
	}

	protected RichPresence createRichPresence(String gameVersion, String state, String details) {
		RichPresence.Builder builder = new RichPresence.Builder();
		builder.setLargeImage("icon", "AxolotlClient " + gameVersion);
		if (showTime.get()) {
			builder.setStartTimestamp(time.getEpochSecond());
		}
		builder.setState(state)
			.setDetails(details);
		return builder.build();
	}

	protected abstract void createRichPresence();

	protected void setRichPresence(RichPresence presence) {
		if (running && client != null) {
			client.sendRichPresence(presence);
		}
	}

	private void updateRPC() {
		createRichPresence();
	}

	public void initRPC() {
		if (enabled.get()) {
			if (client == null) {
				client = new IPCClient(CLIENT_ID);
				client.setListener(new IPCListener() {
					@Override
					public void onPacketSent(IPCClient client, Packet packet) {

					}

					@Override
					public void onPacketReceived(IPCClient client, Packet packet) {

					}

					@Override
					public void onActivityJoin(IPCClient client, String secret) {

					}

					@Override
					public void onActivitySpectate(IPCClient client, String secret) {

					}

					@Override
					public void onActivityJoinRequest(IPCClient client, String secret, User user) {

					}

					@Override
					public void onReady(IPCClient client) {
						createRichPresence();
					}

					@Override
					public void onClose(IPCClient client, JsonObject json) {
						logger.info("RPC Closed");
						running = false;
					}

					@Override
					public void onDisconnect(IPCClient client, Throwable t) {
						running = false;
					}
				});
			}
			try {
				client.connect();
				logger.info("Started RPC");
				running = true;
			} catch (Exception e) {
				enabled.set(false);
			}
		}
	}
}
