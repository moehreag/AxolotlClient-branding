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

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

import io.github.axolotlclient.util.ThreadExecuter;


public class ClientEndpoint implements WebSocket.Listener {

	private final StringBuilder buf = new StringBuilder();

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		buf.append(data);
		if (last) {
			String s = buf.toString();
			ThreadExecuter.scheduleTask(() -> API.getInstance().onMessage(s));
			buf.setLength(0);
		}
		webSocket.request(1);
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		webSocket.request(1);
		API.getInstance().onClose(statusCode, reason);
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		webSocket.request(1);
		API.getInstance().onOpen(webSocket);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		API.getInstance().onError(error);
		WebSocket.Listener.super.onError(webSocket, error);
	}

	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		webSocket.request(1);
		return WebSocket.Listener.super.onPing(webSocket, message);
	}
}
