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

import io.github.axolotlclient.AxolotlClient;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

public class LoggerImpl implements Logger {

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("AxolotlClient");

	private static final String modId = FabricLoader.getInstance().isDevelopmentEnvironment() ? "" : "(AxolotlClient) ";

	public void warn(String message, Object... args) {
		LOGGER.warn(modId + message, args);
	}

	public void error(String message, Object... args) {
		try {
			LOGGER.error(modId + message, args);
		} catch (Exception e) {
			LOGGER.warn(modId + "[ERROR]" + message, args);
		}
	}

	public void info(String message, Object... args) {
		LOGGER.info(modId + message, args);
	}

	public void debug(String message, Object... args) {
		if (AxolotlClient.CONFIG.debugLogOutput.get()) {
			info(modId + "[DEBUG] " + message, args);
		}
	}
}
