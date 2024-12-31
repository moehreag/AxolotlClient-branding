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

package io.github.axolotlclient.modules.mcci;

import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.requests.StatusUpdate;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.events.Events;

public class MccIslandMods extends AbstractModule {

	private static final MccIslandMods INSTANCE = new MccIslandMods();

	public static MccIslandMods getInstance() {
		return INSTANCE;
	}

	private final NoxesiumIntegration noxesium = new NoxesiumIntegration();

	@Override
	public void init() {
		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(event -> {
			event.setCancelled(MccIslandLocation.waitingForResponse(event.getOriginalMessage()));
		});
		noxesium.init();
	}

	public Request getMccIStatus() {
		if (NoxesiumIntegration.NOXESIUM_INSTALLED) {
			Request nox = noxesium.getCurrentStatus();
			if (nox != null) {
				return nox;
			}
		}
		return StatusUpdate.inGame(StatusUpdate.SupportedServer.MCC_ISLAND, MccIslandGameType.fromLocation(MccIslandLocation.get().join()).getName(), "", "");
	}
}
