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

package io.github.axolotlclient.api.requests;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.types.*;
import io.github.axolotlclient.api.util.UUIDHelper;

public class ChannelRequest {

	public static CompletableFuture<Channel> getById(String id) {
		if (id.isEmpty()) {
			return CompletableFuture.failedFuture(new IllegalArgumentException("Empty channel id"));
		}
		return API.getInstance().get(Request.Route.CHANNEL.builder().path(id).build()).thenApply(ChannelRequest::parseChannel);
	}

	@SuppressWarnings("unchecked")
	private static Channel parseChannel(Response response) {
		String id = Long.toUnsignedString(response.<Long>getBody("id"));
		String name = response.getBody("name");
		List<String> participantUuids = response.getBody("participants");
		User owner = UserRequest.get(response.getBody("owner")).join().orElseThrow();
		List<User> participants = participantUuids.stream().map(UserRequest::get)
			.map(CompletableFuture::join).map(Optional::orElseThrow).toList();

		List<ChatMessage> deserialized = new ArrayList<>();
		Persistence persistence = Persistence.fromJson(response.getBody("persistence"));
		API.getInstance().get(Request.Route.CHANNEL.builder().path(id).path("messages")
				.build())
			.thenAccept(res -> {
				List<Map<String, Object>> messages = (List<Map<String, Object>>) res.getBody();

				for (Map<String, Object> o : messages) {
					deserialized.add(new ChatMessage(Long.toUnsignedString((long) o.get("id")), Long.toUnsignedString((long) o.get("channel_id")),
						UserRequest.get((String) o.get("sender")).join().orElseThrow(), (String) o.get("sender_name"),
						(String) o.get("content"), Instant.parse((CharSequence) o.get("timestamp"))));
				}
			}).join();
		if (participants.size() == 1) {
			return new Channel.DM(id, name, persistence, participants, owner, deserialized);
		}
		return new Channel.Group(id, name, persistence, participants, owner, deserialized);
	}

	@SuppressWarnings("unchecked")
	public static CompletableFuture<List<Channel>> getChannelList() {
		return API.getInstance().get(Request.Route.CHANNELS.create())
			.thenApply(response -> {
				List<Long> ids = (List<Long>) response.getBody();
				return ids.stream().map(Long::toUnsignedString)
					.map(ChannelRequest::getById).map(CompletableFuture::join).toList();
			});
	}

	public static CompletableFuture<?> createChannel(String name, Persistence persistence, String... users) {
		if (name.isEmpty()) {
			return CompletableFuture.failedFuture(new Throwable("name is empty"));
		}
		List<String> participants = new ArrayList<>();
		for (String username : users) {
			participants.add(UUIDHelper.getUuid(username));
		}
		return API.getInstance().post(Request.Route.CHANNEL.builder()
				.field("name", name).field("persistence", persistence.toJson())
				.field("participants", participants).build());
			//.thenApply(Response::getPlainBody).thenCompose(ChannelRequest::getById);
	}

	public static void updateChannel(String id, String name, Persistence persistence, String... additionalUsers) {
		API.getInstance().patch(Request.Route.CHANNEL.builder().path(id).field("name", name)
			.field("persistence", persistence.toJson()).field("participants", additionalUsers).build());
	}

	public static CompletableFuture<?> leaveOrDeleteChannel(Channel channel) {
		return API.getInstance().delete(Request.Route.CHANNEL.builder().path(channel.getId()).build());
	}

	public static CompletableFuture<Channel> getOrCreateDM(User user) {

		return getChannelList().thenApply(list -> list.stream().filter(c -> {
			if (!(c instanceof Channel.DM dm)) {
				return false;
			}
			if (dm.getOwner().equals(API.getInstance().getSelf()) && dm.getReceiver().equals(user)) {
				return true;
			}
			return c.getOwner().equals(user) && dm.getParticipants().getFirst().equals(API.getInstance().getSelf());
		}).findFirst()).thenApply(opt -> opt.orElseGet(() -> API.getInstance().post(Request.Route.CHANNEL.builder()
				.field("name", user.getUuid() + "-" + API.getInstance().getSelf().getUuid() + "_"+ UUID.randomUUID()).field("persistence", Persistence.of(Persistence.Type.CHANNEL, 0, 0).toJson())
				.field("participants", List.of(user.getUuid())).build())
			.thenApply(Response::getPlainBody).thenCompose(ChannelRequest::getById).join()));
	}

	public static void removeUserFromChannel(Channel channel, User user) {
		API.getInstance().post(Request.Route.CHANNEL.builder().path(channel.getId()).path("remove").query("user", user.getUuid()).build());
	}

	@SuppressWarnings("unchecked")
	public static CompletableFuture<List<ChannelInvite>> getChannelInvites() {
		return API.getInstance().get(Request.Route.CHANNELS_INVITES.create()).thenApply(res -> {
			List<Map<String, Object>> invites = (List<Map<String, Object>>) res.getBody();
			return invites.stream().map(m -> new ChannelInvite(Long.toUnsignedString((long) m.get("id")), (String) m.get("channel_name"), (String) m.get("from"))).toList();
		});
	}

	public static void acceptChannelInvite(ChannelInvite invite) {
		API.getInstance().post(Request.Route.CHANNELS_INVITES.builder().query("id", invite.channelId()).query("accept", true).build());
	}

	public static void ignoreChannelInvite(ChannelInvite invite) {
		API.getInstance().post(Request.Route.CHANNELS_INVITES.builder().query("id", invite.channelId()).query("accept", false).build());
	}
}
