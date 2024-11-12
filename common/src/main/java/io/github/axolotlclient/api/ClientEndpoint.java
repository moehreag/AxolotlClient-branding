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

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;


public class ClientEndpoint implements WebSocket.Listener {

	private StringBuilder buf;

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		if (buf == null) {
			buf = new StringBuilder();
		}
		buf.append(data);
		if (last) {
			API.getInstance().onMessage(buf.toString());
			buf = null;
		}
		webSocket.request(1);
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		API.getInstance().onClose(statusCode, reason);
		webSocket.request(1);
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		API.getInstance().onOpen(webSocket);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		API.getInstance().onError(error);
		WebSocket.Listener.super.onError(webSocket, error);
	}

	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
		byte[] bytes = new byte[message.remaining()];
		message.get(bytes);
		API.getInstance().logDetailed("received pong: {}", Arrays.toString(bytes));
		webSocket.request(1);
		return WebSocket.Listener.super.onPong(webSocket, message);
	}

	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		byte[] bytes = new byte[message.remaining()];
		message.get(bytes);
		API.getInstance().logDetailed("received ping: {}", Arrays.toString(bytes));
		webSocket.request(1);
		return WebSocket.Listener.super.onPing(webSocket, message);
	}
}
