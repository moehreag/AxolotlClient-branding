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
		try (JsonReader reader = new JsonReader(new StringReader(json))) {
			return (Map<String, ?>) GsonHelper.read(reader);
		}
	}

	public String toString() {
		return "Response(body=" + mapToString(this.getBody()) + ", plainBody=" + getPlainBody() + ", status=" + this.getStatus() + ", error=" + this.getError() + ")";
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
