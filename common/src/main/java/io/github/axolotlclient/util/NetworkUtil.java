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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.github.mizosoft.methanol.Methanol;
import com.google.gson.JsonElement;
import io.github.axolotlclient.AxolotlClientCommon;
import lombok.experimental.UtilityClass;


@UtilityClass
public class NetworkUtil {

	public JsonElement getRequest(String url, HttpClient client) throws IOException {
		return request(HttpRequest.newBuilder(URI.create(url)).build(), client);
	}

	public JsonElement request(HttpRequest request, HttpClient client) throws IOException {
		return request(request, client, false);
	}

	public JsonElement request(HttpRequest request, HttpClient client, boolean ignoreStatus) throws IOException {
		HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();

		if (!ignoreStatus) {
			int status = response.statusCode();
			if (status < 200 || status >= 300) {
				throw new IOException("API request failed, status code " + status + "\nBody: " + response.body());
			}
		}

		String responseBody = response.body();
		return GsonHelper.GSON.fromJson(responseBody, JsonElement.class);
	}

	public JsonElement postRequest(String url, String body, HttpClient client) throws IOException {
		return postRequest(url, body, client, false);
	}

	public JsonElement postRequest(String url, String body, HttpClient client, boolean ignoreStatus) throws IOException {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.POST(HttpRequest.BodyPublishers.ofString(body)).uri(URI.create(url));
		requestBuilder.header("Content-Type", "application/json");
		requestBuilder.header("Accept", "application/json");
		return request(requestBuilder.build(), client, ignoreStatus);
	}

	public HttpClient createHttpClient(String id) {
		return Methanol.newBuilder().userAgent("AxolotlClient/" + id + " (" + AxolotlClientCommon.VERSION + ") contact: moehreag<at>gmail.com")
			.followRedirects(HttpClient.Redirect.NORMAL)
			.executor(ThreadExecuter.service()).build();
	}
}
