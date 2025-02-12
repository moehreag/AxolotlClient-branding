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

package io.github.axolotlclient.api.requests;

import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.AccountSettings;

public class AccountSettingsRequest {

	public static CompletableFuture<AccountSettings> get() {
		return API.getInstance().get(Request.Route.ACCOUNT_SETTINGS.builder().build())
			.thenApply(response ->
				new AccountSettings(
					response.getBody("show_registered"),
					response.getBody("retain_usernames"),
					response.getBody("show_last_online"),
					response.getBody("show_activity"),
					response.getBodyOrElse("allow_friends_image_access", true)));
	}

	public static CompletableFuture<Void> update(AccountSettings settings) {
		return API.getInstance().patch(Request.Route.ACCOUNT_SETTINGS.builder()
				.field("show_registered", settings.showRegistered())
				.field("retain_usernames", settings.retainUsernames())
				.field("show_last_online", settings.showLastOnline())
				.field("show_activity", settings.showActivity())
				.field("allow_friends_image_access", settings.allowFriendsImageAccess())
				.build())
			.thenAccept(response -> {

			});
	}

	public static CompletableFuture<Boolean> deleteAccount() {
		return API.getInstance().delete(Request.Route.ACCOUNT.create()).thenApply(res -> !res.isError());
	}
}
