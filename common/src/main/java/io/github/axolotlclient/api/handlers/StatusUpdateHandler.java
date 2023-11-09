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

package io.github.axolotlclient.api.handlers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIError;
import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.requests.StatusUpdate;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.RequestHandler;
import io.netty.buffer.ByteBuf;

public class StatusUpdateHandler implements RequestHandler {
	@Override
	public boolean isApplicable(int packetType) {
		return packetType == Request.Type.STATUS_UPDATE.getType() && API.getInstance().getApiOptions().statusUpdateNotifs.get();
	}

	@Override
	public void handle(ByteBuf object, APIError error) {
		String uuid = getString(object, 0x09, 16);
		AtomicReference<User> user = new AtomicReference<>();
		FriendHandler.getInstance().getFriends().whenCompleteAsync((list, t) -> user.set(list.stream().filter(u -> u.getUuid().equals(uuid)).collect(Collectors.toList()).get(0)));
		StatusUpdate.Type type = StatusUpdate.Type.fromCode(object.getByte(0x19));
		if (type == StatusUpdate.Type.ONLINE) {
			API.getInstance().getNotificationProvider()
				.addStatus("api.friends", "api.friends.statusChange.online",
					user.get().getName());
		} else if (type == StatusUpdate.Type.OFFLINE) {
			API.getInstance().getNotificationProvider()
				.addStatus("api.friends", "api.friends.statusChange.offline",
					user.get().getName());
		} else if (type == StatusUpdate.Type.IN_GAME || type == StatusUpdate.Type.IN_GAME_UNKNOWN) {
			API.getInstance().getNotificationProvider()
				.addStatus("api.friends", "api.friends.statusChange.inGame",
					user.get().getName(), user.get().getStatus().getTitle());
		}
	}
}
