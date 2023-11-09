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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.github.axolotlclient.api.handlers.*;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.api.types.Status;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.*;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import io.github.axolotlclient.util.translation.TranslationProvider;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Getter;

public class API {

	@Getter
	private static API Instance;
	private final HashMap<Integer, CompletableFuture<ByteBuf>> requests = new HashMap<>();
	private final Set<RequestHandler> handlers = new HashSet<>();
	@Getter
	private final Logger logger;
	@Getter
	private final NotificationProvider notificationProvider;
	@Getter
	private final TranslationProvider translationProvider;
	private final StatusUpdateProvider statusUpdateProvider;
	@Getter
	private final Options apiOptions;
	private Channel channel;
	@Getter
	private String uuid;
	@Getter
	private User self;
	private Account account;

	public API(Logger logger, NotificationProvider notificationProvider, TranslationProvider translationProvider,
			   StatusUpdateProvider statusUpdateProvider, Options apiOptions) {
		this.logger = logger;
		this.notificationProvider = notificationProvider;
		this.translationProvider = translationProvider;
		this.statusUpdateProvider = statusUpdateProvider;
		this.apiOptions = apiOptions;
		Instance = this;
		addHandler(new FriendRequestHandler());
		addHandler(new FriendRequestReactionHandler());
		addHandler(FriendHandler.getInstance());
		addHandler(new StatusUpdateHandler());
		addHandler(ChatHandler.getInstance());

		BufferUtil.registerSerializer(Instant.class, new InstantSerializer());
		BufferUtil.registerSerializer(ChatMessage.class, new ChatMessageSerializer());
		BufferUtil.registerSerializer(ChatMessage.Type.class, new ChatMessageSerializer.ChatMessageTypeSerializer());
	}

	public void addHandler(RequestHandler handler) {
		handlers.add(handler);
	}

	public void onOpen(Channel channel) {
		this.channel = channel;
		logger.debug("API connected!");
		sendHandshake(account);
	}

	private void sendHandshake(Account account) {
		logger.debug("Starting Handshake");
		logger.debug("Authenticating with Mojang");

		AtomicBoolean mojangAuthSuccessful = new AtomicBoolean();
		AtomicReference<String> serverId = new AtomicReference<>();

		send(new Request(Request.Type.GET_PUBLIC_KEY)).whenCompleteAsync((buf, t) -> {
			MojangAuth.Result result = MojangAuth.authenticate(account, BufferUtil.toArray(buf.slice(0x09, buf.readableBytes() - 9)));
			if (result.getStatus() != MojangAuth.Status.SUCCESS) {
				logger.error("Authentication with Mojang failed, aborting!");
				shutdown();
			} else {
				mojangAuthSuccessful.set(true);
				serverId.set(result.getServerId());
			}
		});

		if (mojangAuthSuccessful.get()) {
			Request request = new Request(Request.Type.HANDSHAKE, account.getUuid(), serverId.get(), account.getName());
			send(request).whenCompleteAsync((object, t) -> {
				if (t != null) {
					logger.error("Handshake failed, closing API!");
					if (apiOptions.detailedLogging.get()) {
						notificationProvider.addStatus("api.error.handshake", t.getMessage());
					}
					shutdown();
				} else if (object.getByte(0x09) == 0) {

					logger.debug("Handshake successful!");
					if (apiOptions.detailedLogging.get()) {
						notificationProvider.addStatus("api.success.handshake", "api.success.handshake.desc");
					}
				}
			});
		}
	}

	public boolean requestFailed(ByteBuf object) {
		try {
			return object.getByte(0x03) == Request.Type.ERROR.getType();
		} catch (IndexOutOfBoundsException e) {
			return true;
		}
	}

	public void shutdown() {
		if (channel != null && channel.isOpen()) {
			ClientEndpoint.shutdown();
			channel = null;
		}
	}

	public CompletableFuture<ByteBuf> send(Request request) {
		CompletableFuture<ByteBuf> future = new CompletableFuture<>();
		if (isConnected()) {
				requests.put(request.getId(), future);
				ThreadExecuter.scheduleTask(() -> {
					ByteBuf buf = request.getData();
					if (apiOptions.detailedLogging.get()) {
						logDetailed("Sending Request: " + buf.toString(StandardCharsets.UTF_8));
					}
					channel.writeAndFlush(buf);
					buf.release();
				});
		} else {
			logger.warn("Not sending request because API is closed: " + request);
		}
		return future;
	}

	public boolean isConnected() {
		return channel != null && channel.isOpen();
	}

	public void logDetailed(String message, Object... args) {
		if (apiOptions.detailedLogging.get()) {
			logger.debug("[DETAIL] " + message, args);
		}
	}

	public void onMessage(ByteBuf message) {
		if (apiOptions.detailedLogging.get()) {
			logDetailed("Handling response: " + message.toString(StandardCharsets.UTF_8));
		}
		handleResponse(message);
	}

	private void handleResponse(ByteBuf response) {
		try {
			Integer id = null;
			try {
				id = response.getInt(0x05);
			} catch (IndexOutOfBoundsException ignored) {
			}


			APIError error;

			if(requestFailed(response)){
				error = new APIError(response);
			} else {
				error = null;
			}

			if (requests.containsKey(id)) {
				if(error != null){
					requests.get(id).completeExceptionally(error);
				} else {
					requests.get(id).complete(response);
				}
				requests.remove(id);
			} else if (id == null || id == 0) {
				int type = response.getByte(0x03);
				handlers.stream().filter(handler -> handler.isApplicable(type)).forEach(handler ->
					handler.handle(response, error));
			} else {
				logger.error("Unknown response: " + response.toString(StandardCharsets.UTF_8));
			}

		} catch (RuntimeException e) {
			e.printStackTrace();
			logger.error("Invalid response: " + response);
		}

		response.release();
	}

	public void onError(Throwable throwable) {
		logger.error("Error while handling API traffic:", throwable);
	}

	public void onClose() {
		logDetailed("Session closed!");
		logDetailed("Restarting API session...");
		createSession();
		logDetailed("Restarted API session!");
	}

	private void createSession() {
		if(!Constants.TESTING) {
			new ClientEndpoint().run(Constants.API_URL, Constants.PORT);
		}
	}

	public void restart() {
		if (isConnected()) {
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

		if(account.isOffline()){
			return;
		}

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
	}

	void startupAPI() {
		if (!isConnected()) {
			self = new User(this.uuid, Status.UNKNOWN);

			if(Constants.TESTING){
				return;
			}

			logger.debug("Starting API...");
			ThreadExecuter.scheduleTask(() -> {
				createSession();

				while (channel == null) {
					try {
						//noinspection BusyWait
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

				new Thread("Status Update Thread") {
					@Override
					public void run() {
						try {
							Thread.sleep(50);
						} catch (InterruptedException ignored) {
						}
						while (API.getInstance().isConnected()) {
							Request statusUpdate = statusUpdateProvider.getStatus();
							if (statusUpdate != null) {
								send(statusUpdate);
							}
							try {
								//noinspection BusyWait
								Thread.sleep(Constants.STATUS_UPDATE_DELAY * 1000);
							} catch (InterruptedException ignored) {

							}
						}
					}
				}.start();
			});
		} else {
			logger.warn("API is already running!");
		}
	}

	public String sanitizeUUID(String uuid) {
		if (uuid.contains("-")) {
			return uuid.replace("-", "");
		}
		return uuid;
	}
}
