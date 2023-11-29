package io.github.axolotlclient.api.types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.api.util.Serializer;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

@Data
@AllArgsConstructor
public class PkSystem {
	private static String token;
	@Serializer.Length(5)
	private String id;
	@Serializer.Exclude
	private String name;
	@Serializer.Exclude
	private List<Member> fronters;
	@Serializer.Exclude
	private Member firstFronter;

	private static CompletableFuture<PkSystem> create(String id) {
		return queryPkAPI("systems/" + id).thenApply(object -> getString(object, "name"))
			.thenApply(name -> create(id, name).join());
	}

	private static CompletableFuture<PkSystem> create(String id, String name) {
		return queryPkAPI("systems/" + id + "/fronters").thenApply(object -> {
			JsonArray fronters = object.getAsJsonArray("members");
			List<Member> list = new ArrayList<>();
			fronters.forEach(e ->
				list.add(Member.fromObject(e.getAsJsonObject()))
			);
			return list;
		}).thenApply(list -> new PkSystem(id, name, list, list.get(0)));
	}

	public static CompletableFuture<JsonObject> queryPkAPI(String route) {
		return PluralKitApi.request("https://api.pluralkit.me/v2/" + route);
	}

	public static CompletableFuture<PkSystem> fromToken(String token) {
		PkSystem.token = token;
		if (token.length() != 64) {
			System.out.println(token.length() + ": " + token.getBytes(StandardCharsets.UTF_8).length);
			return CompletableFuture.completedFuture(null);
		}
		return queryPkAPI("systems/@me")
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
		return fronters.stream().filter(m -> m.proxyTags.stream()
			.anyMatch(p -> p.matcher(message).matches())).findFirst().orElse(getFirstFronter());
	}

	@Data
	@AllArgsConstructor
	public static class Member {
		private String id;
		private String displayName;
		private List<Pattern> proxyTags;

		public static Member fromId(String id) {
			return fromObject(queryPkAPI("members/" + id).join());
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

	@SuppressWarnings("UnstableApiUsage")
	static class PluralKitApi {
		private static final RateLimiter limiter = RateLimiter.create(2);

		public static CompletableFuture<JsonObject> request(HttpUriRequest request) {
			CompletableFuture<JsonObject> cF = new CompletableFuture<>();
			ThreadExecuter.scheduleTask(() -> {
				limiter.acquire();
				cF.complete(query(request));
			});
			return cF;
		}

		public static CompletableFuture<JsonObject> request(String url) {
			RequestBuilder builder = RequestBuilder.get().setUri(url);
			if (!token.isEmpty()) {
				builder.addHeader("Authorization", token);
			}
			return request(builder.build());
		}

		private static JsonObject query(HttpUriRequest request) {
			try {
				return NetworkUtil.request(request,
					NetworkUtil.createHttpClient("PluralKit Integration; contact: moehreag<at>gmail.com")).getAsJsonObject();
			} catch (IOException e) {
				return new JsonObject();
			}
		}
	}
}
