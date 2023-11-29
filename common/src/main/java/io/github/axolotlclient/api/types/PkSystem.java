package io.github.axolotlclient.api.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.github.axolotlclient.util.NetworkUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PkSystem {
	@Serializer.Length(5)
	private String id;
	@Serializer.Exclude
	private String name;
	@Serializer.Exclude
	private final List<Member> fronters = new ArrayList<>();
	@Serializer.Exclude
	private Member firstFronter;

	public PkSystem(String id) {
		this.id = id;
		queryPkAPI("systems/"+id).thenAccept(object -> {
			this.name = getString(object, "name");
		});
		queryPkAPI("systems/" + id + "/fronters").thenAccept(object -> {
			JsonArray fronters = object.getAsJsonArray("members");
			fronters.forEach(e ->
				this.fronters.add(Member.fromObject(e.getAsJsonObject()))
			);
			this.firstFronter = this.fronters.get(0);
		});
	}

	public static CompletableFuture<JsonObject> queryPkAPI(String route) {
		return PluralKitApi.request("https://api.pluralkit.me/v2/"+route);
	}

	public static PkSystem fromMinecraftUuid(String uuid) {
		String pkId = getPkId(uuid);
		if (!pkId.isEmpty()) {
			return new PkSystem(pkId);
		}
		return null;
	}

	public static String getPkId(String uuid) {
		return API.getInstance().send(new Request(Request.Type.QUERY_PK_INFO, uuid)).thenApply(buf -> {
			if (buf.readableBytes() > 0x09) {
				return BufferUtil.getString(buf, 0x09, 5);
			}
			return "";
		}).join();
	}

	public Member getProxy(String message){
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
					proxyTags.add(Pattern.compile(prefix + ".*" + suffix));
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
		private static final Thread thread = new Thread(PluralKitApi::run, "PluralKit API request thread");
		private static final Map<String, CompletableFuture<JsonObject>> requests = new HashMap<>();

		static {
			thread.start();
		}

		public static CompletableFuture<JsonObject> request(String url) {
			CompletableFuture<JsonObject> cF = new CompletableFuture<>();
			if (requests.isEmpty()){
				cF.complete(query(url));
			} else {
				requests.put(url, cF);
			}
			return cF;
		}

		private static void run() {
			while (true) {
				for (Map.Entry<String, CompletableFuture<JsonObject>> entry : requests.entrySet()) {
					String s = entry.getKey();
					CompletableFuture<JsonObject> future = entry.getValue();
					future.complete(query(s));
					try {
						//noinspection BusyWait
						Thread.sleep(505);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		private static JsonObject query(String url) {
			try {
				return NetworkUtil.getRequest(url,
					NetworkUtil.createHttpClient("PluralKit Integration; contact: moehreag<at>gmail.com")).getAsJsonObject();
			} catch (IOException e) {
				return new JsonObject();
			}
		}
	}
}
