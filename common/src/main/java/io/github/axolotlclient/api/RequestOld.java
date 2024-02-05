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
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.JsonObject;
import lombok.*;

/**
 * Defines a generic request that can be sent to the backend API.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(doNotUseGetters = true)
public class RequestOld {

	private static final byte[] PACKET_MAGIC = Constants.PACKET_MAGIC.getBytes(StandardCharsets.UTF_8);

	@EqualsAndHashCode.Include
	private final Type type;
	@EqualsAndHashCode.Include
	@Getter(AccessLevel.PACKAGE)
	private final int id;
	private final Data data;

	public RequestOld(Type type, Data data) {
		this.type = type;
		id = generateId();
		this.data = data;
	}

	public RequestOld(Type type) {
		this(type, new Data());
	}

	public RequestOld(Type type, String... data) {
		this(type, new Data(data));
	}

	public RequestOld(Type type, byte... data) {
		this(type, new Data(data));
	}

	private int generateId() {
		int id = 0;
		while (id == 0) {
			id = ThreadLocalRandom.current().nextInt();
		}
		return id;
	}

	public String getData() {
		JsonObject object = new JsonObject();
		object.addProperty("type", type.getType());
		object.addProperty("id", id);
		object.ad
		return Unpooled.buffer()
			.writeBytes(PACKET_MAGIC)
			.writeByte(type.getType())
			.writeByte(Constants.PROTOCOL_VERSION)
			.writeInt(id)
			.writeBytes(data.getData());
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

	public static class Data {
		private final JsonObject data = new JsonObject();

		public Data() {
		}

		public Data(String... keyValuePairs) {
			add(keyValuePairs);
		}

		public Data(String name, byte b) {
			add(name, b);
		}

		public Data(String name, int i){
			add(name, i);
		}

		public Data add(String name, int i) {
			data.addProperty(name, i);
			return this;
		}

		public Data add(String name, String e) {
			data.addProperty(name, e);
			return this;
		}

		public Data add(String... keyValuePairs) {
			for (int i=0; i< keyValuePairs.length-1;i+=2) {
				add(keyValuePairs[i], keyValuePairs[i+1]);
			}
			return this;
		}

		public Data add(String name, long l) {
			data.addProperty(name, l);
			return this;
		}

		public Data add(String name, byte b) {
			data.addProperty(name, b);
			return this;
		}

		JsonObject getData() {
			return data;
		}

		@Override
		public String toString() {
			return data.toString();
		}
	}
}
