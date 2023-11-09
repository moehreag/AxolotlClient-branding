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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.Lists;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Keyword;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.BiContainer;
import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.api.util.RequestHandler;
import io.github.axolotlclient.api.util.UUIDHelper;
import lombok.Getter;

public class FriendHandler implements RequestHandler {

	@Getter
	private final static FriendHandler Instance = new FriendHandler();
	private final API api;

	protected FriendHandler() {
		this.api = API.getInstance();
	}

	public void addFriend(String uuid) {
		api.send(new Request(Request.Type.CREATE_FRIEND_REQUEST, uuid)).whenCompleteAsync((object, t) -> {
			if (t == null) {
				api.getNotificationProvider()
					.addStatus("api.success.requestSent", "api.success.requestSent.desc", UUIDHelper.getUsername(uuid));
			} else {
				APIError.display(t);
			}
		});
	}

	public void removeFriend(User user) {
		api.send(new Request(Request.Type.REMOVE_FRIEND, API.getInstance().sanitizeUUID(user.getUuid()))).whenCompleteAsync((object, t) -> {
			if (t == null) {
				APIError.display(object);
			} else {
				api.getNotificationProvider().addStatus("api.success.removeFriend", "api.success.removeFriend.desc", user.getName());
			}
		});
	}

	public void blockUser(String uuid) {
		api.send(new Request(Request.Type.BLOCK_USER, uuid)).whenCompleteAsync((buf, throwable) -> {
			APIError.displayOrElse(throwable, buf, b -> {
				if(b.getBoolean(0x09)) {
					api.getNotificationProvider().addStatus("api.success.blockUser", "api.success.blockUser.desc", UUIDHelper.getUsername(uuid));
				}
			});
		});
	}

	public void unblockUser(String uuid) {
		api.send(new Request(Request.Type.UNBLOCK_USER, uuid)).whenCompleteAsync((buf, throwable) -> {
			APIError.displayOrElse(throwable, buf, b -> {
				if(b.getBoolean(0x09)) {
					api.getNotificationProvider().addStatus("api.success.unblockUser", "api.success.unblockUser.desc", UUIDHelper.getUsername(uuid));
				}
			});
		});
	}

	public CompletableFuture<List<User>> getFriends() {
		return api.send(new Request(Request.Type.FRIENDS_LIST)).handleAsync((object, t) -> {
			if (t != null) {
				APIError.display(object);
				return Collections.emptyList();
			} else {
				List<User> list = new ArrayList<>();
				for (int i = 0x0E; i <= object.getInt(0x0A); i += 16) {
					getFriendInfo(getString(object, i, 16)).whenCompleteAsync((u, th) -> list.add(u));
				}
				return list;
			}
		});
	}

	public CompletableFuture<User> getFriendInfo(String uuid) {
		return api.send(new Request(Request.Type.GET_FRIEND, uuid)).thenApply(buf -> {

			Instant startTime = Instant.ofEpochSecond(buf.getLong(0x09));

			return new User(uuid,
				new Status(buf.getBoolean(0x0D),
					Keyword.get(getString(buf, 0x0E, 64).trim()),
					Keyword.get(getString(buf, 0x4E, 64).trim()),
					Keyword.get(getString(buf, 0x8E, 32).trim()), startTime));
		});
	}

	public CompletableFuture<BiContainer<List<User>, List<User>>> getFriendRequests() {
		return api.send(new Request(Request.Type.GET_FRIEND_REQUESTS)).handleAsync((object, th) -> {
			if (th != null) {
				APIError.display(th);
			} else {
				List<User> in = new ArrayList<>();
				List<User> out = new ArrayList<>();
				int i = 0x0E;
				while (i < object.getInt(0x0A)) {
					getFriendInfo(getString(object, i, 16)).whenCompleteAsync((u, t) -> in.add(u));
					i += 16;
				}
				int offset = i;
				i += 4;
				while (i < object.getInt(offset)) {
					getFriendInfo(getString(object, i, 16)).whenCompleteAsync((u, t) -> out.add(u));
					i += 16;
				}

				return BiContainer.of(in, out);
			}
			return BiContainer.of(Collections.emptyList(), Collections.emptyList());
		});
	}

	public CompletableFuture<List<User>> getBlocked() {
		return api.send(new Request(Request.Type.GET_BLOCKED)).handleAsync((buf, th) -> {
			int count = buf.getInt(0x09);

			List<User> users = new ArrayList<>();
			for(int i = 0;i<count;i++){
				users.add(new User(BufferUtil.getString(buf, 0x0C + (i * 16), 16), Status.UNKNOWN));
			}
			return users;
		});
	}

	public boolean isBlocked(String uuid) {
		return getBlocked().getNow(Collections.emptyList()).stream().map(User::getUuid).anyMatch(uuid::equals);
	}

	public void acceptFriendRequest(User from) {
		api.send(new Request(Request.Type.FRIEND_REQUEST_REACTION, new Request.Data(from.getUuid()).add((byte) 1)))
			.whenCompleteAsync((object, t) -> {
			if (t != null) {
				APIError.display(t);
			} else {
				api.getNotificationProvider().addStatus("api.success.acceptFriend", "api.success.acceptFriend.desc", from.getName());
			}
		});
	}

	public void denyFriendRequest(User from) {
		api.send(new Request(Request.Type.FRIEND_REQUEST_REACTION, new Request.Data(from.getUuid()).add((byte) 0)))
			.whenCompleteAsync((object, t) -> {
			if (t != null) {
				APIError.display(t);
			} else {
				api.getNotificationProvider().addStatus("api.success.denyFriend", "api.success.denyFriend.desc", from.getName());
			}
		});
	}
}
