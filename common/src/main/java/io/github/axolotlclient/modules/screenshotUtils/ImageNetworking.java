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

package io.github.axolotlclient.modules.screenshotUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Constants;
import io.github.axolotlclient.api.Request;

public abstract class ImageNetworking {

	public abstract void uploadImage(Path file);

	protected CompletableFuture<String> upload(Path file) {
		try {
			return upload(file.getFileName().toString(), Files.readAllBytes(file));
		} catch (IOException e) {
			return CompletableFuture.completedFuture("");
		}
	}

	protected CompletableFuture<String> upload(String name, byte[] data) {
		return API.getInstance().post(Request.Route.IMAGE.builder().path(name).rawBody(data).build())
			.thenApply(response -> response.isError() ? "" : Request.Route.IMAGE.builder()
				.path(response.getPlainBody())
				.path("raw").build().resolve().toString());
	}

	protected ImageData download(String url) {
		if (url.contains("/") && !url.startsWith(Constants.API_URL)) {
			return ImageData.EMPTY;
		}
		if (url.endsWith("/raw")) {
			url = url.substring(0, url.length() - 4);
		}
		String id = url.substring(url.lastIndexOf("/") + 1);
		return API.getInstance().get(Request.Route.IMAGE.builder().path(id).build())
			.thenApply(res -> {
				if (res.isError()) {
					return ImageData.EMPTY;
				}
				String name = res.getBody("filename");
				String base64 = res.getBody("file");
				String uploader = res.getBody("uploader");
				Instant sharedAt = res.getBody("shared_at", Instant::parse);
				return new ImageData(name, Base64.getDecoder().decode(base64), uploader, sharedAt);
			}).join();
	}

	public record ImageData(String name, byte[] data, String uploader, Instant sharedAt) {
		public static final ImageData EMPTY = new ImageData("", new byte[0], null, null);
	}
}
