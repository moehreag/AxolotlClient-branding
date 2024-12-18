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

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.Logger;
import lombok.Getter;

public class AxolotlClientCommon {
	@Getter
	private static AxolotlClientCommon instance;
	@Getter
	public static final String VERSION = readVersion();
	@Getter
	private final Logger logger;
	private final Supplier<ConfigManager> manager;

	public AxolotlClientCommon(Logger logger, Supplier<ConfigManager> manager) {
		instance = this;
		this.logger = logger;
		this.manager = manager;
	}

	@SuppressWarnings("unchecked")
	private static String readVersion() {
		try {
			return (String) ((Map<Object, Object>) GsonHelper.read(API.class.getResourceAsStream("/fabric.mod.json"))).get("version");
		} catch (IOException ignored) {
			return "(unknown)";
		}
	}

	public void saveConfig() {
		manager.get().save();
	}
}
