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

package io.github.axolotlclient.api.requests;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.TimestampParser;

@SuppressWarnings("UnstableApiUsage")
public class UserRequest {

	private static final Cache<String, User> userCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
		.maximumSize(400).build();
	private static final Cache<String, Boolean> onlineCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(200)
		.build();

	public static boolean getOnline(String uuid) {

		if (uuid == null) {
			return false;
		}

		uuid = API.getInstance().sanitizeUUID(uuid);

		if (uuid.equals(API.getInstance().getUuid())) {
			return true;
		}

		final String fUuid = uuid;
		try {
			return onlineCache.get(uuid, () -> get(fUuid).thenApply(User::getStatus).thenApply(Status::isOnline).handle((b, th) -> b != null && b).getNow(false));
		} catch (ExecutionException e) {
			return false;
		}
	}

	public static CompletableFuture<User> get(String dUuid) {
		final String uuid = API.getInstance().sanitizeUUID(dUuid);
		User cached = userCache.getIfPresent(uuid);
		if (cached != null) {
			return CompletableFuture.completedFuture(cached);
		}

		User user = API.getInstance().get(Request.Route.USER.builder().path(uuid).build()).thenApply(response -> {
			if (response.isError()) {
				return null;
			}
			return new User(
				response.getBody("uuid"),
				response.getBody("username"),
				response.getBodyOrElse("relation", "none"),
				response.getBody("registered", TimestampParser::parse),
				new Status(response.getBody("status.type").equals("online"),
					response.getBody("status.last_online", TimestampParser::parse),
					response.ifBodyHas("activity", () -> new Status.Activity(response.getBody("activity.title"),
						response.getBody("activity.description"),
						response.getBody("activity.started", TimestampParser::parse)))
				),
				response.getBody("previous_usernames", (List<String> list) ->
					list.stream().map(s -> new User.OldUsername(s, true)).collect(Collectors.toList())));
		}).join();

		if (user != null) {
			userCache.put(uuid, user);
			return CompletableFuture.completedFuture(user);
		}

		return CompletableFuture.failedFuture(new Throwable());
	}

	public static CompletableFuture<Boolean> delete() {
		return API.getInstance().delete(Request.Route.ACCOUNT.create()).thenApply(res -> !res.isError());
	}
}
