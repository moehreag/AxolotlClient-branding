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

package io.github.axolotlclient.api.util;

import java.io.IOException;

import com.google.gson.JsonElement;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.util.NetworkUtil;

public class UUIDHelper {

	public static String getUsername(String uuid) {
		try {
			JsonElement e = NetworkUtil.getRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid, NetworkUtil.createHttpClient("API"));
			return e.getAsJsonObject().get("name").getAsString();
		} catch (IOException e) {
			API.getInstance().getLogger().warn("Conversion uuid -> username failed: ", e);
		}
		return uuid;
	}

	public static String getUuid(String username) {
		try {
			JsonElement response = NetworkUtil.getRequest("https://api.mojang.com/users/profiles/minecraft/" + username, NetworkUtil.createHttpClient("API"));
			return response.getAsJsonObject().get("id").getAsString();
		} catch (IOException e) {
			API.getInstance().getLogger().warn("Conversion username -> uuid failed: ", e);
		}
		return username;
	}
}
