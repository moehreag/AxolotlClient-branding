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

package io.github.axolotlclient.api.types;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class ChatMessage {

	private final User sender;
	private final String content;
	private Type type;
	private final long timestamp;

	@AllArgsConstructor
	public enum Type {
		NORMAL(0x00),
		REPLY(0x01),
		JOIN_LEAVE(0x02),
		PARTY_INVITE(0x03);
		@Getter
		private final int value;

		private static final Map<Integer, Type> CODES = Arrays.stream(values()).collect(Collectors.toMap(k -> k.value, k -> k));

		public static Type fromCode(int code) {
			return CODES.get(code);
		}
	}
}
