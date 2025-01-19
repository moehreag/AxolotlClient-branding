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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Constants;
import io.github.axolotlclient.api.Request;

public abstract class ImageNetworking {

	private static final Pattern URL_PATTERN = Pattern.compile("(?:.+/)?(\\d+)(?:/?.+)?");

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
			.thenApply(response -> response.isError() ? "" : idToUrl(response.getPlainBody()));
	}

	protected static String idToUrl(String id) {
		return Request.Route.IMAGE.builder().path(id).path("view").build().resolve().toString();
	}

	protected static Optional<String> urlToId(String url) {
		if (url.contains("/") && !url.startsWith(Constants.API_URL)) {
			return Optional.empty();
		}
		Matcher matcher = URL_PATTERN.matcher(url);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		return Optional.of(matcher.group(1));
	}

	protected static Optional<String> ensureUrl(String urlOrId) {
		return urlToId(urlOrId).map(ImageNetworking::idToUrl);
	}

	protected CompletableFuture<ImageData> download(String url) {
		Optional<String> id = urlToId(url);
		return id.map(s -> API.getInstance().get(Request.Route.IMAGE.builder().path(s).build())
			.thenApply(res -> {
				if (res.isError()) {
					return ImageData.EMPTY;
				}
				String name = res.getBody("filename");
				String base64 = res.getBody("file");
				String uploader = res.getBody("uploader");
				Instant sharedAt = res.getBody("shared_at", Instant::parse);
				return new ImageData(name, Base64.getDecoder().decode(base64), uploader, sharedAt);
			})).orElseGet(() -> CompletableFuture.completedFuture(ImageData.EMPTY));
	}

	public record ImageData(String name, byte[] data, String uploader, Instant sharedAt) {
		public static final ImageData EMPTY = new ImageData("", new byte[0], null, null);
	}
}
