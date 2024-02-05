package io.github.axolotlclient.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import lombok.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {
	public static final Response CLIENT_ERROR = new Response(Collections.emptyMap(), 0, new Error(0, 1, "Client Request Error!"));

	private final Map<String, ?> body;
	private final int status;
	private final Error error;

	@Builder
	private Response(Map<String, ?> body, int status) {
		this.body = body;
		this.status = status;
		this.error = Error.of(this);
	}

	public boolean isError() {
		return error != null;
	}

	private String mapToString(Map<?, ?> map) {
		StringBuilder builder = new StringBuilder(map.toString() + "{");
		map.forEach((o, o2) -> {
			builder.append(o).append(": ").append(o2).append("\n");
		});
		builder.append("}");
		return builder.toString();
	}

	private String listToString(Collection<?> c) {
		StringBuilder builder = new StringBuilder(c.toString() + "{");
		c.forEach(o -> builder.append(o).append("\n"));
		builder.append("}");
		return builder.toString();
	}

	public String toString() {
		return "Response(body=" + mapToString(this.getBody()) + ", status=" + this.getStatus() + ", error=" + this.getError() + ")";
	}

	@Data
	public static class Error {
		private final int httpCode;
		private final int apiCode;
		private final String description;

		public static Error of(Response response) {
			if (response.status >= 200 && response.status < 300) {
				return null;
			}
			if (!response.body.containsKey("error_code")) {
				return null;
			}
			int http = (int) response.body.get("status_code");
			int error = (int) response.body.get("error_code");
			String description = (String) response.body.get("description");
			return new Error(http, error, description);
		}
	}
}
