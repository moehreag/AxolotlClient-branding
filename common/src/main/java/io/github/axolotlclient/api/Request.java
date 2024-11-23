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

package io.github.axolotlclient.api;


import java.net.URI;
import java.util.*;

import lombok.Getter;
import lombok.ToString;

public record Request(Route route, List<String> path, List<String> query,
					  Map<String, ?> bodyFields, byte[] rawBody, Map<String, String> headers,
					  boolean requiresAuthentication) {

	public URI resolve() {
		return API.getInstance().getUrl(this);
	}

	@ToString
	@Getter
	public enum Route {
		AUTHENTICATE("authenticate"),
		USER("user"),
		ACCOUNT("account", true),
		GATEWAY("gateway", true),
		CHANNELS("channels", true),
		CHANNEL("channel", true),
		ACCOUNT_SETTINGS("account/settings", true),
		ACCOUNT_ACTIVITY("account/activity", true),
		ACCOUNT_DATA("account/data", true),
		ACCOUNT_USERNAME("account/username", true),
		ACCOUNT_RELATIONS_FRIENDS("account/relations/friends", true),
		ACCOUNT_RELATIONS_BLOCKED("account/relations/blocked", true),
		ACCOUNT_RELATIONS_REQUESTS("account/relations/requests", true),
		REPORT("report", true),
		GLOBAL_DATA("global_data"),
		IMAGE("image", true),
		HYPIXEL("hypixel", true);

		private final String path;
		private final boolean requiresAuthentication;

		Route(String path) {
			this.path = path;
			requiresAuthentication = false;
		}

		Route(String path, boolean requiresAuthentication) {
			this.path = path;
			this.requiresAuthentication = requiresAuthentication;
		}

		public Request create() {
			return builder().build();
		}

		public RequestBuilder builder() {
			return new RequestBuilder(this);
		}
	}

	public static class RequestBuilder {
		private final Request.Route route;
		private List<String> path;
		private List<String> query;
		private Map<String, Object> bodyFields;
		private Map<String, String> headers;
		private boolean requiresAuthentication;
		private byte[] rawBody;

		RequestBuilder(Route route) {
			this.route = route;
			this.requiresAuthentication = route.requiresAuthentication;
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
				query = new ArrayList<>();
			}
			query.add(key + "=" + value);
			return this;
		}

		public Request.RequestBuilder query(String key, Object value) {
			if (query == null) {
				query = new ArrayList<>();
			}
			query.add(key + "=" + value);
			return this;
		}

		public Request.RequestBuilder field(String key, Object value) {
			if (bodyFields == null) {
				bodyFields = new HashMap<>();
			}
			bodyFields.put(key, value);
			return this;
		}

		public RequestBuilder rawBody(byte[] body) {
			this.rawBody = body;
			return this;
		}

		public RequestBuilder header(String key, String value) {
			if (headers == null) {
				headers = new HashMap<>();
			}
			headers.put(key, value);
			return this;
		}

		public RequestBuilder requiresAuthentication() {
			requiresAuthentication = true;
			return this;
		}

		public RequestBuilder requiresAuthentication(boolean requiresAuthentication) {
			this.requiresAuthentication = requiresAuthentication;
			return this;
		}

		public Request build() {
			if (rawBody != null && bodyFields != null) {
				throw new IllegalArgumentException("Request cannot have json body and raw body at the same time!");
			}
			return new Request(this.route, this.path, this.query, this.bodyFields, this.rawBody, this.headers, this.requiresAuthentication);
		}

		public String toString() {
			return "Request.RequestBuilder(route=" + this.route + ", path=" + listToString(path) + ", query=" + listToString(this.query) + ", bodyFields=" + mapToString(this.bodyFields) + ")";
		}

		private String mapToString(Map<?, ?> map) {
			StringBuilder builder = new StringBuilder("{");
			map.forEach((o, o2) -> {
				builder.append(o).append(": ").append(o2).append(",\n");
			});
			int length = builder.length();
			builder.delete(length - 2, length - 1);
			builder.append("}");
			return builder.toString();
		}

		private String listToString(Collection<?> c) {
			StringBuilder builder = new StringBuilder("[");
			c.forEach(o -> builder.append(o).append(",\n"));
			builder.append("]");
			return builder.toString();
		}
	}
}
