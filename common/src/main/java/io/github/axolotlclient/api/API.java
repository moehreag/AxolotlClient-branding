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

package io.github.axolotlclient.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.axolotlclient.api.types.PkSystem;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.*;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import io.github.axolotlclient.util.translation.TranslationProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.PongMessage;
import jakarta.websocket.Session;
import lombok.Getter;
import org.glassfish.tyrus.container.grizzly.client.GrizzlyContainerProvider;
import org.glassfish.tyrus.core.CloseReasons;

public class API {

	@Getter
	private static API Instance;
	@Getter
	private final Logger logger;
	@Getter
	private final NotificationProvider notificationProvider;
	@Getter
	private final TranslationProvider translationProvider;
	private final StatusUpdateProvider statusUpdateProvider;
	@Getter
	private final Options apiOptions;
	private Session session;
	@Getter
	private String uuid;
	@Getter
	private User self;
	private Account account;
	private String token;

	public API(Logger logger, NotificationProvider notificationProvider, TranslationProvider translationProvider,
			   StatusUpdateProvider statusUpdateProvider, Options apiOptions) {
		this.logger = logger;
		this.notificationProvider = notificationProvider;
		this.translationProvider = translationProvider;
		this.statusUpdateProvider = statusUpdateProvider;
		this.apiOptions = apiOptions;
		Instance = this;
	}

	public void onOpen(Session channel) {
		this.session = channel;
		logger.debug("API connected!");
	}

	private void authenticate(){

		MojangAuth.Result result = MojangAuth.authenticate(account);

		if (result.getStatus() != MojangAuth.Status.SUCCESS){
			logger.error("Failed to authenticate with Mojang! Status: ", result.getStatus());
		}

		get(Request.builder().route(Request.Route.AUTHENTICATE)
			.query("username", account.getName())
			.query("server_id", result.getServerId())
			.build()).whenComplete((response, throwable) -> {

			if (throwable != null){
				logger.error("Failed to authenticate!", throwable);
				return;
			}
			if (response.isError()){
				logger.error("Failed to authenticate!", response.getError().getDescription());
			}

			token = (String) response.getBody().get("access_token");
			checkGateway();
			startStatusUpdateThread();
		});
	}

	private void checkGateway(){

		get(Request.builder().route(Request.Route.GATEWAY).build()).thenAccept(response -> {
			if (response.getStatus() == 101){
				createSession();
			}
		});

	}

	public CompletableFuture<Response> get(Request request) {
		return request(request, "GET");
	}

	public CompletableFuture<Response> patch(Request request) {
		return request(request, "PATCH");
	}

	public CompletableFuture<Response> post(Request request) {
		return request(request, "POST");
	}

	public CompletableFuture<Response> delete(Request request) {
		return request(request, "DELETE");
	}

	private CompletableFuture<Response> request(Request request, String method) {
		return request(getUrl(request), request.getBodyFields(), request.getRoute().isAuthenticated(), method);
	}

	private CompletableFuture<Response> request(String url, Map<String, ?> payload,
												boolean authenticated, String method) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
				connection.setRequestMethod(method);

				connection.addRequestProperty("Content-Type", "application/json");

				connection.addRequestProperty("Accept", "application/json");
				if (authenticated) {
					connection.addRequestProperty("Authorization", token);
				}
				connection.connect();

