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

package io.github.axolotlclient.api.types;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PkSystem {
	private static String token;
	private final String id;
	private final String name;
	private final List<Member> members;
	private final List<Member> fronters;
	private final Member firstFronter;
	private final String tag;
	private final int latchTimeout;
	private final String avatarUrl;

	private Member lastLatchProxy;
	private long lastLatchProxyTime;
	private Member autoproxyMember;

	@Builder(builderClassName = "SystemBuilder")
	public PkSystem(String id, String name, List<Member> members, List<Member> fronters, Member firstFronter, String tag, int latchTimeout, String avatarUrl) {
		this.id = id;
		this.name = name;
		this.members = members;
		this.fronters = fronters;
		this.firstFronter = firstFronter;
		this.tag = tag;
		this.latchTimeout = latchTimeout;
		this.avatarUrl = avatarUrl;
		updateAutoproxyMember(API.getInstance().getApiOptions().autoproxyMember.get());
	}

	public void updateAutoproxyMember(String value) {
		this.autoproxyMember = members.stream().filter(m -> value.toLowerCase(Locale.ROOT).equals(m.getDisplayName()) ||
															value.toLowerCase(Locale.ROOT).equals(m.getId()))
			.findFirst().orElse(null);
	}

	private static CompletableFuture<PkSystem> create(JsonObject system) {

		SystemBuilder builder = builder();
		builder.id(getString(system, "id"));
		builder.name(getString(system, "name"));
		builder.tag(getString(system, "tag"));
		builder.avatarUrl(getString(system, "avatar_url"));

		return queryPkAPI("systems/@me/fronters")
			.thenApply(JsonElement::getAsJsonObject).thenAccept(object -> {
				JsonArray fronters = object.getAsJsonArray("members");
				List<Member> list = new ArrayList<>();
				fronters.forEach(e ->
					list.add(Member.fromObject(e.getAsJsonObject()))
				);
				builder.fronters(list);
				builder.firstFronter(list.get(0));
				log("Fetched fronters list");
			}).thenCompose(v -> queryPkAPI("systems/@me/members").thenAccept(object -> {
				JsonArray array = object.getAsJsonArray();
				List<Member> list = new ArrayList<>();
				array.forEach(e ->
					list.add(Member.fromObject(e.getAsJsonObject()))
				);
				builder.members(list);
				log("Fetched Members list");
			})).thenCompose(v -> queryPkAPI("systems/@me/settings").thenApply(JsonElement::getAsJsonObject)
				.thenAccept(object -> {
					log("Fetched Latch Timeout Settings");
					if (object.has("latch_timeout") && !object.get("latch_timeout").isJsonNull()) {
						builder.latchTimeout(object.get("latch_timeout").getAsInt());
					}
				}))
			.thenApply(v -> {
				PkSystem sys = builder.build();
				log("Logged in as system: " + sys.getName());
				return sys;
			});
	}

	private static void log(String message) {
		API.getInstance().getLogger().debug("[PluralKit] " + message);
	}

	public static CompletableFuture<JsonElement> queryPkAPI(String route) {
		return PluralKitApi.request("https://api.pluralkit.me/v2/" + route);
	}

	public static CompletableFuture<PkSystem> fromToken(String token) {
		if (token.length() != 64) {
			return CompletableFuture.completedFuture(null);
		}
		PkSystem.token = token;
		return queryPkAPI("systems/@me").thenApply(JsonElement::getAsJsonObject)
			.thenCompose(object -> {
				if (object.has("id")) {
					return create(object);
				}
				return null;
			});
	}

	public Optional<String> getProxy(String message) {
		return getProxyMember(message)
			.map(this::decorateMemberName);
	}

	private Optional<Member> getProxyMember(String message) {
		if (!API.getInstance().getApiOptions().autoproxy.get()) {
			return Optional.empty();
		}
		Optional<Member> proxy = members.stream().filter(m -> m.proxyTags.stream()
			.anyMatch(p -> p.matcher(message).matches())).findFirst();
		switch (API.getInstance().getApiOptions().autoproxyMode.get()) {
			case PROXY_FRONT:
				return Optional.ofNullable(proxy.orElse(getFirstFronter()));
			case PROXY_LATCH:
				if ((lastLatchProxyTime - System.currentTimeMillis()) / 1000 > latchTimeout) {
					lastLatchProxy = null;
					return proxy;
				}
				lastLatchProxyTime = System.currentTimeMillis();
				proxy.ifPresent(member -> lastLatchProxy = member);
				if (lastLatchProxy != null) {
					return Optional.of(lastLatchProxy);
				}
				return proxy;
			case PROXY_MEMBER:
				return Optional.ofNullable(proxy.orElse(autoproxyMember));
			case PROXY_OFF:
			default:
				return Optional.empty();
		}
	}

	public String decorateMemberName(Member member) {
		String name = member.getDisplayName();
		if (name.isEmpty()) {
			name = member.getName();
		}
		return name + getTag();
	}

	public Optional<String> getProxyAvatarUrl(Member member) {
		String[] urls = new String[]{
			member.webhookAvatarUrl,
			member.avatarUrl,
			avatarUrl
		};
		for (String url : urls) {
			if (!url.isEmpty()) {
				return Optional.of(url);
			}
		}
		return Optional.empty();
	}

	@AllArgsConstructor
	@Getter
	public static class Member {
		private String id;
		private String name;
		private String displayName;
		private List<Pattern> proxyTags;
		private boolean autoProxy;
		private String avatarUrl;
		private String webhookAvatarUrl;

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
			boolean autoProxy = object.has("autoproxy_enabled") && object.get("autoproxy_enabled").getAsBoolean();
			return new Member(id, name, getString(object, "display_name"), proxyTags, autoProxy,
				getString(object, "avatar_url"), getString(object, "webhook_avatar_url"));
		}
	}

	public enum ProxyMode {
		PROXY_OFF,
		PROXY_FRONT,
		PROXY_LATCH,
		PROXY_MEMBER,
		;

		static ProxyMode ofMode(String mode) {
			return valueOf(mode.toUpperCase(Locale.ROOT));
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

		private final HttpClient client = NetworkUtil.createHttpClient("PluralKit Integration");
		private int remaining = 1;
		private long resetsInMillis = 0;
		private int limit = 2;


		public CompletableFuture<JsonElement> request(HttpRequest request) {
			CompletableFuture<JsonElement> cF = new CompletableFuture<>();
			ThreadExecuter.scheduleTask(() -> cF.complete(schedule(request)));
			return cF;
		}

		public static CompletableFuture<JsonElement> request(String url) {
			HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url));
			if (!token.isEmpty()) {
				builder.header("Authorization", token);
			}
			return getInstance().request(builder.build());
		}

		private synchronized JsonElement schedule(HttpRequest request) {
			if (remaining == 0) {
				try {
					Thread.sleep(resetsInMillis);
				} catch (InterruptedException ignored) {
				}
				remaining = limit;
			}
			return query(request);
		}

		private JsonElement query(HttpRequest request) {
			try {
				log("Requesting: " + request);
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				String responseBody = response.body();
				log("Response: " + responseBody);

				remaining = Integer.parseInt(response.headers().firstValue("X-RateLimit-Remaining").orElseThrow());
				long resetsInMillisHeader = (response.headers().firstValueAsLong("X-RateLimit-Reset")
												 .orElseThrow() * 1000) - System.currentTimeMillis();
				// If the header value is bogus just reset in 0.5 seconds
				this.resetsInMillis = resetsInMillisHeader < 0 ? 500 : resetsInMillisHeader;
				limit = Integer.parseInt(response.headers().firstValue("X-RateLimit-Limit").orElseThrow());

				return GsonHelper.GSON.fromJson(responseBody, JsonElement.class);
			} catch (Exception e) {
				return new JsonObject();
			}
		}
	}
}
