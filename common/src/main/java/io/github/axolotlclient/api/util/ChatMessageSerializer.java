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

package io.github.axolotlclient.api.util;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.types.User;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ChatMessageSerializer implements Serializer<ChatMessage> {
	@Override
	public ByteBuf serialize(ChatMessage message) {
		ByteBuf buf = Unpooled.buffer();
		buf.writeCharSequence(message.getSender().getUuid(), StandardCharsets.UTF_8);
		buf.writeLong(message.getTimestamp());
		buf.writeByte(message.getType().getValue());
		buf.writeInt(message.getContent().length());
		buf.writeCharSequence(message.getContent(), StandardCharsets.UTF_8);
		return buf;
	}

	@Override
	public ChatMessage deserialize(ByteBuf buf) {
		AtomicReference<User> u = new AtomicReference<>();
		io.github.axolotlclient.api.requests.User.get(BufferUtil.getString(buf, 0x00, 16)).whenCompleteAsync((us, t) -> u.set(us));

		return new ChatMessage(u.get(), BufferUtil.getString(buf, 0x1D, buf.getInt(0x19)),
			ChatMessage.Type.fromCode(buf.getByte(0x18)), buf.getLong(0x10));
	}

	public static class ChatMessageTypeSerializer implements Serializer<ChatMessage.Type> {

		@Override
		public ByteBuf serialize(ChatMessage.Type type) {
			return BufferUtil.wrap(type.getValue());
		}

		@Override
		public ChatMessage.Type deserialize(ByteBuf buf) {
			return ChatMessage.Type.fromCode(buf.readInt());
		}
	}
}
