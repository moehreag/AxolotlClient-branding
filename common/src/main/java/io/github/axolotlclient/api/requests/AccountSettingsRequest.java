package io.github.axolotlclient.api.requests;

import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.AccountSettings;

public class AccountSettingsRequest {

	public static CompletableFuture<AccountSettings> get() {
		return API.getInstance().get(Request.builder().route(Request.Route.ACCOUNT_SETTINGS).build())
			.thenApply(response ->
				new AccountSettings(
					response.getBody("show_registered"),
					response.getBody("retain_usernames"),
					response.getBody("show_last_online"),
					response.getBody("sho_activity")));
	}

	public static CompletableFuture<Void> update(AccountSettings settings) {
		return API.getInstance().patch(Request.builder().route(Request.Route.ACCOUNT_SETTINGS)
				.field("show_registered", settings.isShowRegistered())
				.field("retain_usernames", settings.isRetainUsernames())
				.field("show_last_online", settings.isShowLastOnline())
				.field("show_activity", settings.isShowActivity())
				.build())
			.thenAccept(response -> {

			});
	}
}
