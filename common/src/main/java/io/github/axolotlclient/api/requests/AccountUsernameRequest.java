package io.github.axolotlclient.api.requests;

import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;

public class AccountUsernameRequest {

	public static CompletableFuture<Response> post(String username, boolean pub) {
		return API.getInstance().post(Request.builder().route(Request.Route.ACCOUNT_USERNAME)
			.path(username).query("public", pub).build());
	}

	public static CompletableFuture<Response> delete(String username) {
		return API.getInstance().delete(Request.builder().route(Request.Route.ACCOUNT_USERNAME)
			.path(username).build());
	}
}
