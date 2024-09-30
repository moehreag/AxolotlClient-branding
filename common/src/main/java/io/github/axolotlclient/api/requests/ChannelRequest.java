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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.types.Channel;

public class ChannelRequest {

	public static CompletableFuture<Channel> getById(String id) {
		//return API.getInstance().get(Request.builder().route(Request.Route.CHANNEL).build());
		return new CompletableFuture<>();
	}

	/*private static Channel parseChannelResponse(ByteBuf object, Throwable t) {
		if (t != null) {
			APIError.display(t);
			return null;
		}
		return parseChannel(object);
	}*/

	/*private static Channel parseChannel(ByteBuf channel) {
		String id = BufferUtil.getString(channel, 0x09, 5);
		String name = BufferUtil.getString(channel, 0x0E, 64).trim();

		API.getInstance().logDetailed("Parsing channel: "+id+" ("+name+")");

		List<User> users = new ArrayList<>();
		int i = 0x4E;
		while (i < channel.getInt(0x53)) {
			String uuid = BufferUtil.getString(channel, i, 32);
			io.github.axolotlclient.api.requests.User.get(uuid).thenAccept(users::add);
			i += 32;
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
	}*/

	/*private static ChatMessage parseMessage(ByteBuf buf) {
		return BufferUtil.unwrap(buf, ChatMessage.class);
	}*/

	public static CompletableFuture<List<Channel>> getChannelList() {
		//return API.getInstance().send(new RequestOld(RequestOld.Type.GET_CHANNEL_LIST)).handle(ChannelRequest::parseChannels);
		return new CompletableFuture<>();
	}

	/*private static List<Channel> parseChannels(ByteBuf object, Throwable t) {
		if (t != null) {
			APIError.display(t);
			return Collections.emptyList();
		}
		List<Channel> channelList = new ArrayList<>();

		int i = 0;
		while (i < object.getInt(0x09)) {
			String channelId = BufferUtil.getString(object, i+0x0D, 5);
			API.getInstance().logDetailed("Processing channelId: "+channelId);
			getById(channelId).thenAccept(channelList::add).join();
			i += 5;
		}

		return channelList;
	}*/

	private static CompletableFuture<Channel> createChannel(String name) {
		return API.getInstance().post(Request.builder().route(Request.Route.CHANNEL)
				.field("name", name).field("persistence", Collections.singletonMap("type", "channel")).build())
			.thenApply(response -> {
				String id = response.getPlainBody();
				return getById(id).join();
			});
	}

	private static CompletableFuture<Channel> createChannel(String name, String... users) {
		return API.getInstance().post(Request.builder().route(Request.Route.CHANNEL)
				.field("name", name).field("persistence", Collections.singletonMap("type", "channel")).build())
			.thenApply(Response::getPlainBody).thenCompose(ChannelRequest::getById);
	}

	public static CompletableFuture<Channel> getOrCreateGroup(String... users) {
		/*return API.getInstance().send(new RequestOld(RequestOld.Type.GET_OR_CREATE_CHANNEL,
			new RequestOld.Data(users.length).add(users))).handleAsync(ChannelRequest::parseChannelResponse);*/
		return new CompletableFuture<>();
	}

	public static CompletableFuture<Channel> getOrCreateDM(String uuid) {
		/*return API.getInstance().send(new RequestOld(RequestOld.Type.GET_OR_CREATE_CHANNEL,
			new RequestOld.Data((byte) 1).add(uuid))).handleAsync(ChannelRequest::parseChannelResponse);*/
		return new CompletableFuture<>();
	}

	public static void createGroup(String... uuids) {
		/*API.getInstance().send(new RequestOld(RequestOld.Type.CREATE_CHANNEL,
			new RequestOld.Data(uuids.length).add(uuids)));*/
	}
}
