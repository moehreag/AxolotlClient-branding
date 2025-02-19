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

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.axolotlclient.util.GsonHelper;
import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {
	public static final Response CLIENT_ERROR = new Response(Collections.emptyMap(), "", 0, Collections.emptyMap(), new Error(0, "Client Request Error!"));

	private Object body;
	private final String plainBody;
	private final int status;
	private final Map<String, List<String>> headers;
	private final Error error;

	@Builder
	private Response(String body, int status, Map<String, List<String>> headers) {
		try {
			this.body = parseJson(body);
		} catch (IOException ignored) {
		}
		plainBody = body;
		this.status = status;
		this.headers = headers;
		this.error = Error.of(this);
	}

	public boolean isError() {
		return error != null;
	}

	private String mapToString(Map<?, ?> map) {
		StringBuilder builder = new StringBuilder(map.toString() + "{");
		map.forEach((o, o2) -> builder.append(o).append(": ").append(o2).append("\n"));
		builder.append("}");
		return builder.toString();
	}

	private String listToString(Collection<?> c) {
		StringBuilder builder = new StringBuilder(c.toString() + "{");
		c.forEach(o -> builder.append(o).append("\n"));
		builder.append("}");
		return builder.toString();
	}

	private String bodyToString(Object body) {
		if (body instanceof Collection) {
			return listToString((Collection<?>) body);
		} else if (body instanceof Map) {
			return mapToString((Map<?, ?>) body);
		}
		return String.valueOf(body);
	}

	private Object parseJson(String json) throws IOException {
		if (!json.isEmpty()) {
			return GsonHelper.read(json);
		}
		return Collections.emptyMap();
	}

	public String toString() {
		return "Response(headers=" + this.getHeaders() + ", body=" + bodyToString(this.getBody()) + ", plainBody=" + getPlainBody() + ", status=" + this.getStatus() + ", error=" + this.getError() + ")";
	}

	@SuppressWarnings("unchecked")
	public <T> T getBody(String path) {
		path = path.replace("\\.", "_#+#_");
		String[] elements = path.split("\\.");

		Object o = getBody();
		for (String s : elements) {
			s = s.replace("_#+#_", ".");
			if (o instanceof Map<?, ?> map) {
				if (map.containsKey(s)) {
					o = map.get(s);
					continue;
				}
			} else if (o instanceof List<?> list) {
				try {
					int i = Integer.parseInt(s);
					o = list.get(i);
					continue;
				} catch (Exception ignored) {
				}
			}
			return null;
		}
		return (T) o;
	}

	public <T> T getBodyOrElse(String path, T other) {
		T element = getBody(path);
		return element != null ? element : other;
	}

	public <T> Optional<T> getBodyOpt(String path) {
		return Optional.ofNullable(getBody(path));
	}

	public <T, U> U getBody(String path, Function<T, U> mapper) {
		T element = getBody(path);
		if (element != null) {
			return mapper.apply(element);
		}
		return null;
	}

	public boolean bodyHas(String path) {
		return getBody(path) != null;
	}

	public <T> T ifBodyHas(String path, Supplier<T> supplier) {
		Object content = getBody(path);
		if (content != null) {
			return supplier.get();
		}
		return null;
	}

	public Optional<String> firstHeader(String name) {
		return Optional.ofNullable(headers.getOrDefault(name, null)).map(index -> index.get(0));
	}

	public record Error(int httpCode, String description) {
		public static Error of(Response response) {
			if (response.status >= 200 && response.status < 300) {
				return null;
			}
			return new Error(response.status, response.getBodyOrElse("description", "Unexpected internal error"));
		}
	}
}
