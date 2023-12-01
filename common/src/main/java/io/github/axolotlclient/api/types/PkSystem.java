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

package io.github.axolotlclient.api.types;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.api.util.Serializer;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;

@Data
@AllArgsConstructor
public class PkSystem {
	private static String token;
	@Serializer.Length(5)
	private String id;
	@Serializer.Exclude
	private String name;
	@Serializer.Exclude
	private List<Member> members;
	@Serializer.Exclude
	private List<Member> fronters;
	@Serializer.Exclude
	private Member firstFronter;

	private static CompletableFuture<PkSystem> create(String id) {
		return queryPkAPI("systems/" + id).thenApply(JsonElement::getAsJsonObject)
			.thenApply(object -> getString(object, "name"))
			.thenApply(name -> create(id, name).join());
	}

	private static CompletableFuture<PkSystem> create(String id, String name) {
		return queryPkAPI("systems/" + id + "/fronters")
			.thenApply(JsonElement::getAsJsonObject).thenApply(object -> {
			JsonArray fronters = object.getAsJsonArray("members");
			List<Member> list = new ArrayList<>();
			fronters.forEach(e ->
				list.add(Member.fromObject(e.getAsJsonObject()))
			);
			return list;
		}).thenCombine(queryPkAPI("systems/"+id+"/members").thenApply(object -> {
			JsonArray array = object.getAsJsonArray();
			List<Member> list = new ArrayList<>();
			array.forEach(e ->
				list.add(Member.fromObject(e.getAsJsonObject()))
			);
			return list;
		}), (members, fronters) -> new PkSystem(id, name, members, fronters, fronters.get(0)));
	}

	public static CompletableFuture<JsonElement> queryPkAPI(String route) {
		return PluralKitApi.request("https://api.pluralkit.me/v2/" + route);
	}

	public static CompletableFuture<PkSystem> fromToken(String token) {
		PkSystem.token = token;
		if (token.length() != 64) {
			return CompletableFuture.completedFuture(null);
		}
		return queryPkAPI("systems/@me").thenApply(JsonElement::getAsJsonObject)
			.thenApply(object -> {
				if (object.has("id")) {
					PkSystem system = create(object.get("id").getAsString(), object.get("name").getAsString()).join();
					API.getInstance().getLogger().debug("Logged in as system: " + system.getName());
					return system;
				}
				return null;
			});
	}

	public static CompletableFuture<PkSystem> fromMinecraftUuid(String uuid) {
		return getPkId(uuid).thenApply(pkId -> {
			if (!pkId.isEmpty()) {
				return create(pkId).join();
			}
			return null;
		});
	}

	public static CompletableFuture<String> getPkId(String uuid) {
		return API.getInstance().send(new Request(Request.Type.QUERY_PK_INFO, uuid)).thenApply(buf -> {
			if (buf.readableBytes() > 0x09) {
				return BufferUtil.getString(buf, 0x09, 5);
			}
			return "";
		});
	}

	public Member getProxy(String message) {
		return members.stream().filter(m -> m.proxyTags.stream()
			.anyMatch(p -> p.matcher(message).matches())).findFirst().orElse(getFirstFronter());
	}

	@Data
	@AllArgsConstructor
	public static class Member {
		private String id;
		private String displayName;
		private List<Pattern> proxyTags;

		public static Member fromId(String id) {
			return fromObject(queryPkAPI("members/" + id)
				.thenApply(JsonElement::getAsJsonObject).join());
		}

		public static Member fromObject(JsonObject object) {
			String id = getString(object, "id");
			String name = getString(object, "name");
			JsonArray tags = object.get("proxy_tags").getAsJsonArray();
			List<Pattern> proxyTags = new ArrayList<>();
			tags.forEach(e -> {
				if (e.isJsonObject()) {
					JsonObject o = e.getAsJsonObject();
					String prefix = getString(o, "prefix");
					String suffix = getString(o, "suffix");
					proxyTags.add(Pattern.compile(Pattern.quote(prefix) + ".*" + Pattern.quote(suffix)));
				}
			});
			return new Member(id, name, proxyTags);
		}
	}

	private static String getString(JsonObject object, String key) {
		if (object.has(key)) {
			JsonElement e = object.get(key);
			if (e.isJsonPrimitive() && ((JsonPrimitive) e).isString()) {
				return e.getAsString();
			}
		}
		return "";
	}

	static class PluralKitApi {

		@Getter
		private static final PluralKitApi instance = new PluralKitApi();

		private PluralKitApi() {
		}

		private final HttpClient client = NetworkUtil.createHttpClient("PluralKit Integration; contact: moehreag<at>gmail.com");
		private int remaining = 1;
		private long resetsInMillis = 0;
		private int limit = 2;


		public CompletableFuture<JsonElement> request(HttpUriRequest request) {
			CompletableFuture<JsonElement> cF = new CompletableFuture<>();
			ThreadExecuter.scheduleTask(() -> cF.complete(schedule(request)));
			return cF;
		}

		public static CompletableFuture<JsonElement> request(String url) {
			RequestBuilder builder = RequestBuilder.get().setUri(url);
			if (!token.isEmpty()) {
				builder.addHeader("Authorization", token);
			}
			return getInstance().request(builder.build());
		}

		private synchronized JsonElement schedule(HttpUriRequest request) {
			if (remaining == 0) {
				try {
					Thread.sleep(resetsInMillis);
				} catch (InterruptedException ignored) {
				}
				remaining = limit;
			}
			return query(request);
		}

		private JsonElement query(HttpUriRequest request) {
			try {
				API.getInstance().logDetailed("Requesting: "+request);
				HttpResponse response = client.execute(request);

				String responseBody = EntityUtils.toString(response.getEntity());
				API.getInstance().logDetailed("Response: "+responseBody);

				remaining = Integer.parseInt(response.getFirstHeader("X-RateLimit-Remaining").getValue());
				long resetsInMillisHeader = (Long.parseLong(response.getFirstHeader("X-RateLimit-Reset")
					.getValue())*1000)-System.currentTimeMillis();
				// If the header value is bogus just reset in 0.5 seconds
				this.resetsInMillis = resetsInMillisHeader < 0 ?
					500 : // System.currentTimeMillis() - (System.currentTimeMillis() /1000L)*1000
					resetsInMillisHeader;
				limit = Integer.parseInt(response.getFirstHeader("X-RateLimit-Limit").getValue());

				return GsonHelper.GSON.fromJson(responseBody, JsonElement.class);
			} catch (Exception e) {
				return new JsonObject();
			}
		}
	}
}
