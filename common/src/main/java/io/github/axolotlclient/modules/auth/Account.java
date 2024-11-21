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

package io.github.axolotlclient.modules.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.AccessLevel;
import lombok.Getter;

public class Account {

	public static final String OFFLINE_TOKEN = "AxolotlClient/Offline";

	@Getter
	private final String uuid;
	@Getter
	private final String name;

	@Getter(AccessLevel.PACKAGE)
	private final String msaToken;

	@Getter
	private final String authToken;
	@Getter(AccessLevel.PACKAGE)
	private final String refreshToken;
	private final Instant expiration;

	public Account(String name, String uuid, String accessToken) {
		this.name = name;
		this.uuid = uuid.replace("-", "");
		this.authToken = accessToken;
		expiration = Instant.EPOCH;
		refreshToken = "";
		msaToken = "";
	}

	public Account(JsonObject profile, String authToken, String msaToken, String refreshToken) {
		uuid = profile.get("id").getAsString();
		name = profile.get("name").getAsString();
		this.authToken = authToken;
		this.msaToken = msaToken;
		this.refreshToken = refreshToken;
		expiration = Instant.now().plus(1, ChronoUnit.DAYS);
	}

	private Account(String uuid, String name, String authToken, String msaToken, String refreshToken, long expiration) {
		this.uuid = uuid;
		this.name = name;
		this.authToken = authToken;
		this.msaToken = msaToken;
		this.refreshToken = refreshToken;
		this.expiration = Instant.ofEpochSecond(expiration);
	}

	public static Account deserialize(JsonObject object) {
		String uuid = object.get("uuid").getAsString();
		String name = object.get("name").getAsString();
		String authToken = object.get("authToken").getAsString();
		String refreshToken = object.get("refreshToken").getAsString();
		String msaToken = object.has("msToken") ? object.get("msToken").getAsString() : "";
		long expiration = object.get("expiration").getAsLong();
		return new Account(uuid, name, authToken, msaToken, refreshToken, expiration);
	}

	public void refresh(MSAuth auth, Runnable runAfter) {
		ThreadExecuter.scheduleTask(() -> auth.refreshToken(refreshToken, this, runAfter));
	}

	public boolean isOffline() {
		return authToken.equals(OFFLINE_TOKEN) || authToken.length() < 400;
	}

	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.add("uuid", new JsonPrimitive(uuid));
		object.add("name", new JsonPrimitive(name));
		object.add("authToken", new JsonPrimitive(authToken));
		object.add("msToken", new JsonPrimitive(msaToken));
		object.add("refreshToken", new JsonPrimitive(refreshToken == null ? "" : refreshToken));
		object.add("expiration", new JsonPrimitive(expiration == null ? 0 : expiration.getEpochSecond()));
		return object;
	}

	public boolean isExpired() {
		return expiration.isBefore(Instant.now());
	}

	public boolean needsRefresh() {
		return expiration.isBefore(Instant.now().minus(6, ChronoUnit.HOURS));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Account other) {
			return name.equals(other.name) &&
				   uuid.equals(other.uuid);
		}
		return false;
	}
}
