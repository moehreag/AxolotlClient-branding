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
import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.LoggerFactory;

public class LoggerImpl implements Logger {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("AxolotlClient");

	private static final String prefix = QuiltLoader.isDevelopmentEnvironment() ? "" : "(AxolotlClient) ";

	public void info(String msg, Object... args) {
		LOGGER.info(prefix + msg, args);
	}

	public void warn(String msg, Object... args) {
		LOGGER.warn(prefix + msg, args);
	}

	public void error(String msg, Object... args) {
		LOGGER.error(prefix + msg, args);
	}

	public void debug(String msg, Object... args) {
		if (AxolotlClient.CONFIG.debugLogOutput.get()) {
			LOGGER.info(prefix + "[DEBUG] " + msg, args);
		}
	}
}
