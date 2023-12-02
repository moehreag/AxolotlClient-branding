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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.github.axolotlclient.api.requests.ServerResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class ApiTestServer {

	@Getter
	private final static ApiTestServer instance = new ApiTestServer();

	private final static int PROTOCOL_VERSION = 0x01;
	private final static boolean LAUNCH_CLIENT = false;

	@Getter
	private final Logger logger = Logger.getLogger("Api Test Server");

	public static void main(String[] args) {
		getInstance().startup();
	}

	private void startup() {
		logger.info("Starting Api Test Server on port 8081");
		try {
			int port = 8081;
			new NettyServer().startup(port);
			logger.info("Server started!");
			if (LAUNCH_CLIENT) {
				Thread.sleep(2000);
				TestClientThings t = TestClientThings.getInstance();
				new API(t, t, t, t, t);
				API.getInstance().getApiOptions().detailedLogging.fromSerializedValue("true");
				API.getInstance().startupAPI();
				new ClientEndpoint().run("127.0.0.1", port);
			}
		} catch (Exception e) {
			onError(e);
		}
	}

	public void onError(Throwable throwable) {
		logger.severe(throwable.getMessage());
		logger.severe(Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
	}

	public ByteBuf prependMetadata(ServerResponse request) {
		ByteBuf fi = Unpooled.buffer();
		fi.writeBytes("AXO".getBytes(StandardCharsets.UTF_8));
		fi.writeByte(Requests.getType(request.getClass()));
		fi.writeByte(PROTOCOL_VERSION);
		fi.writeInt(request.identifier);
		fi.writeBytes(request.serialize());
		return fi;
	}
}
