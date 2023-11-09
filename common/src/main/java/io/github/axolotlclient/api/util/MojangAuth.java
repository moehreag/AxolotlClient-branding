/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.google.gson.JsonObject;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.GsonHelper;
import lombok.Builder;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class MojangAuth {

	public static Result authenticate(Account account, byte[] publicKey) {
		Result.Builder result = Result.builder();
		try (CloseableHttpClient client = HttpClients.createDefault()) {

			SecretKey secretKey = generateSecretKey();

			RequestBuilder builder = RequestBuilder.post();
			builder.addHeader("Content-Type", "application/json");

			builder.addHeader("Accept", "application/json");

			JsonObject body = new JsonObject();
			body.addProperty("accessToken", account.getAuthToken());
			body.addProperty("selectedProfile", account.getUuid());
			assert secretKey != null;
			String serverId = minecraftSha1(account.getName().getBytes(StandardCharsets.US_ASCII), publicKey,
				secretKey.getEncoded());
			result.serverId(serverId);
			body.addProperty("serverId", serverId);

			builder.setEntity(new StringEntity(body.toString()));

			HttpResponse response = client.execute(builder.build());

			HttpEntity entity = response.getEntity();
			if (entity == null && response.getStatusLine().getStatusCode() == 204) {
				return result.status(Status.SUCCESS).build();
			} else if (entity != null) {
				JsonObject element = GsonHelper.fromJson(EntityUtils.toString(entity));

				if (element.get("error").getAsString().equals("InsufficientPrivilegesException")) {
					return result.status(Status.MULTIPLAYER_DISABLED).build();
				} else if (element.get("error").getAsString().equals("UserBannedException")) {
					return result.status(Status.USER_BANNED).build();
				}
			}

		} catch (IOException ignored) {
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

	@Data
	@Builder(builderClassName = "Builder")
	public static class Result {
		private final Status status;
		private final String serverId;
	}

	public enum Status {
		SUCCESS,
		MULTIPLAYER_DISABLED,
		USER_BANNED,
		FAILURE
	}

}
