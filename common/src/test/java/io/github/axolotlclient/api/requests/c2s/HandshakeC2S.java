package io.github.axolotlclient.api.requests.c2s;

import com.google.gson.JsonObject;
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
	public ServerResponse handle() {
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
		} catch (Exception ignored) {
		}
		return new HandshakeS2C((byte) 1);
	}


}
