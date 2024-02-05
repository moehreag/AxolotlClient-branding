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

import java.util.Arrays;

import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import io.github.axolotlclient.util.translation.TranslationProvider;
import lombok.Getter;

public class TestClientThings extends Options implements Logger, StatusUpdateProvider, TranslationProvider, NotificationProvider {
	@Getter
	private static final TestClientThings instance = new TestClientThings();

	private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("TestClient");

	@Override
	public void initialize() {

	}

	@Override
	public Request getStatus() {
		return new RequestOld(RequestOld.Type.STATUS_UPDATE, "Playing around with the API!");
	}

	@Override
	public void info(String msg, Object... args) {
		logger.info("Client: "+ msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void warn(String msg, Object... args) {
		logger.warning("Client: "+msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void error(String msg, Object... args) {
		logger.severe("Client: "+msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void debug(String msg, Object... args) {
		logger.info("Client: "+"[DEBUG] " + msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void addStatus(String titleKey, String descKey, Object... args) {
		info(titleKey + ":", String.format(descKey, args));
	}

	@Override
	public String translate(String key, Object... args) {
		return key;
	}
}
