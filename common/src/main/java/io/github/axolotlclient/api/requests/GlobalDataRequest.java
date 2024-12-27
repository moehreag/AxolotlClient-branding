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

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.types.GlobalData;
import io.github.axolotlclient.api.types.SemVer;

public class GlobalDataRequest {
	private static GlobalData cachedData = null;
	private static Instant nextRequest = null;
	private static Semaphore lock = new Semaphore(1);

	public static CompletableFuture<GlobalData> get() {
		return get(false);
	}

	public static CompletableFuture<GlobalData> get(boolean forceRequest) {
		if (API.getInstance().getApiOptions().enabled.get()) {
			try {
				lock.acquire();
			} catch (InterruptedException ignored) {}
			if (cachedData != null) {
				var now = Instant.now();
				if (!forceRequest && nextRequest.isAfter(now)) {
					lock.release();
					return CompletableFuture.completedFuture(cachedData);
				}
			}
			return API.getInstance().get(Request.Route.GLOBAL_DATA.create())
				.thenApply(res -> {
					if (res.isError()) {
						return GlobalData.EMPTY;
					}
					return new GlobalData(true, res.getBody("total_players"),
						res.getBody("online_players"), SemVer.parse(res.getBody("latest_version")), res.getBodyOrElse("notes", ""));
				})
				.thenApply(d -> {
					nextRequest = Instant.now().plusSeconds(300);
					cachedData = d;
					lock.release();
					return d;
				});
		}
		return CompletableFuture.completedFuture(GlobalData.EMPTY);
	}
}
