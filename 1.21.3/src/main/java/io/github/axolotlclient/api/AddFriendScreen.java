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

package io.github.axolotlclient.api;

import java.util.UUID;

import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddFriendScreen extends SimpleTextInputScreen {

	public AddFriendScreen(Screen parent) {
		super(parent, Component.translatable("api.screen.friends.add"),
			  Component.translatable("api.screen.friends.add.name"), string -> {
				if (API.getInstance().isSocketConnected()) {
					String uuid;
					try {
						uuid = API.getInstance().sanitizeUUID(UUID.fromString(string).toString());
					} catch (IllegalArgumentException e) {
						uuid = UUIDHelper.getUuid(string);
					}
					FriendRequest.getInstance().addFriend(uuid);
				} else {
					Notifications.getInstance().addStatus("api.error.notLoggedIn", "api.error.notLoggedIn.desc");
				}
			}
			 );
	}
}
