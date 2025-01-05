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

package io.github.axolotlclient.api.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;

import com.google.gson.JsonObject;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.NetworkUtil;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;

public class MojangAuth {

	public static Result authenticate(Account account) {
		Result.Builder result = Result.builder();
		//try (HttpClient client = NetworkUtil.createHttpClient("MojangAuth")) {
		try { // Can't use try-with-resources because java 17's HttpClient doesn't implement AutoCloseable...
			HttpClient client = NetworkUtil.createHttpClient("MojangAuth");

			HttpRequest.Builder builder = HttpRequest.newBuilder().timeout(Duration.ofSeconds(10));
			builder.header("Content-Type", "application/json; charset=utf-8");
			builder.header("Accept", "application/json");

			JsonObject body = new JsonObject();
			body.addProperty("accessToken", account.getAuthToken());
			body.addProperty("selectedProfile", account.getUuid());

			String serverId = minecraftSha1(RandomStringUtils.random(40).getBytes(StandardCharsets.UTF_8));

			result.serverId(serverId);
			body.addProperty("serverId", serverId);
			builder.header("Authorization", "Bearer "+account.getAuthToken());

			builder.POST(HttpRequest.BodyPublishers.ofByteArray(body.toString().getBytes(StandardCharsets.UTF_8)));
			builder.uri(URI.create("https://sessionserver.mojang.com/session/minecraft/join"));

			HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

			if ((response.body() == null || response.body().isEmpty()) && response.statusCode() == 204) {
				return result.status(Status.SUCCESS).build();
			} else {
				JsonObject element = GsonHelper.fromJson(response.body());

				if (!element.has("error")) {
					API.getInstance().logDetailed("Don't know how to handle response: "+element);
				}

				String error = element.get("error").getAsString();
				/*if ("ForbiddenOperationException".equals(error) && "INVALID_SIGNATURE".equals(element.get("errorMessage").getAsString())) {
					return account.refresh(MSAuth.INSTANCE).thenApply(MojangAuth::authenticate).join();
				}*/

				API.getInstance().logDetailed("Response code: "+response.statusCode());
				API.getInstance().logDetailed(element.toString());

				if (error.equals("InsufficientPrivilegesException")) {
					return result.status(Status.MULTIPLAYER_DISABLED).build();
				} else if (error.equals("UserBannedException")) {
					return result.status(Status.USER_BANNED).build();
				}
			}

		} catch (Exception e) {
			API.getInstance().logDetailed("MojangAuth Exception: ", e);
		}
		return result.status(Status.FAILURE).build();
	}

	private static String minecraftSha1(byte[]... bytes) {
		int length = Arrays.stream(bytes).mapToInt(a -> a.length).sum();
		byte[] data = new byte[length];

		int index = 0;

		for (byte[] arr : bytes) {
			int size = arr.length;
			System.arraycopy(arr, 0, data, index, size);
			index += size;
		}

		try {
			return new BigInteger(MessageDigest.getInstance("SHA-1").digest(data)).toString(16);
		} catch (NoSuchAlgorithmException ignored) {
		}
		return null;
	}

	private static SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			return keyGenerator.generateKey();
		} catch (Exception ignored) {
		}
		return null;
	}

	public enum Status {
		SUCCESS,
		MULTIPLAYER_DISABLED,
		USER_BANNED,
		FAILURE
	}

	@Data
	@Builder(builderClassName = "Builder")
	public static class Result {
		private final Status status;
		private final String serverId;
	}

}
