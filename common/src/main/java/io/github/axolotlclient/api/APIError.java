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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class APIError extends Exception {

	@Getter
	private final ErrorCode code;
	@Getter
	private final ByteBuf buf;

	public APIError(ByteBuf buf){
		super(fromCode(buf.getInt(0x09)));
		code = ErrorCode.fromCode(buf.getInt(0x09));
		this.buf = buf;

	}

	public void display(){
		API.getInstance().getNotificationProvider().addStatus("api.error.requestGeneric", getMessage());
	}

	public static void display(Throwable t){
		if(t instanceof APIError){
			((APIError) t).display();
		} else {
			API.getInstance().getLogger().debug("APIError: " + t);
			API.getInstance().getNotificationProvider().addStatus("api.error.requestGeneric", t.getMessage());
		}
	}

	public static void displayOrElse(Throwable t, ByteBuf buf, Consumer<ByteBuf> action){
		if(t != null){
			display(t);
		} else {
			action.accept(buf);
		}
	}

	public static void display(ByteBuf object) {
		API.getInstance().getLogger().debug("APIError: " + object);
		API.getInstance().getNotificationProvider().addStatus("api.error.requestGeneric", fromResponse(object));
	}

	public static String fromResponse(ByteBuf object) {
		return fromCode(object.getInt(0x09));
	}

	public static String fromCode(int errorCode) {
		try {
			return API.getInstance().getTranslationProvider().translate(ErrorCode.fromCode(errorCode).getTranslationKey());
		} catch (IllegalArgumentException e) {
			API.getInstance().getLogger().error("Error code " + errorCode + " not found! Report this IMMEDIATELY!");
			return String.valueOf(errorCode);
		}
	}

	@AllArgsConstructor
	private enum ErrorCode {
		USER_NOT_FOUND("api.error.userNotFound", 0x01),
		FRIEND_REQUEST_NOT_FOUND("api.error.friendRequestNotFound", 0x02),
		USER_BLOCKED("api.error.userBlocked", 0x03),
		USER_ALREADY_BLOCKED("api.error.userAlreadyBlocked", 0x04),
		USER_ALREADY_FRIENDS("api.error.userAlreadyFriends", 0x05),
		MALFORMED_PACKET("api.error.packetMalformed", 0x06);
		@Getter
		private final String translationKey;
		private final int code;

		private static final Map<Integer, ErrorCode> CODES = Arrays.stream(values()).collect(Collectors.toMap(k -> k.code, k -> k));

		public static ErrorCode fromCode(int code) {
			return CODES.get(code);
		}
	}
}
