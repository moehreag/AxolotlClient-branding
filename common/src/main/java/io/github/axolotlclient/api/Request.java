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
import java.util.concurrent.ThreadLocalRandom;

import io.github.axolotlclient.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.*;

/**
 * Defines a generic request that can be sent to the backend API.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(doNotUseGetters = true)
public class Request {

	private static final byte[] PACKET_MAGIC = "AXO".getBytes(StandardCharsets.UTF_8);
	private static final int PROTOCOL_VERSION = 0x01;

	@EqualsAndHashCode.Include
	private final Type type;
	@EqualsAndHashCode.Include
	@Getter(AccessLevel.PACKAGE)
	private final int id;
	private final Data data;

	public Request(Type type, Data data) {
		this.type = type;
		id = generateId();
		this.data = data;
	}

	public Request(Type type) {
		this(type, new Data());
	}

	public Request(Type type, String... data) {
		this(type, new Data(data));
	}

	public Request(Type type, byte... data) {
		this(type, new Data(data));
	}

	private int generateId() {
		int id = 0;
		while (id == 0) {
			id = ThreadLocalRandom.current().nextInt();
		}
		return id;
	}

	public ByteBuf getData() {
		return Unpooled.buffer()
			.setBytes(0x00, PACKET_MAGIC)
			.setByte(0x03, type.getType())
			.setByte(0x04, PROTOCOL_VERSION)
			.setInt(0x05, id)
			.setBytes(0x09, data.getData());
	}

	public static class Data {
		private final ByteBuf buf = Unpooled.buffer();

		public Data(){}

		public Data(String... data) {
			for (String s : data) {
				add(s);
			}
		}

		public Data(byte b) {
			add(b);
		}

		public Data(byte[] data) {
			add(data);
		}

		public Data add(int i){
			buf.writeInt(i);
			return this;
		}

		public Data add(String e) {
			buf.writeCharSequence(e, StandardCharsets.UTF_8);
			return this;
		}

		public Data add(String... a) {
			for (String s : a) {
				add(s);
			}
			return this;
		}

		public Data add(long l){
			buf.writeLong(l);
			return this;
		}

		public Data add(byte b) {
			buf.writeByte(b);
			return this;
		}

		public Data add(byte[] data) {
			buf.writeBytes(data);
			return this;
		}

		public Data add(ByteBuf buf){
			this.buf.writeBytes(buf);
			return this;
		}

		ByteBuf getData() {
			return buf.setIndex(0, buf.capacity());
		}

		@Override
		public String toString() {
			return Arrays.toString(BufferUtil.toArray(buf));
		}
	}

	/**
	 * Defines human-readable names for all request types.
	 */
	@RequiredArgsConstructor
	@Getter
	public enum Type {
		HANDSHAKE(0x01),
		GLOBAL_DATA(0x02),
		FRIENDS_LIST(0x03),
		GET_FRIEND(0x04),
		USER(0x05),
		CREATE_FRIEND_REQUEST(0x06),
		FRIEND_REQUEST_REACTION(0x07),
		GET_FRIEND_REQUESTS(0x08),
		REMOVE_FRIEND(0x09),
		INCOMING_FRIEND_REQUEST(0x0A),
		STATUS_UPDATE(0x0B),
		CREATE_CHANNEL(0x0C),
		GET_OR_CREATE_CHANNEL(0x0D),
		GET_MESSAGES(0x0E),
		GET_CHANNEL_LIST(0x0F),
		SEND_MESSAGE(0x10),
		GET_CHANNEL_BY_ID(0x11),
		GET_PUBLIC_KEY(0x12),
		GET_HYPIXEL_API_DATA(0x13),
		GET_BLOCKED(0x14),
		BLOCK_USER(0x15),
		UNBLOCK_USER(0x16),
		UPLOAD_SCREENSHOT(0x17),
		DOWNLOAD_SCREENSHOT(0x18),
		REPORT_MESSAGE(0x19),
		REPORT_USER(0x1A),
		ERROR(0xFF);

		private final int type;
	}
}
