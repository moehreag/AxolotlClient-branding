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

package io.github.axolotlclient.api.handlers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.util.SocketMessageHandler;
import lombok.Getter;
import lombok.Setter;

public class ChatHandler implements SocketMessageHandler {

	public static final Consumer<ChatMessage> DEFAULT_MESSAGE_CONSUMER = message -> {
	};
	public static final Consumer<List<ChatMessage>> DEFAULT_MESSAGES_CONSUMER = messages -> {
	};
	public static final NotificationsEnabler DEFAULT = message -> true;
	@Getter
	private static final ChatHandler Instance = new ChatHandler();
	@Setter
	private Consumer<ChatMessage> messageConsumer = DEFAULT_MESSAGE_CONSUMER;
	@Setter
	private Consumer<List<ChatMessage>> messagesConsumer = DEFAULT_MESSAGES_CONSUMER;
	@Setter
	private NotificationsEnabler enableNotifications = DEFAULT;

	@Override
	public boolean isApplicable(String target) {
		return "chat_message".equals(target);
	}

	@Override
	public void handle(Response response) {
		Instant time = Instant.now();
		String channelId = response.getBody("channel", d -> Long.toUnsignedString((long) d));
		String id = response.getBody("id", d -> Long.toUnsignedString((long) d));
		String sender = response.getBody("sender");
		String senderName = response.getBody("sender_name");
		String content = response.getBody("content");
		ChatMessage message = new ChatMessage(id, channelId, UserRequest.get(sender).join(), senderName, content, time);
		if (enableNotifications.showNotification(message)) {
			API.getInstance().getNotificationProvider().addStatus(API.getInstance().getTranslationProvider().translate("api.chat.newMessageFrom", message.sender().getName()), message.content());
		}
		messageConsumer.accept(message);
	}

	public void sendMessage(Channel channel, String message) {
		String displayName = API.getInstance().getSelf().getDisplayName(message);

		API.getInstance().post(Request.Route.CHANNEL.builder().path(channel.getId()).field("content", message)
			.field("display_name", displayName).build())
			.whenComplete((res, th) ->
				messageConsumer.accept(new ChatMessage(res.getPlainBody(), channel.getId(), API.getInstance().getSelf(), displayName, message, Instant.now())));
	}

	@SuppressWarnings("unchecked")
	public void getMessagesBefore(Channel channel, long getBefore) {
		API.getInstance().get(Request.Route.CHANNEL.builder().path(channel.getId()).path("messages")
				.query("before", Instant.ofEpochSecond(getBefore).toString()).build())
			.thenAccept(res -> {
				List<Map<String, Object>> messages = (List<Map<String, Object>>) res.getBody();

				List<ChatMessage> deserialized = new ArrayList<>();

				for (Map<String, Object> o : messages) {
					deserialized.add(new ChatMessage(Long.toUnsignedString((long) o.get("id")),
						Long.toUnsignedString((long) o.get("channel_id")),
						UserRequest.get((String) o.get("sender")).join(), (String) o.get("sender_name"),
						(String) o.get("content"), Instant.parse((CharSequence) o.get("timestamp"))));
				}

				messagesConsumer.accept(deserialized);
			});
	}

	public void reportMessage(ChatMessage message) {
		API.getInstance().post(Request.Route.REPORT.builder().path(message.id()).build());
	}

	public interface NotificationsEnabler {
		boolean showNotification(ChatMessage message);
	}
}
