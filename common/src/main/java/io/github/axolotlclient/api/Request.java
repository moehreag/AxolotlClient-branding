/*
 * Copyright © 2021-2024 moehreag <moehreag@gmail.com> & Contributors
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


import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
public class Request {

	private final Route route;
	private final List<String> path;
	private final List<String> query;
	private final Map<String, String> bodyFields;


	public static Request.RequestBuilder builder() {
		return new Request.RequestBuilder();
	}


	@ToString
	@Getter
	public enum Route {
		AUTHENTICATE("authenticate"),
		USER("user"),
		ACCOUNT("account"),
		GATEWAY("gateway"),
		CHANNEL("channel"),
		ACCOUNT_SETTINGS("account/settings"),
		ACCOUNT_DATA("account/data"),
		ACCOUNT_USERNAME("account/username");

		private final String path;
		private final Map<Integer, String> errors;

		Route(String path) {
			this.path = path;
			errors = Collections.emptyMap();
		}

		Route(String path, Map<Integer, String> errors) {
			this.path = path;
			this.errors = errors;
		}

		public Request create() {
			return new RequestBuilder().route(this).build();
		}
	}

	public static class RequestBuilder {
		private Request.Route route;
		private List<String> path;
		private List<String> query;
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

		public Request.RequestBuilder query(String key) {
			if (query == null) {
				query = new ArrayList<>();
			}
			query.add(key);
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
			return field(key, String.valueOf(value));
		}

		public Request.RequestBuilder field(String key, String value) {
			if (bodyFields == null) {
				bodyFields = new HashMap<>();
			}
			bodyFields.put(key, value);
			return this;
		}

		public Request.RequestBuilder field(String key, Map<String, ?> value) {
			if (bodyFields == null) {
				bodyFields = new HashMap<>();
			}
			bodyFields.put(key, mapToString(value));
			return this;
		}

		public Request build() {
			return new Request(this.route, this.path, this.query, this.bodyFields);
		}

		public String toString() {
			return "Request.RequestBuilder(route=" + this.route + ", path=" + listToString(path) + ", query=" + listToString(this.query) + ", bodyFields=" + mapToString(this.bodyFields) + ")";
		}

		private String mapToString(Map<?, ?> map) {
			StringBuilder builder = new StringBuilder(map.toString() + "{");
			map.forEach((o, o2) -> {
				builder.append(o).append(": ").append(o2).append(",\n");
			});
			builder.append("}");
			return builder.toString();
		}

		private String listToString(Collection<?> c) {
			StringBuilder builder = new StringBuilder(c.toString() + "[");
			c.forEach(o -> builder.append(o).append(",\n"));
			builder.append("]");
			return builder.toString();
		}
	}
}
