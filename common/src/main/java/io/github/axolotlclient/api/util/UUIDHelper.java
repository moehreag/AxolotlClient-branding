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

package io.github.axolotlclient.api.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;

public class UUIDHelper {

	private static final WeakHashMap<String, String> nameCache = new WeakHashMap<>();
	private static final WeakHashMap<String, String> uuidCache = new WeakHashMap<>();
	private static final HttpClient client = NetworkUtil.createHttpClient("UUIDHelper");

	public static String getUsername(String uuid) {
		return getUsername0(uuid).orElse(uuid);
	}

	private static Optional<String> getUsername0(String uuid) {
		return Optional.ofNullable(nameCache.computeIfAbsent(uuid, s ->
			client.sendAsync(HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid)).GET().build(), HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(GsonHelper::fromJson)
				.thenApply(o -> {
					if (o.has("name")) {
						return o.get("name").getAsString();
					}
					if (API.getInstance().getApiOptions().detailedLogging.get()) {
						API.getInstance().getLogger().warn("Conversion uuid -> username failed: {}", o);
					}
					return "";
				}).join())).map(s -> s.isEmpty() ? null : s);
	}

	public static String getUuid(String username) {
		return getUuid0(username).orElse(username);
	}

	private static Optional<String> getUuid0(String username) {
		return Optional.of(uuidCache.computeIfAbsent(username, s ->
			client.sendAsync(HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username)).GET().build(), HttpResponse.BodyHandlers.ofString())
				.thenApply(HttpResponse::body)
				.thenApply(GsonHelper::fromJson)
				.thenApply(o -> {
					if (o.has("id")) {
						return o.get("id").getAsString();
					}
					if (API.getInstance().getApiOptions().detailedLogging.get()) {
						API.getInstance().getLogger().warn("Conversion username -> uuid failed: {}", o);
					}
					return "";
				}).join())).map(s -> s.isEmpty() ? null : s);
	}

	public static String ensureUuid(String uuidOrUsername) {
		return ensureUuidOpt(uuidOrUsername).orElse(uuidOrUsername);
	}

	public static Optional<String> ensureUuidOpt(String uuidOrUsername) {
		Optional<String> uuid;
		try {
			uuid = Optional.of(API.getInstance().sanitizeUUID(fromUndashed(uuidOrUsername).toString()));
		} catch (IllegalArgumentException e) {
			uuid = getUuid0(uuidOrUsername.trim());
		}
		return uuid;
	}

	public static UUID fromUndashed(String uuid) {
		return UUID.fromString(uuid.trim().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}

	public static String toUndashed(UUID uuid) {
		return API.getInstance().sanitizeUUID(uuid.toString());
	}
}
