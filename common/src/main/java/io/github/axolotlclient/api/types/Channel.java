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

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.Serializer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class Channel {

	@Serializer.Length(5)
	private final String id;
	@Serializer.Length(64)
	protected final String name;
	private final User[] users;
	private final ChatMessage[] messages;

	public abstract boolean isDM();

	public static class Group extends Channel {

		public Group(String id, User[] users, String name, ChatMessage[] messages) {
			super(id, name, users, messages);
		}

		public boolean isDM() {
			return false;
		}
	}

	public static class DM extends Channel {

		@Getter
		private final User receiver;

		public DM(String id, User[] users, ChatMessage[] messages) {
			super(id, Arrays.stream(users).filter(user -> !user.getUuid()
				.equals(API.getInstance().getUuid())).map(User::getName).findFirst().orElse(""), users, messages);
			receiver = Arrays.stream(users).filter(user -> !user.getUuid()
				.equals(API.getInstance().getUuid())).findFirst().orElseThrow(IllegalStateException::new);
		}

		public boolean isDM() {
			return true;
		}
	}
}
