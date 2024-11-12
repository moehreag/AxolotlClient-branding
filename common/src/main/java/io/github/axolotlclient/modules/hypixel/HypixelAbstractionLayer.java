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
import java.util.function.Supplier;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HypixelAbstractionLayer {

	private static final Map<String, Map<RequestDataType, Object>> cachedPlayerData = new HashMap<>();
	private static final Map<String, Integer> tempValues = new HashMap<>();
	private static Instant ratelimitReset = Instant.now();

	public static int getPlayerLevel(String uuid, LevelHeadMode mode) {
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
		return cache(uuid, type, () -> {
			synchronized (tempValues) {
				if (ratelimitReset.isAfter(Instant.now())) {
					return -1;
				}
			}
			return getHypixelApiData(uuid, type).thenApply(res -> {
				if (res.getStatus() == 429) {
					int header = res.firstHeader("RateLimit-Reset").map(Integer::parseInt).orElse(2);
					ratelimitReset = Instant.now().plus(header, ChronoUnit.SECONDS);
				}
				if (res.isError()) {
					return -1;
				}
				Number lvl = res.getBody(type.getId());
				return lvl.intValue();
			}).join();
		});
	}

	public int getBedwarsLevel(String uuid) {
		return getLevel(uuid, RequestDataType.BEDWARS_LEVEL);
	}

	public BedwarsData getBedwarsData(String playerUuid) {

		return cache(playerUuid, RequestDataType.BEDWARS_DATA, () -> {
			synchronized (tempValues) {
				if (ratelimitReset.isAfter(Instant.now())) {
					return BedwarsData.EMPTY;
				}
			}
			return getHypixelApiData(playerUuid, RequestDataType.BEDWARS_DATA).thenApply(res -> {
				if (res.getStatus() == 429) {
					ratelimitReset = Instant.now().plus(res.firstHeader("RateLimit-Reset").map(Integer::parseInt).orElse(2), ChronoUnit.SECONDS);
				}
				if (res.isError()) {
					return BedwarsData.EMPTY;
				}
				return new BedwarsData(
					res.getBody("final_kills_bedwars"),
					res.getBody("final_deaths_bedwars"),
					res.getBody("beds_broken_bedwars"),
					res.getBody("deaths_bedwars"),
					res.getBody("kills_bedwars"),
					res.getBody("losses_bedwars"),
					res.getBody("wins_bedwars"),
					res.getBody("winstreak")
				);
			}).join();
		});
	}

	@SuppressWarnings("unchecked")
	private <T> T cache(String uuid, RequestDataType type, Supplier<T> dataSupplier) {
		Map<RequestDataType, Object> map = cachedPlayerData.computeIfAbsent(uuid, s -> new HashMap<>());
		if (map.containsKey(type)) {
			if (map.get(type).equals(-1) || map.get(type).equals(BedwarsData.EMPTY)) {
				T data = dataSupplier.get();
				map.put(type, data);
				return data;
			}
		}
		return (T) map.computeIfAbsent(type, t -> dataSupplier.get());
	}

	private CompletableFuture<Response> getHypixelApiData(String uuid, RequestDataType type) {
		return API.getInstance().get(Request.Route.HYPIXEL.builder().field("request_type", type.getId()).field("target_player", uuid).build());
	}

	public static void clearPlayerData() {
		cachedPlayerData.clear();
	}

	public static void handleDisconnectEvents(UUID uuid) {
		freePlayerData(uuid.toString());
	}

	private static void freePlayerData(String uuid) {
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
