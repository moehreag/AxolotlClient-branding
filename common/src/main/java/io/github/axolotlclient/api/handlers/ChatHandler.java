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
import java.util.List;
import java.util.function.Consumer;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.types.User;
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
		return "chat".equals(target);
	}

	@Override
	public void handle(Response response) {

	}

	/*@Override
	public boolean isApplicable(int packetType) {
		return packetType == RequestOld.Type.SEND_MESSAGE.getType();
	}*/

	/*@Override
	public void handle(ByteBuf buf, APIError error) {

		ChatMessage message = unwrap(BufferUtil.removeMetadata(buf));

		if (enableNotifications.showNotification(message)) {
			API.getInstance().getNotificationProvider().addStatus(API.getInstance().getTranslationProvider().translate("api.chat.newMessageFrom", message.getSender().getName()), message.getContent());
		}
		messageConsumer.accept(message);
	}*/

	public void sendMessage(Channel channel, String message) {
		String displayName = API.getInstance().getSelf().getDisplayName(message);
		/*if (API.getInstance().getSelf().isSystem()){
			displayName += (" §r§o§7("+ API.getInstance() // gray + italic
				.getSelf().getSystem().getName()+
				"/"+ API.getInstance().getSelf().getName()+")§r");
		}
		API.getInstance().send(new RequestOld(RequestOld.Type.SEND_MESSAGE,
			new RequestOld.Data(channel.getId()).add(
				Instant.now().getEpochSecond()).add(displayName.length()).add(displayName)
				.add(message.length()).add(message)));*/
		messageConsumer.accept(new ChatMessage(API.getInstance().getSelf(), displayName, message, Instant.now().getEpochSecond()));
	}

	public void getMessagesBefore(Channel channel, long getBefore) {
		/*API.getInstance().send(new RequestOld(RequestOld.Type.GET_MESSAGES,
			new RequestOld.Data(channel.getId()).add(25).add(getBefore).add(0x00))).whenCompleteAsync(this::handleMessages);*/
	}

	/*private void handleMessages(ByteBuf object, Throwable t) {
		if (t == null) {
			List<ChatMessage> list = new ArrayList<>();

			int i = 0x16;
			while (i < object.getInt(0x0E)) {
				int length = 0x1d + object.getInt(i + 0x19);
				list.add(unwrap(object.slice(i, length)));
				i += length;
			}
			messagesConsumer.accept(list);

		} else {
			APIError.display(t);
		}
	}*/

	/*private ChatMessage unwrap(ByteBuf buf) {
		return BufferUtil.unwrap(buf, ChatMessage.class);
	}*/

	public void getMessagesAfter(Channel channel, long getAfter) {
		/*API.getInstance().send(new RequestOld(RequestOld.Type.GET_MESSAGES,
			new RequestOld.Data(channel.getId()).add(25).add(getAfter).add(0x01))).whenCompleteAsync(this::handleMessages);*/
	}

	public void reportMessage(ChatMessage message) {
		/*API.getInstance().send(new RequestOld(RequestOld.Type.REPORT_MESSAGE,
			new RequestOld.Data(message.getSender().getUuid()).add(message.getTimestamp())
				.add(message.getSenderDisplayName().length()).add(message.getSenderDisplayName())
				.add(message.getContent().length()).add(message.getContent())));*/
	}

	public void reportUser(User user) {
		//API.getInstance().send(new RequestOld(RequestOld.Type.REPORT_USER, user.getUuid()));
	}

	public interface NotificationsEnabler {
		boolean showNotification(ChatMessage message);
	}
}
