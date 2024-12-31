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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;

public class AccountDataRequest {

	public static void get(Path target) {
		API.getInstance().get(Request.Route.ACCOUNT_DATA.create())
			.thenAccept(res -> {
				if (!res.isError()) {
					try {
						Files.writeString(target, res.getPlainBody());
						API.getInstance().getNotificationProvider().addStatus("api.account.export.success.title", "api.account.export.success.description", target);
					} catch (IOException e) {
						API.getInstance().getLogger().warn("Failed to write export file: ", e);
						API.getInstance().getNotificationProvider().addStatus("api.account.export.failure.title", "api.account.export.failure.description_write");
					}
				} else {
					API.getInstance().getNotificationProvider().addStatus("api.account.export.failure.title", "api.account.export.failure.description_server");
				}
			});
	}
}
