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

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.types.Relation;
import io.github.axolotlclient.api.util.SocketMessageHandler;
import io.github.axolotlclient.api.util.UUIDHelper;

public class FriendRequestHandler implements SocketMessageHandler {
	@Override
	public boolean isApplicable(String target) {
		return "friend_request".equals(target);
	}

	@Override
	public void handle(Response response) {
		String from = response.getBody("from");
		if (API.getInstance().getApiOptions().friendRequestsEnabled.get()) {
			API.getInstance().getNotificationProvider().addStatus("api.friends", "api.friends.request", UUIDHelper.getUsername(from));
		} else {
			FriendRequest.getInstance().setRelation(from, Relation.NONE);
		}
	}
}