				if (!(payload == null || payload.isEmpty())) {
					PrintWriter writer = new PrintWriter(connection.getOutputStream());
					StringBuilder body = new StringBuilder("{");
					payload.forEach((s, s2) -> {
						if (body.charAt(body.length() - 1) == '{') {
							body.append(",");
						}
						body.append("\"").append(s).append("\": \"").append(s2).append("\"");
					});
					body.append("}");
					writer.println(body);
					writer.flush();
					writer.close();
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String body = reader.lines().collect(Collectors.joining("\n"));
				reader.close();

				int code = connection.getResponseCode();
				connection.disconnect();
				return Response.builder().body(body).status(code).build();
			} catch (IOException e) {
				handleError(e);
				return Response.CLIENT_ERROR;
			}
		});
	}

	private void handleError(Exception e) {
		logger.error("Error in API traffic: ", e);
	}

	private String getUrl(Request request) {
		StringBuilder url = new StringBuilder(Constants.API_URL);
		url.append(request.getRoute().getPath());
		for (String p : request.getPath()) {
			url.append("/").append(p);
		}
		if (!request.getQuery().isEmpty()) {
			url.append("?");
			request.getQuery().forEach((k, v) -> {
				if (url.charAt(url.length() - 1) == '?') {
					url.append("&");
				}
				url.append(k).append("=").append(v);
			});
		}
		return url.toString();
	}

	public void shutdown() {
		if (session != null && session.isOpen()) {
			try {
				session.close(CloseReasons.NORMAL_CLOSURE.getCloseReason());
			} catch (IOException e) {
				logger.warn("Failed to close session: ", e);
			}
			session = null;
		}
	}

	public boolean isSocketConnected() {
		return session != null && session.isOpen();
	}

	public void logDetailed(String message, Object... args) {
		if (apiOptions.detailedLogging.get()) {
			logger.debug("[DETAIL] " + message, args);
		}
	}

	public void onMessage(String message) {
		logDetailed("Handling response: ", message);
		// TODO handle socket messages
	}

	public void onError(Throwable throwable) {
		logger.error("Error while handling API traffic:", throwable);
	}

	public void onClose() {
		logDetailed("Session closed!");
		logDetailed("Restarting API session...");
		startup(account);
		logDetailed("Restarted API session!");
	}

	private void createSession() {
		if (!Constants.TESTING) {
			try {

				String apiUrl = Constants.SOCKET_URL;
				logDetailed("Connecting to " + apiUrl);
				session = GrizzlyContainerProvider.getWebSocketContainer().connectToServer(ClientEndpoint.class, URI.create(apiUrl));
			} catch (IOException | DeploymentException e) {
				logger.error("Failed to start API! ", e);
			}
		}
	}

	public void restart() {
		if (isSocketConnected()) {
			shutdown();
		}
		if (account != null) {
			startup(account);
		} else {
			apiOptions.enabled.set(false);
		}
	}

	public void startup(Account account) {
		this.uuid = account.getUuid();
		this.account = account;
		if (!Constants.ENABLED) {
			return;
		}

		if (account.isOffline()) {
			return;
		}

		ThreadExecuter.scheduleTask(() -> {
			if (apiOptions.enabled.get()) {
				switch (apiOptions.privacyAccepted.get()) {
					case "unset":
						apiOptions.openPrivacyNoteScreen.accept(v -> {
							if (v) startupAPI();
						});
						break;
					case "accepted":
						startupAPI();
						break;
					default:
						break;
				}
			}
		});
	}

	void startupAPI() {
		if (!isSocketConnected()) {
			logger.debug("Creating self user..");
			self = new User(this.account.getName(), this.uuid, Status.UNKNOWN, PkSystem.fromToken(apiOptions.pkToken.get()).join());

			if (Constants.TESTING) {
				return;
			}

			logger.debug("Starting API...");
			ThreadExecuter.scheduleTask(this::authenticate);
		} else {
			logger.warn("API is already running!");
		}
	}

	private void startStatusUpdateThread() {
		statusUpdateProvider.initialize();
		new Thread("Status Update Thread") {
			@Override
			public void run() {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ignored) {
				}
				while (API.getInstance().isSocketConnected()) {
					Request request = statusUpdateProvider.getStatus();
					if (request != null) {
						post(request);
					}
					try {
						//noinspection BusyWait
						Thread.sleep(Constants.STATUS_UPDATE_DELAY * 1000);
					} catch (InterruptedException ignored) {

					}
				}
			}
		}.start();
	}

	public String sanitizeUUID(String uuid) {
		if (uuid.contains("-")) {
			return validateUUID(uuid.replace("-", ""));
		}
		return validateUUID(uuid);
	}

	private String validateUUID(String uuid) {
		if (uuid.length() != 32) {
			throw new IllegalArgumentException("Not a valid UUID (undashed): " + uuid);
		}
		return uuid;
	}

	public void onPong(PongMessage pong) {
		try {
			session.getBasicRemote().sendPong(pong.getApplicationData());
		} catch (IOException e) {
			onError(e);
		}
	}
}
