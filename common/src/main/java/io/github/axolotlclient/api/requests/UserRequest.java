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

import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.Relation;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.TimestampParser;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.ThreadExecuter;

@SuppressWarnings("UnstableApiUsage")
public class UserRequest {

	private static final Cache<String, Optional<User>> userCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
		.maximumSize(400).build();
	private static final WeakHashMap<String, Boolean> onlineCache = new WeakHashMap<>();

	public static boolean getOnline(String uuid) {

		if (uuid == null) {
			return false;
		}

		String sanitized = API.getInstance().sanitizeUUID(uuid);

		if (sanitized.equals(API.getInstance().getSelf().getUuid())) {
			return true;
		}

		synchronized (onlineCache) {
			if (!onlineCache.containsKey(sanitized)) {
				ThreadExecuter.scheduleTask(() -> get(sanitized).thenApply(u -> u.isPresent() && u.get().getStatus().isOnline()).thenAccept(b -> onlineCache.put(sanitized, b)));
				return false;
			}
			return onlineCache.get(sanitized);
		}
	}

	public static CompletableFuture<Optional<User>> get(String dUuid) {
		final String uuid = API.getInstance().sanitizeUUID(dUuid);
		Optional<User> cached = userCache.getIfPresent(uuid);
		if (cached != null && cached.isPresent()) {
			return CompletableFuture.completedFuture(cached);
		}

		synchronized (userCache) {
			return API.getInstance().get(Request.Route.USER.builder().path(uuid).build()).thenApply(response -> {
				if (response.isError()) {
					return null;
				}
				return new User(
					response.getBody("uuid"),
					response.getBody("username"),
					Relation.get(response.getBodyOrElse("relation", "none")),
					response.getBody("registered", TimestampParser::parse),
					new Status(response.getBody("status.type").equals("online"),
						response.getBody("status.last_online", TimestampParser::parse),
						response.ifBodyHas("status.activity", () -> {
							String desc = response.getBody("status.activity.description");
							String description;
							if (desc.contains("{")) {
								try {
									var json = GsonHelper.fromJson(desc);
									description = json.has("value") ? json.get("value").getAsString() : "";
								} catch (Throwable t) {
									description = desc;
								}
							} else {
								description = desc;
							}
							return new Status.Activity(response.getBody("status.activity.title"),
								description, desc,
								response.getBody("status.activity.started", TimestampParser::parse));
						})
					),
					response.getBody("previous_usernames", (List<String> list) ->
						list.stream().map(s -> new User.OldUsername(s, true)).collect(Collectors.toList())));
			}).thenApply(u -> {
				Optional<User> opt = Optional.ofNullable(u);
				userCache.put(uuid, opt);
				return opt;
			});
		}
	}

	public static CompletableFuture<Boolean> delete() {
		return API.getInstance().delete(Request.Route.ACCOUNT.create()).thenApply(res -> !res.isError());
	}
}
