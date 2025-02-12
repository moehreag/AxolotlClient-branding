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

package io.github.axolotlclient.api.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.types.Relation;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.BiContainer;
import io.github.axolotlclient.api.util.UUIDHelper;
import lombok.Getter;

public class FriendRequest {

	@Getter
	private final static FriendRequest Instance = new FriendRequest();
	private final API api;

	protected FriendRequest() {
		this.api = API.getInstance();
	}

	public CompletableFuture<Response> setRelation(String uuid, Relation relation) {
		return api.post(Request.Route.USER.builder().path(uuid).query("relation", relation.getId()).build());
	}

	public CompletableFuture<?> addFriend(String uuid) {
		return setRelation(uuid, Relation.REQUEST).whenCompleteAsync((response, t) -> {
			if (!response.isError()) {
				api.getNotificationProvider()
					.addStatus("api.success.request_sent", "api.success.request_sent.desc", UUIDHelper.getUsername(uuid));
			} else if (response.getError().httpCode() == 404) {
				api.getNotificationProvider().addStatus("api.failure.request_sent", "api.failure.request_sent.not_found", UUIDHelper.getUsername(uuid));
			} else if (response.getError().httpCode() == 403) {
				api.getNotificationProvider().addStatus("api.failure.request_sent", "api.failure.request_sent.forbidden", UUIDHelper.getUsername(uuid));
			}
		});
	}

	public CompletableFuture<?> removeFriend(User user) {
		return setRelation(api.sanitizeUUID(user.getUuid()), Relation.NONE).whenCompleteAsync((response, t) -> {
			if (!response.isError()) {
				api.getNotificationProvider().addStatus("api.success.removeFriend", "api.success.removeFriend.desc", user.getName());
				user.setRelation(Relation.NONE);
			}
		});
	}

	public CompletableFuture<?> blockUser(User user) {
		return setRelation(user.getUuid(), Relation.BLOCKED).whenCompleteAsync((response, t) -> {
			if (!response.isError()) {
				api.getNotificationProvider().addStatus("api.success.blockUser", "api.success.blockUser.desc", user.getName());
				user.setRelation(Relation.BLOCKED);
			}
		});
	}

	public CompletableFuture<?> unblockUser(User user) {
		return setRelation(user.getUuid(), Relation.NONE).whenCompleteAsync((response, t) -> {
			if (!response.isError()) {
				api.getNotificationProvider().addStatus("api.success.unblockUser", "api.success.unblockUser.desc", user.getName());
				user.setRelation(Relation.NONE);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<List<String>> getFriendUuids() {
		return api.get(Request.Route.ACCOUNT_RELATIONS_FRIENDS.builder().build())
			.thenApply(r -> {
				if (!r.isError()) {
					return (List<String>) r.getBody();
				}
				return new ArrayList<>();
			});
	}

	public CompletableFuture<List<User>> getFriends() {
		return getFriendUuids()
			.thenApply(r -> r.stream().map(UserRequest::get).map(CompletableFuture::join).map(Optional::orElseThrow).toList());
	}

	public CompletableFuture<BiContainer<List<User>, List<User>>> getFriendRequests() {
		return api.get(Request.Route.ACCOUNT_RELATIONS_REQUESTS.create())
			.thenApply(res -> {
				List<String> in = res.getBody("in");
				List<String> out = res.getBody("out");
				List<User> incoming = in.stream().map(UserRequest::get).map(CompletableFuture::join).map(Optional::orElseThrow).toList();
				List<User> outgoing = out.stream().map(UserRequest::get).map(CompletableFuture::join).map(Optional::orElseThrow).toList();
				return BiContainer.of(incoming, outgoing);
			});
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<List<User>> getBlocked() {
		return api.get(Request.Route.ACCOUNT_RELATIONS_BLOCKED.create())
			.thenApply(res -> {
				List<String> uuids = (List<String>) res.getBody();
				return uuids.stream().map(UserRequest::get).map(CompletableFuture::join).map(Optional::orElseThrow).toList();
			});
	}

	public CompletableFuture<?> acceptFriendRequest(User from) {
		return setRelation(from.getUuid(), Relation.FRIEND)
			.thenAccept(res -> {
				if (!res.isError()) {
					api.getNotificationProvider().addStatus("api.success.acceptFriend", "api.success.acceptFriend.desc", from.getName());
					from.setRelation(Relation.FRIEND);
				}
			});
	}

	public CompletableFuture<?> denyFriendRequest(User from) {
		return setRelation(from.getUuid(), Relation.NONE).thenAccept(res -> {
			if (!res.isError()) {
				api.getNotificationProvider().addStatus("api.success.denyFriend", "api.success.denyFriend.desc", from.getName());
				from.setRelation(Relation.NONE);
			}
		});
	}

	public CompletableFuture<?> cancelFriendRequest(User to) {
		return setRelation(to.getUuid(), Relation.NONE).thenAccept(res -> {
			if (!res.isError()) {
				api.getNotificationProvider().addStatus("api.friends", "api.friends.request.cancelled", to.getName());
				to.setRelation(Relation.NONE);
			}
		});
	}
}
