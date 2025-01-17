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

package io.github.axolotlclient.api;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.api.handlers.*;
import io.github.axolotlclient.api.requests.AccountSettingsRequest;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.api.types.*;
import io.github.axolotlclient.api.util.MojangAuth;
import io.github.axolotlclient.api.util.SocketMessageHandler;
import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.api.util.TimestampParser;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import io.github.axolotlclient.util.translation.TranslationProvider;
import lombok.Getter;
import lombok.Setter;

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
	private final Collection<SocketMessageHandler> handlers;
	private WebSocket socket;
	@Getter
	private User self;
	private Account account;
	private String token;
	@Getter
	@Setter
	private AccountSettings settings;
	private HttpClient client;
	private CompletableFuture<?> restartingFuture;
	private static final List<Runnable> afterStartupListeners = new ArrayList<>();

	public API(Logger logger, TranslationProvider translationProvider,
			   StatusUpdateProvider statusUpdateProvider, Options apiOptions) {
		if (Instance != null) {
			throw new IllegalStateException("API may only be instantiated once!");
		}
		this.logger = logger;
		this.notificationProvider = AxolotlClientCommon.getInstance().getNotificationProvider();
		this.translationProvider = translationProvider;
		this.statusUpdateProvider = statusUpdateProvider;
		this.apiOptions = apiOptions;
		handlers = new HashSet<>();
		handlers.add(ChatHandler.getInstance());
		handlers.add(new FriendRequestHandler());
		handlers.add(new FriendRequestReactionHandler());
		handlers.add(new StatusUpdateHandler());
		handlers.add(new ChannelInviteHandler());
		Instance = this;
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
	}

	public static void addStartupListener(Runnable listener) {
		afterStartupListeners.add(listener);
	}


	public void onOpen(WebSocket channel) {
		this.socket = channel;
		logger.debug("API connected!");
		afterStartupListeners.forEach(Runnable::run);
	}

	private void authenticate() {
		//if (client != null) {
		// We have to rely on the gc to collect previous client objects as close() was only implemented in java 21.
		// However, we are currently compiling against java 17.
		//client.close();
		//}

		try {
			if (!GlobalDataRequest.get(true).get(1, TimeUnit.MINUTES).success()) {
				logger.warn("Not trying to start API as it couldn't be reached!");
				scheduleRestart(false);
				return;
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.warn("Not trying to start API as it couldn't be reached within the timeout of 1 minute!");
			scheduleRestart(false);
			return;
		}

		logDetailed("Authenticating with Mojang...");

		MojangAuth.Result result = MojangAuth.authenticate(account);

		if (result.getStatus() != MojangAuth.Status.SUCCESS) {
			logger.error("Failed to authenticate with Mojang! Status: ", result.getStatus());
			return;
		}

		logDetailed("Requesting authentication from backend...");

		get(Request.Route.AUTHENTICATE.builder()
			.query("username", account.getName())
			.query("server_id", result.getServerId())
			.build()).whenComplete((response, throwable) -> {

			if (throwable != null) {
				logger.error("Failed to authenticate!", throwable);
				return;
			}
			if (response.isError()) {
				logger.error("Failed to authenticate!", response.getError().description());
				return;
			}

			token = response.getBody("access_token");
			logDetailed("Obtained token!");
			CompletableFuture.allOf(get(Request.Route.ACCOUNT.builder().build())
					.thenAccept(r -> {
						self = new User(sanitizeUUID(r.getBody("uuid")),
							r.getBody("username"), Relation.NONE,
							r.getBody("registered", TimestampParser::parse),
							Status.UNKNOWN,
							r.ifBodyHas("previous_usernames", () -> {
								List<Map<?, ?>> previous = r.getBody("previous_usernames");
								return previous.stream().map(m -> new User.OldUsername((String) m.get("username"), (boolean) m.get("public")))
									.collect(Collectors.toList());
							}));
						self.setSystem(PkSystem.fromToken(apiOptions.pkToken.get()).join());
						logDetailed("Created self user!");
					}),
				AccountSettingsRequest.get().thenAccept(r -> {
					apiOptions.retainUsernames.set(r.retainUsernames());
					apiOptions.showActivity.set(r.showActivity());
					apiOptions.showLastOnline.set(r.showLastOnline());
					apiOptions.showRegistered.set(r.showRegistered());
					apiOptions.allowFriendsImageAccess.set(r.allowFriendsImageAccess());
				})).thenRun(() -> logDetailed("completed data requests")).join();
			createSession();
			startStatusUpdateThread();
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
		if (!getApiOptions().enabled.get()) {
			return CompletableFuture.completedFuture(Response.builder().status(0).body("{\"description\":\"Integration disabled!\"}").build());
		}
		if (!getApiOptions().privacyAccepted.get().isAccepted()) {
			return CompletableFuture.completedFuture(Response.CLIENT_ERROR);
		}
		if (request.requiresAuthentication() && !isAuthenticated()) {
			logger.debug("Tried to request {} {} without authentication, but this request requires it!", method, request);
			return CompletableFuture.completedFuture(Response.CLIENT_ERROR);
		}
		URI route = getUrl(request);
		return request(route, request.bodyFields(), request.rawBody(), method, request.headers());
	}

	private CompletableFuture<Response> request(URI url, Map<String, ?> payload, byte[] rawBody, String method, Map<String, String> headers) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				logDetailed("Starting request to " + method + " " + url);

				HttpRequest.Builder builder = HttpRequest.newBuilder(url)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json");

				if (token != null) {
					builder.header("Authorization", token);
				}

				if (headers != null) {
					headers.forEach(builder::header);
				}

				if (rawBody != null) {
					builder.method(method, HttpRequest.BodyPublishers.ofByteArray(rawBody));
				} else if (!(payload == null || payload.isEmpty())) {
					StringBuilder body = new StringBuilder();
					GsonHelper.GSON.toJson(payload, body);
					logDetailed("Sending payload: \n" + body);
					builder.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));
				} else {
					builder.method(method, HttpRequest.BodyPublishers.noBody());
				}
				if (client == null) {
					client = NetworkUtil.createHttpClient("API");
				}

				HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

				String body = response.body();

				int code = response.statusCode();
				if (apiOptions.detailedLogging.get()) {
					if (!url.getPath().endsWith(Request.Route.AUTHENTICATE.getPath())) {
						logDetailed("Response: code: " + code + " body: " + body);
					} else {
						logDetailed("Response: code: " + code + " body: " + String.valueOf(body).replaceAll("(\"access_token\": ?\")[^\"]+(\")", "$1[token redacted]$2"));
					}
				}
				return Response.builder().body(body).status(code).headers(response.headers().map()).build();
			} catch (ConnectException | HttpTimeoutException e) {
				logger.warn("Backend unreachable!");
				return Response.CLIENT_ERROR;
			} catch (Exception e) {
				onError(e);
				return Response.CLIENT_ERROR;
			}
		}, ThreadExecuter.service());
	}

	URI getUrl(Request request) {
		StringBuilder url = new StringBuilder(Constants.API_URL.endsWith("/") ? Constants.API_URL : Constants.API_URL + "/");
		url.append(request.route().getPath());
		if (request.path() != null) {
			for (String p : request.path()) {
				url.append("/").append(p);
			}
		}
		if (request.query() != null && !request.query().isEmpty()) {
			url.append("?");
			request.query().forEach((v) -> {
				if (url.charAt(url.length() - 1) != '?') {
					url.append("&");
				}
				url.append(v);
			});
		}
		return URI.create(url.toString());
	}

	public void shutdown() {
		if (restartingFuture != null) {
			restartingFuture.cancel(true);
			restartingFuture = null;
		}
		if (isAuthenticated()) {
			logger.debug("Shutting down API");
			if (isSocketConnected()) {
				socket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown");
				socket = null;
			}
			// We have to rely on the gc to collect previous client objects as close() was only implemented in java 21.
			// However, we are currently compiling against java 17.
			//client.close();
			token = null;
		}
		client = null;
	}

	public boolean isSocketConnected() {
		return socket != null;
	}

	public boolean isAuthenticated() {
		return token != null;
	}

	public int getIndicatorColor() {
		int color = isAuthenticated() ? 0xFFFFCC00 : 0xFFFF0000;
		if (isSocketConnected()) {
			color = 0xFF009000;
		}
		return color;
	}

	public void logDetailed(String message, Object... args) {
		if (apiOptions.detailedLogging.get()) {
			logger.debug("[DETAIL] " + message, args);
		}
	}

	public void onMessage(String message) {
		logDetailed("Handling socket message: {}", message);

		Response res = Response.builder().status(200).body(message).build();
		String target = res.getBody("target");
		boolean handled = false;
		for (SocketMessageHandler handler : handlers) {
			if (handler.isApplicable(target)) {
				handler.handle(res);
				handled = true;
			}
		}
		if (!handled) {
			logger.warn("Unhandled socket message target {}! This may be caused by using an outdated client.", target);
		}
	}

	public void onError(Throwable throwable) {
		logger.error("Error while handling API traffic:", throwable);
	}

	public void onClose(int statusCode, String reason) {
		logDetailed("Session closed! code: " + statusCode + " reason: " + reason);
		int[] error_codes = new int[]{
			1011,
			1007,
			1014
		};
		if (Arrays.stream(error_codes).anyMatch(i -> i == statusCode) && apiOptions.enabled.get()) {
			scheduleRestart(true);
		}
	}

	private void scheduleRestart(boolean immediate) {
		if (restartingFuture != null) {
			restartingFuture.cancel(true);
		}
		logger.info("Trying restart in " + (immediate ? "10 seconds" : "5 minutes."));
		restartingFuture = CompletableFuture.runAsync(() -> {
			logDetailed("Restarting API session...");
			startup(account);
		}, immediate ? CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS, ThreadExecuter.service()) :
			CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES, ThreadExecuter.service()));
	}

	private void createSession() {
		if (!Constants.TESTING) {
			try {
				logDetailed("Connecting to websocket..");
				URI gateway = Request.Route.GATEWAY.create().resolve();
				String uri = (gateway.getScheme().endsWith("s") ? "wss" : "ws") + gateway.toString().substring(gateway.getScheme().length());
				socket = client.newWebSocketBuilder().header("Authorization", token)
					.buildAsync(URI.create(uri), new ClientEndpoint()).join();
				logDetailed("Socket connected");
			} catch (Exception e) {
				logger.error("Failed to start Socket! ", e);
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
					case UNSET:
						apiOptions.openPrivacyNoteScreen.accept(v -> {
							if (v) startupAPI();
						});
						break;
					case ACCEPTED:
						startupAPI();
						break;
					default:
						break;
				}
			}
		});
	}

	private void startupAPI() {
		if (!isSocketConnected()) {

			if (Constants.TESTING) {
				return;
			}
			logger.info("Starting API...");
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
				while (API.getInstance().isAuthenticated()) {
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
}
