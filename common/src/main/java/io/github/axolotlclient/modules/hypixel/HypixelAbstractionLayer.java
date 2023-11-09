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

package io.github.axolotlclient.modules.hypixel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Based on Osmium by Intro-Dev
 * (<a href="https://github.com/Intro-Dev/Osmium">Github</a>)
 *
 * <p>Original License: CC0-1.0</p>
 *
 * @implNote Provides a layer between the hypixel api and the client to obtain information with minimal api calls
 */

@UtilityClass
public class HypixelAbstractionLayer {

	private static final Map<String, Map<RequestDataType, Object>> cachedPlayerData = new HashMap<>();
	private static final Map<String, Integer> tempValues = new HashMap<>();

	public static int getPlayerLevel(String uuid, String mode) {
		int value = -1;
		if (Objects.equals(mode, LevelHeadMode.NETWORK.toString())) {
			value = getLevel(uuid, RequestDataType.NETWORK_LEVEL);
		} else if (Objects.equals(mode, LevelHeadMode.BEDWARS.toString())) {
			value = getLevel(uuid, RequestDataType.BEDWARS_LEVEL);
		} else if (Objects.equals(mode, LevelHeadMode.SKYWARS.toString())) {
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
		return cache(uuid, type, () ->
			getHypixelApiData(uuid, type).handleAsync((buf, throwable) -> {
			if (throwable != null) {
				APIError.display(throwable);
				return -1;
			}
			return buf.getInt(0x09);
		}).getNow(-1));
	}

	public int getBedwarsLevel(String uuid){
		return getLevel(uuid, RequestDataType.BEDWARS_LEVEL);
	}

	public BedwarsData getBedwarsData(String playerUuid) {
		return cache(playerUuid, RequestDataType.BEDWARS_DATA, () ->
			getHypixelApiData(playerUuid, RequestDataType.BEDWARS_DATA).handleAsync(((buf, throwable) -> {
			if (throwable != null) {
				APIError.display(throwable);
				return BedwarsData.EMPTY;
			}
			ByteBuf data = buf.slice(0x09, buf.readableBytes() - 0x09);
			return BufferUtil.unwrap(data, BedwarsData.class);
		})).getNow(BedwarsData.EMPTY));
	}

	@SuppressWarnings("unchecked")
	private <T> T cache(String uuid, RequestDataType type, Supplier<T> dataSupplier){
		return (T) cachedPlayerData.computeIfAbsent(uuid, s -> new HashMap<>()).computeIfAbsent(type, t -> dataSupplier);
	}

	private CompletableFuture<ByteBuf> getHypixelApiData(String uuid, RequestDataType type) {
		return API.getInstance().send(new Request(Request.Type.GET_HYPIXEL_API_DATA, new Request.Data(uuid).add(type.getId())));
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
		NETWORK_LEVEL(0x1),
		BEDWARS_LEVEL(0x2),
		SKYWARS_EXPERIENCE(0x3),
		BEDWARS_DATA(0x4);
		private final int id;
	}
}
