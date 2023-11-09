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

package io.github.axolotlclient.api.requests;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;

public class ChannelRequest {

	public static CompletableFuture<Channel> getById(String id) {
		return API.getInstance().send(new Request(Request.Type.GET_CHANNEL_BY_ID, id)).handleAsync(ChannelRequest::parseChannelResponse);
	}

	private static Channel parseChannelResponse(ByteBuf object, Throwable t) {
		if (t != null) {
			APIError.display(t);
			return null;
		}
		return parseChannel(object);
	}

	private static Channel parseChannel(ByteBuf channel) {
		String id = BufferUtil.getString(channel, 0x09, 5);
		String name = BufferUtil.getString(channel, 0x0E, 64).trim();

		List<User> users = new ArrayList<>();
		int i = 0x4E;
		while (i < channel.getInt(0x53)) {
			String uuid = BufferUtil.getString(channel, i, 16);
			io.github.axolotlclient.api.requests.User.get(uuid).whenCompleteAsync((user, throwable) -> users.add(user));
			i += 16;
		}
		List<ChatMessage> messages = new ArrayList<>();
		int offset = i + 8;
		while (i < channel.getInt(offset)) {
			messages.add(parseMessage(channel.slice(i, 0x1D + channel.getInt(i + 0x19))));
			i += 0x1D + channel.getInt(i + 0x19);
		}


		if (users.size() == 2) {
			return new Channel.DM(id, users.toArray(new User[0]), messages.toArray(new ChatMessage[0]));
		} else if (users.size() > 2) {
			return new Channel.Group(id, users.toArray(new User[0]), name, messages.toArray(new ChatMessage[0]));
		}

		throw new UnsupportedOperationException("Unknown message channel type: " + channel.toString(StandardCharsets.UTF_8));
	}

	private static ChatMessage parseMessage(ByteBuf buf) {
		AtomicReference<User> u = new AtomicReference<>();
		io.github.axolotlclient.api.requests.User.get(BufferUtil.getString(buf, 0x00, 16)).whenCompleteAsync((us, t) -> u.set(us));

		return new ChatMessage(u.get(), BufferUtil.getString(buf, 0x1D, buf.getInt(0x19)),
			ChatMessage.Type.fromCode(buf.getByte(0x18)), buf.getLong(0x10));
	}

	public static CompletableFuture<List<Channel>> getChannelList() {
		return API.getInstance().send(new Request(Request.Type.GET_CHANNEL_LIST)).handleAsync(ChannelRequest::parseChannels);
	}

	private static List<Channel> parseChannels(ByteBuf object, Throwable t) {
		if (t != null) {
			APIError.display(t);
			return Collections.emptyList();
		}
		List<Channel> channelList = new ArrayList<>();

		int i = object.getInt(0x0D);
		while (i < object.getInt(0x09)) {
			getById(BufferUtil.getString(object, i, 5)).whenCompleteAsync((channel, throwable) -> channelList.add(channel));
			i += 5;
		}

		return channelList;
	}

	public static CompletableFuture<Channel> getOrCreateGroup(String... users) {
		return API.getInstance().send(new Request(Request.Type.GET_OR_CREATE_CHANNEL,
			new Request.Data((byte) users.length).add(users))).handleAsync(ChannelRequest::parseChannelResponse);
	}

	public static CompletableFuture<Channel> getOrCreateDM(String uuid) {
		return API.getInstance().send(new Request(Request.Type.GET_OR_CREATE_CHANNEL,
			new Request.Data((byte) 1).add(uuid))).handleAsync(ChannelRequest::parseChannelResponse);
	}

	public static void createGroup(String... uuids) {
		API.getInstance().send(new Request(Request.Type.CREATE_CHANNEL,
			new Request.Data((byte) uuids.length).add(uuids)));
	}
}
