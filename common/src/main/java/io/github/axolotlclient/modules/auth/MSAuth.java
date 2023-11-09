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

package io.github.axolotlclient.modules.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpServer;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.OSUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

// Partly oriented on In-Game-Account-Switcher by The-Fireplace, VidTu
public class MSAuth {

	private static final String CLIENT_ID = "938592fc-8e01-4c6d-b56d-428c7d9cf5ea"; // AxolotlClient MSA ClientID
	private static final int PORT = 59281;
	private static final String FALLBACK_RESPONSE = "You may now close this tab.";

	private final Logger logger;
	private final Accounts accounts;
	private HttpServer server;

	public MSAuth(Logger logger, Accounts accounts) {
		this.logger = logger;
		this.accounts = accounts;
	}

	public void startAuth(Runnable whenFinished) {
		try {
			OSUtil.getOS().open(new URI("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize" +
				"?client_id=" + CLIENT_ID +
				"&response_type=code" +
				"&scope=XboxLive.signin%20XboxLive.offline_access" +
				"&redirect_uri=http://localhost:" + PORT +
				"&prompt=select_account"), logger);
			msAuthCode(whenFinished);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void msAuthCode(Runnable whenFinished) {
		try {
			server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
			server.createContext("/", ex -> {
				logger.debug("Microsoft authentication callback request: " + ex.getRemoteAddress());
				byte[] b = null;
				try (InputStream in = this.getClass().getResourceAsStream("/assets/axolotlclient/redirect.html")) {
					if (in != null) {
						b = IOUtils.toByteArray(in);
					}
				}
				if (b == null) {
					b = FALLBACK_RESPONSE.getBytes(StandardCharsets.UTF_8);
				}
				ex.getResponseHeaders().add("Content-Type", "text/html");
				ex.sendResponseHeaders(307, b.length);
				ex.getResponseBody().write(b);
				String query = ex.getRequestURI().getQuery();
				close();
				authenticate(query, whenFinished);
			});
			server.start();
		} catch (Throwable t) {
			close();
		}
	}

	public void close() {
		if (server != null) {
			server.stop(0);
		}
	}

	public void authenticate(String params, Runnable whenFinished) {
		try {
			String authCode = params.replace("code=", "");
			logger.debug("getting ms token... ");
			Map.Entry<String, String> msTokens = getMSTokens(authCode);
			logger.debug("getting xbl token... ");
			String xblToken = authXbl(msTokens.getKey());
			logger.debug("getting xsts token... ");
			Map.Entry<String, String> xsts = authXstsMC(xblToken);
			logger.debug("getting mc auth token...");
			String accessToken = authMC(xsts.getValue(), xsts.getKey());
			if (checkOwnership(accessToken)) {
				logger.debug("finished auth flow!");
				Account account = new Account(getMCProfile(accessToken), accessToken, msTokens.getValue());
				if (accounts.isContained(account.getUuid())) {
					accounts.getAccounts().removeAll(accounts.getAccounts().stream().filter(acc -> acc.getUuid().equals(account.getUuid())).collect(Collectors.toList()));
				}
				accounts.addAccount(account);
				accounts.login(account);
				whenFinished.run();
			} else {
				throw new IllegalStateException("Do you actually own the game?");
			}
		} catch (Exception e) {
			logger.error("Failed to authenticate!", e);
		}
	}

	public Map.Entry<String, String> getMSTokens(String authCode) throws IOException {
		List<NameValuePair> form = new ArrayList<>();
		form.add(new BasicNameValuePair("client_id", CLIENT_ID));
		form.add(new BasicNameValuePair("code", authCode));
		form.add(new BasicNameValuePair("scope", "XboxLive.signin XboxLive.offline_access"));
		form.add(new BasicNameValuePair("redirect_uri", "http://localhost:" + PORT));
		form.add(new BasicNameValuePair("grant_type", "authorization_code"));
		RequestBuilder requestBuilder = RequestBuilder.post()
			.setUri("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
			.addHeader("ContentType", "application/x-www-form-urlencoded")
			.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
		JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();

		return new AbstractMap.SimpleImmutableEntry<>(response.get("access_token").getAsString(), response.get("refresh_token").getAsString());
	}

	public String authXbl(String code) throws IOException {
		JsonObject object = new JsonObject();
		JsonObject properties = new JsonObject();
		properties.add("AuthMethod", new JsonPrimitive("RPS"));
		properties.add("SiteName", new JsonPrimitive("user.auth.xboxlive.com"));
		properties.add("RpsTicket", new JsonPrimitive("d=" + code));
		object.add("Properties", properties);
		object.add("RelyingParty", new JsonPrimitive("http://auth.xboxlive.com"));
		object.add("TokenType", new JsonPrimitive("JWT"));
		RequestBuilder requestBuilder = RequestBuilder.post()
			.setUri("https://user.auth.xboxlive.com/user/authenticate")
			.setEntity(new StringEntity(object.toString(), ContentType.APPLICATION_JSON))
			.addHeader("Content-Type", "application/json")
			.addHeader("Accept", "application/json");

		JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();
		return response.get("Token").getAsString();
	}

	public Map.Entry<String, String> authXstsMC(String xblToken) throws IOException {
		String body = "{" +
			"    \"Properties\": {" +
			"        \"SandboxId\": \"RETAIL\"," +
			"        \"UserTokens\": [" +
			"            \"" + xblToken + "\"" +
			"        ]" +
			"    }," +
			"    \"RelyingParty\": \"rp://api.minecraftservices.com/\"," +
			"    \"TokenType\": \"JWT\"" +
			" }";
		JsonObject response = NetworkUtil.postRequest("https://xsts.auth.xboxlive.com/xsts/authorize", body, getHttpClient(), true).getAsJsonObject();
		return new AbstractMap.SimpleImmutableEntry<>(response.get("Token").getAsString(), response.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString());
	}

	public String authMC(String userhash, String xsts) throws IOException {
		return NetworkUtil.postRequest("https://api.minecraftservices.com/authentication/login_with_xbox",
			"{\"identityToken\": \"XBL3.0 x=" + userhash + ";" + xsts + "\"\n}",
			getHttpClient(), true).getAsJsonObject().get("access_token").getAsString();
	}

	public boolean checkOwnership(String accessToken) throws IOException {
		JsonObject response = NetworkUtil.request(RequestBuilder.get()
			.setUri("https://api.minecraftservices.com/entitlements/mcstore")
			.addHeader("Authorization", "Bearer " + accessToken).build(), getHttpClient(), true).getAsJsonObject();

		return response.get("items").getAsJsonArray().size() != 0;
	}

	public JsonObject getMCProfile(String accessToken) throws IOException {
		return NetworkUtil.request(RequestBuilder.get()
			.setUri("https://api.minecraftservices.com/minecraft/profile")
			.addHeader("Authorization", "Bearer " + accessToken).build(), getHttpClient(), true).getAsJsonObject();
	}

	private CloseableHttpClient getHttpClient() {
		return NetworkUtil.createHttpClient("Auth");
	}

	public Map.Entry<String, String> refreshToken(String token, String name) {
		try {
			logger.debug("refreshing auth code... ");
			List<NameValuePair> form = new ArrayList<>();
			form.add(new BasicNameValuePair("client_id", CLIENT_ID));
			form.add(new BasicNameValuePair("refresh_token", token));
			form.add(new BasicNameValuePair("scope", "XboxLive.signin XboxLive.offline_access"));
			form.add(new BasicNameValuePair("grant_type", "refresh_token"));
			RequestBuilder requestBuilder = RequestBuilder.post()
				.setUri("https://login.microsoftonline.com/consumers/oauth2/v2.0/token")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.setEntity(new UrlEncodedFormEntity(form))
				.addHeader("Accept", "application/json");

			JsonObject response = NetworkUtil.request(requestBuilder.build(), getHttpClient(), true).getAsJsonObject();
			String refreshToken = response.get("refresh_token").getAsString();

			logger.debug("getting xbl token... ");
			String xblToken = authXbl(response.get("access_token").getAsString());
			logger.debug("getting xsts token... ");
			Map.Entry<String, String> xsts = authXstsMC(xblToken);
			logger.debug("getting mc auth token...");
			String accessToken = authMC(xsts.getValue(), xsts.getKey());
			if (checkOwnership(accessToken)) {
				logger.info("Successfully refreshed token for " + name + "!");

				return new AbstractMap.SimpleImmutableEntry<>(accessToken, refreshToken);
			}
		} catch (Exception e) {
			logger.error("Failed to refresh Auth token! ", e);
		}
		return new AbstractMap.SimpleImmutableEntry<>(null, null);
	}
}
