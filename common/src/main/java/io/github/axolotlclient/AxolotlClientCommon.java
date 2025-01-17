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

package io.github.axolotlclient;

import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

public class AxolotlClientCommon {
	@Getter
	private static AxolotlClientCommon instance;
	@Getter
	public static final String VERSION = readVersion();
	@Getter
	public static final String GAME_VERSION = readGameVersion();

	@Getter
	private final Logger logger;
	@Getter
	private final NotificationProvider notificationProvider;

	private final Supplier<ConfigManager> manager;
	public DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonOptions.datetimeFormat.get());

	public AxolotlClientCommon(Logger logger, NotificationProvider notifications, Supplier<ConfigManager> manager) {
		instance = this;
		this.logger = logger;
		this.notificationProvider = notifications;
		this.manager = manager;
	}

	private static String readVersion() {
		return FabricLoader.getInstance().getModContainer("axolotlclient-common").orElseThrow().getMetadata().getVersion().getFriendlyString();
	}

	private static String readGameVersion() {
		return FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
	}

	public static String getUAVersionString() {
		return "AxolotlClient/"+VERSION+" (Minecraft "+GAME_VERSION+")";
	}

	public void saveConfig() {
		manager.get().save();
	}
}
