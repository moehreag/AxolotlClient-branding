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

package io.github.axolotlclient.api.requests.c2s;

import java.util.logging.Level;

import com.google.gson.JsonObject;
import io.github.axolotlclient.api.ApiTestServer;
import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.requests.s2c.HandshakeS2C;
import io.github.axolotlclient.api.util.Serializer;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class HandshakeC2S extends ServerRequest {

	public String uuid;
	public String serverId;
	public String name;

	public HandshakeC2S(@Serializer.Length(32) String uuid, @Serializer.Length(40) String serverId, String name) {
		this.uuid = uuid;
		this.serverId = serverId;
		this.name = name;
	}

	@Override
	public ServerResponse handle(String senderUuid) {
		System.out.printf("Handshake: "+senderUuid+" (%s); serverId=%s%n", name, serverId);
		RequestBuilder builder = RequestBuilder.get();
		builder.setUri("https://sessionserver.mojang.com/session/minecraft/hasJoined");
		builder.addParameter("username", name);
		builder.addParameter("serverId", serverId);
		try (CloseableHttpClient client = NetworkUtil.createHttpClient("API-Test Server")) {
			HttpResponse response = client.execute(builder.build());
			String body = EntityUtils.toString(response.getEntity());
			JsonObject object = GsonHelper.fromJson(body);
			if (object.has("id") && object.get("id").getAsString().equals(uuid)) {
				return new HandshakeS2C((byte) 0);
			}
		} catch (Exception e) {
			ApiTestServer.getInstance().getLogger().log(Level.WARNING, "", e);
		}
		return new HandshakeS2C((byte) 1);
	}


}
