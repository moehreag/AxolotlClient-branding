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

package io.github.axolotlclient.util;

import java.io.IOException;

import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@UtilityClass
public class NetworkUtil {

	public JsonElement getRequest(String url, CloseableHttpClient client) throws IOException {
		return request(new HttpGet(url), client);
	}

	public JsonElement request(HttpUriRequest request, CloseableHttpClient client) throws IOException {
		return request(request, client, false);
	}

	public JsonElement request(HttpUriRequest request, CloseableHttpClient client, boolean ignoreStatus) throws IOException {
		HttpResponse response = client.execute(request);

		if (!ignoreStatus) {
			int status = response.getStatusLine().getStatusCode();
			if (status != 200) {
				throw new IOException("API request failed, status code " + status + "\nBody: " + EntityUtils.toString(response.getEntity()));
			}
		}

		String responseBody = EntityUtils.toString(response.getEntity());
		return GsonHelper.GSON.fromJson(responseBody, JsonElement.class);
	}

	public JsonElement postRequest(String url, String body, CloseableHttpClient client) throws IOException {
		return postRequest(url, body, client, false);
	}

	public JsonElement postRequest(String url, String body, CloseableHttpClient client, boolean ignoreStatus) throws IOException {
		RequestBuilder requestBuilder = RequestBuilder.post().setUri(url);
		requestBuilder.addHeader("Content-Type", "application/json");
		requestBuilder.addHeader("Accept", "application/json");
		requestBuilder.setEntity(new StringEntity(body));
		return request(requestBuilder.build(), client, ignoreStatus);
	}

	public JsonElement deleteRequest(String url, String body, CloseableHttpClient client) throws IOException {
		RequestBuilder requestBuilder = RequestBuilder.delete().setUri(url);
		requestBuilder.addHeader("Content-Type", "application/json");
		requestBuilder.setEntity(new StringEntity(body));
		return request(requestBuilder.build(), client);
	}

	public CloseableHttpClient createHttpClient(String id) {
		return HttpClients.custom().setUserAgent("AxolotlClient/" + id).build();
	}
}
