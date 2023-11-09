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

package io.github.axolotlclient.api.handlers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.api.util.RequestHandler;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class ChatHandler implements RequestHandler {

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
	public boolean isApplicable(int packetType) {
		return packetType == Request.Type.SEND_MESSAGE.getType();
	}

	@Override
	public void handle(ByteBuf buf, APIError error) {

		ChatMessage message = BufferUtil.unwrap(BufferUtil.removeMetadata(buf), ChatMessage.class);

		if (enableNotifications.showNotification(message)) {
			API.getInstance().getNotificationProvider().addStatus(API.getInstance().getTranslationProvider().translate("api.chat.newMessageFrom", message.getSender().getName()), message.getContent());
		}
		messageConsumer.accept(message);
	}

	public void sendMessage(Channel channel, String message) {
		API.getInstance().send(new Request(Request.Type.SEND_MESSAGE,
			new Request.Data(channel.getId()).add(
				Instant.now().getEpochSecond()).add(message.length()).add(message)));
		messageConsumer.accept(new ChatMessage(API.getInstance().getSelf(), message, Instant.now().getEpochSecond()));
	}

	public void getMessagesBefore(Channel channel, long getBefore) {
		API.getInstance().send(new Request(Request.Type.GET_MESSAGES,
			new Request.Data(channel.getId()).add(25).add(getBefore).add(0x00))).whenCompleteAsync(this::handleMessages);
	}

	private void handleMessages(ByteBuf object, Throwable t) {
		if (t == null) {
			List<ChatMessage> list = new ArrayList<>();

			int i = 0x16;
			while (i < object.getInt(0x0E)) {
				int length = 0x1d + object.getInt(i + 0x19);
				list.add(BufferUtil.unwrap(object.slice(i, length), ChatMessage.class));
				i += length;
			}
			messagesConsumer.accept(list);

		} else {
			APIError.display(t);
		}
	}

	public void getMessagesAfter(Channel channel, long getAfter) {
		API.getInstance().send(new Request(Request.Type.GET_MESSAGES,
			new Request.Data(channel.getId()).add(25).add(getAfter).add(0x01))).whenCompleteAsync(this::handleMessages);
	}

	public interface NotificationsEnabler {
		boolean showNotification(ChatMessage message);
	}

	public void reportMessage(ChatMessage message){
		API.getInstance().send(new Request(Request.Type.REPORT_MESSAGE,
			new Request.Data(message.getSender().getUuid()).add(message.getTimestamp())
				.add(message.getContent().length()).add(message.getContent())));
	}

	public void reportUser(User user){
		API.getInstance().send(new Request(Request.Type.REPORT_USER, user.getUuid()));
	}
}
