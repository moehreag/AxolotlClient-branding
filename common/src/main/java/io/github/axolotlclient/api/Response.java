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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.gson.stream.JsonReader;
import io.github.axolotlclient.util.GsonHelper;
import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {
	public static final Response CLIENT_ERROR = new Response(Collections.emptyMap(), "", 0, new Error(0, "Client Request Error!"));

	private Map<String, ?> body;
	private final String plainBody;
	private final int status;
	private final Error error;

	@Builder
	private Response(String body, int status) {
		try {
			this.body = parseJson(body);
		} catch (IOException ignored) {
		}
		plainBody = body;
		this.status = status;
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

	@SuppressWarnings("unchecked")
	private Map<String, ?> parseJson(String json) throws IOException {
		if (!json.isEmpty()) {
			try (JsonReader reader = new JsonReader(new StringReader(json))) {
				return (Map<String, ?>) GsonHelper.read(reader);
			}
		}
		return Collections.emptyMap();
	}

	public String toString() {
		return "Response(body=" + mapToString(this.getBody()) + ", plainBody=" + getPlainBody() + ", status=" + this.getStatus() + ", error=" + this.getError() + ")";
	}

	@SuppressWarnings("unchecked")
	public <T> T getBody(String path) {
		path = path.replace("\\.", "_#+#_");
		String[] elements = path.split("\\.");

		Object o = getBody();
		for (String s : elements) {
			s = s.replace("_#+#_", ".");
			if (!(o instanceof Map<?,?>)) {
				return null;
			}
			Map<?, ?> map = ((Map<?, ?>)o);
			if (map.containsKey(s)) {
				o = map.get(s);
			} else {
				return null;
			}
		}
		return (T) o;
	}

	@Data
	public static class Error {
		private final int httpCode;
		private final String description;

		public static Error of(Response response) {
			if (response.status >= 200 && response.status < 300) {
				return null;
			}
			String description = (String) response.body.get("description");
			return new Error(response.status, description);
		}
	}
}
