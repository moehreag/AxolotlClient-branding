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

package io.github.axolotlclient.api.types;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.api.API;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Channel {

	private final String id;
	protected String name;
	protected Persistence persistence;
	private List<User> participants;
	private User owner;
	private final List<ChatMessage> messages;

	public List<User> getAllUsers() {
		List<User> list = new ArrayList<>(participants);
		list.add(0, owner);
		return list;
	}

	public String getRealName() {
		return name;
	}

	public abstract boolean isDM();

	public static class Group extends Channel {

		public Group(String id, String name, Persistence persistence, List<User> participants, User owner, List<ChatMessage> messages) {
			super(id, name, persistence, participants, owner, messages);
		}

		public boolean isDM() {
			return false;
		}
	}

	@Getter
	public static class DM extends Channel {

		private final User receiver;

		public DM(String id, String name, Persistence persistence, List<User> participants, User owner, List<ChatMessage> messages) {
			super(id, name, persistence, participants, owner, messages);
			receiver = API.getInstance().getSelf().equals(owner) ? participants.get(0) : owner;
		}

		public boolean isDM() {
			return true;
		}

		@Override
		public String getName() {
			return receiver.getName();
		}
	}
}
