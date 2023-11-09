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

package io.github.axolotlclient.modules.auth;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Account {

	public static final String OFFLINE_TOKEN = "AxolotlClient/Offline";

	private final String uuid;
	private String name;

	private String authToken;
	private String refreshToken;
	private Instant expiration;

	public Account(String name, String uuid, String accessToken) {
		this.name = name;
		this.uuid = uuid.replace("-", "");
		this.authToken = accessToken;
		expiration = Instant.EPOCH;
		refreshToken = "";
	}

	public Account(JsonObject profile, String authToken, String refreshToken) {
		uuid = profile.get("id").getAsString();
		name = profile.get("name").getAsString();
		this.authToken = authToken;
		this.refreshToken = refreshToken;
		expiration = Instant.now().plus(1, ChronoUnit.DAYS);
	}

	private Account(String uuid, String name, String authToken, String refreshToken, long expiration) {
		this.uuid = uuid;
		this.name = name;
		this.authToken = authToken;
		this.refreshToken = refreshToken;
		this.expiration = Instant.ofEpochSecond(expiration);
	}

	public static Account deserialize(JsonObject object) {
		String uuid = object.get("uuid").getAsString();
		String name = object.get("name").getAsString();
		String authToken = object.get("authToken").getAsString();
		String refreshToken = object.get("refreshToken").getAsString();
		long expiration = object.get("expiration").getAsLong();
		return new Account(uuid, name, authToken, refreshToken, expiration);
	}

	public void refresh(MSAuth auth, Runnable runAfter) {
		new Thread(() -> {
			Map.Entry<String, String> tokens = auth.refreshToken(refreshToken, name);
			if (tokens.getKey() != null) {
				authToken = tokens.getKey();
				refreshToken = tokens.getValue();
				expiration = Instant.now().plus(1, ChronoUnit.DAYS);
				try {
					JsonObject object = auth.getMCProfile(authToken);
					name = object.get("name").getAsString();
				} catch (IOException ignored) {
				}
			}
			runAfter.run();
		}).start();
	}

	public boolean isOffline() {
		return authToken.equals(OFFLINE_TOKEN) || authToken.length() < 400;
	}

	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.add("uuid", new JsonPrimitive(uuid));
		object.add("name", new JsonPrimitive(name));
		object.add("authToken", new JsonPrimitive(authToken));
		object.add("refreshToken", new JsonPrimitive(refreshToken == null ? "" : refreshToken));
		object.add("expiration", new JsonPrimitive(expiration == null ? 0 : expiration.getEpochSecond()));
		return object;
	}

	public boolean isExpired() {
		return expiration.isBefore(Instant.now());
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getAuthToken() {
		return authToken;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Account) {
			Account other = (Account) obj;
			return name.equals(other.name) &&
				uuid.equals(other.uuid);
		}
		return false;
	}
}
