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

package io.github.axolotlclient.modules.hypixel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HypixelAbstractionLayer {

	private final Map<String, Map<RequestDataType, Object>> cachedPlayerData = new HashMap<>();
	private final Map<String, Map<RequestDataType, CompletableFuture<Optional<Object>>>> cachedRequests = new HashMap<>();
	private final Map<String, Integer> tempValues = new HashMap<>();
	private Instant ratelimitReset = Instant.now();

	public int getPlayerLevel(String uuid, LevelHeadMode mode) {
		int value = -1;
		if (Objects.equals(mode, LevelHeadMode.NETWORK)) {
			value = getLevel(uuid, RequestDataType.NETWORK_LEVEL);
		} else if (Objects.equals(mode, LevelHeadMode.BEDWARS)) {
			value = getLevel(uuid, RequestDataType.BEDWARS_LEVEL);
		} else if (Objects.equals(mode, LevelHeadMode.SKYWARS)) {
			int exp = getLevel(uuid, RequestDataType.SKYWARS_EXPERIENCE);
			if (exp != -1) {
				value = Math.round(ExpCalculator.getLevelForExp(exp));
			}
		}
		if (value > -1) {
			tempValues.remove(uuid);
			return value;
		}
		return tempValues.computeIfAbsent(uuid, s -> (int) (new Random().nextGaussian() * 30 + 150));
	}

	private int getLevel(String uuid, RequestDataType type) {
		return cache(uuid, type, res -> {
			Number lvl = res.getBody(type.getId());
			return lvl.intValue();
		}, -1);
	}

	public int getBedwarsLevel(String uuid) {
		return getLevel(uuid, RequestDataType.BEDWARS_LEVEL);
	}

	public BedwarsData getBedwarsData(String playerUuid) {
		return cache(playerUuid, RequestDataType.BEDWARS_DATA, res -> new BedwarsData(
			res.getBody("final_kills_bedwars"),
			res.getBody("final_deaths_bedwars"),
			res.getBody("beds_broken_bedwars"),
			res.getBody("deaths_bedwars"),
			res.getBody("kills_bedwars"),
			res.getBody("losses_bedwars"),
			res.getBody("wins_bedwars"),
			res.getBody("winstreak")
		), BedwarsData.EMPTY);
	}

	@SuppressWarnings("unchecked")
	private <T> T cache(String uuid, RequestDataType type, Function<Response, T> func, T absent) {
		uuid = API.getInstance().sanitizeUUID(uuid);
		if (!API.getInstance().isAuthenticated()) {
			return absent;
		}
		Map<RequestDataType, Object> map = cachedPlayerData.computeIfAbsent(uuid, s -> new HashMap<>());
		Map<RequestDataType, CompletableFuture<Optional<Object>>> requests = cachedRequests.computeIfAbsent(uuid, s -> new HashMap<>());
		if (map.containsKey(type)) {
			return (T) map.get(type);
		} else  {
			if (requests.containsKey(type)) {
				var request = requests.get(type);
				if (request.isDone()) {
					requests.remove(type);
					Optional<T> option = (Optional<T>) request.getNow(Optional.empty());
					if (option.isPresent()) {
						T value = option.get();
						map.put(type, value);
						return value;
					}
				}
			} else {
				CompletableFuture<Optional<Object>> request;
				synchronized (tempValues) {
					if (Instant.now().isBefore(ratelimitReset)) {
						return absent;
					}

					request = getHypixelApiData(uuid, type).thenApply(res -> {
						if (res.getStatus() == 429) {
							ratelimitReset = Instant.now().plus(res.firstHeader("RateLimit-Reset").map(Long::parseLong).orElse(2L), ChronoUnit.SECONDS);
						} else {
							ratelimitReset = Instant.now().plus(100, ChronoUnit.MILLIS);
						}
						if (res.isError()) {
							return Optional.empty();
						}
						return Optional.ofNullable(func.apply(res));
					});
				}
				if (request.isDone()) {
					Optional<T> option = (Optional<T>) request.getNow(Optional.empty());
					if (option.isPresent()) {
						T value = option.get();
						map.put(type, value);
						return value;
					}
				} else {
					requests.put(type, request);
				}
			}
		}
		return absent;
	}

	private CompletableFuture<Response> getHypixelApiData(String uuid, RequestDataType type) {
		return API.getInstance().get(Request.Route.HYPIXEL.builder().field("request_type", type.getId()).field("target_player", uuid).build());
	}

	public void clearPlayerData() {
		cachedPlayerData.clear();
	}

	public void handleDisconnectEvents(UUID uuid) {
		freePlayerData(uuid.toString());
	}

	private void freePlayerData(String uuid) {
		cachedPlayerData.remove(uuid);
	}

	@AllArgsConstructor
	@Getter
	private enum RequestDataType {
		NETWORK_LEVEL("network_level"),
		BEDWARS_LEVEL("bedwars_level"),
		SKYWARS_EXPERIENCE("skywars_experience"),
		BEDWARS_DATA("bedwars_data");
		private final String id;
	}
}
