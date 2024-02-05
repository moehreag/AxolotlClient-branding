package io.github.axolotlclient.api;


import java.util.*;
import java.util.concurrent.CompletableFuture;

import jakarta.websocket.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public class Request {

	private final Route route;
	private final List<String> path;
	private final Map<String, String> query;
	private final Map<String, String> bodyFields;


	public static Request.RequestBuilder builder() {
		return new Request.RequestBuilder();
	}


	@ToString @Getter
	public enum Route {
		AUTHENTICATE("authenticate", false),
		USER("user", false),
		ACCOUNT("account"),
		ACCOUNT_USER("account/user"),
		ACCOUNT_SETTINGS("account/settings"),
		ACCOUNT_DATA("account/data");

		private final String path;
		private final boolean authenticated;

		Route(String path) {
			this(path, true);
		}

		Route(String path, boolean authenticated) {
			this.path = path;
			this.authenticated = authenticated;
		}

		public Request create() {
			return new RequestBuilder().route(this).build();
		}
	}

	public static class RequestBuilder {
		private Request.Route route;
		private List<String> path;
		private Map<String, String> query;
		private Map<String, String> bodyFields;

		RequestBuilder() {
		}

		public Request.RequestBuilder route(Request.Route route) {
			this.route = route;
			return this;
		}

		public RequestBuilder path(String parameter) {
			if (path == null) {
				path = new ArrayList<>();
			}
			path.add(parameter);
			return this;
		}

		public Request.RequestBuilder query(String key, String value) {
			if (query == null) {
				query = new HashMap<>();
			}
			query.put(key, value);
			return this;
		}

		public Request.RequestBuilder field(String key, String value) {
			if (bodyFields == null) {
				bodyFields = new HashMap<>();
			}
			bodyFields.put(key, value);
			return this;
		}

		public Request build() {
			return new Request(this.route, this.path, this.query, this.bodyFields);
		}

		public String toString() {
			return "Request.RequestBuilder(route=" + this.route + ", path=" + listToString(path) + ", query=" + mapToString(this.query) + ", bodyFields=" + mapToString(this.bodyFields) + ")";
		}

		private String mapToString(Map<?, ?> map){
			StringBuilder builder = new StringBuilder(map.toString()+"{");
			map.forEach((o, o2) -> {
				builder.append(o).append(": ").append(o2).append("\n");
			});
			builder.append("}");
			return builder.toString();
		}

		private String listToString(Collection<?> c){
			StringBuilder builder = new StringBuilder(c.toString()+"{");
			c.forEach(o -> builder.append(o).append("\n"));
			builder.append("}");
			return builder.toString();
		}
	}
}
