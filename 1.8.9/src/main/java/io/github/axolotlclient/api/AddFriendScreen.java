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

import java.util.UUID;

import io.github.axolotlclient.api.handlers.FriendHandler;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public class AddFriendScreen extends SimpleTextInputScreen {
	public AddFriendScreen(Screen parent) {
		super(parent, I18n.translate("api.screen.friends.add"), I18n.translate("api.screen.friends.add.name"),
			s -> {
				if (API.getInstance().isConnected()) {
					String uuid;
					try {
						uuid = API.getInstance().sanitizeUUID(UUID.fromString(s).toString());
					} catch (IllegalArgumentException e) {
						uuid = UUIDHelper.getUuid(s);
					}
					FriendHandler.getInstance().addFriend(uuid);
				} else {
					Notifications.getInstance().addStatus("api.error.notLoggedIn", "api.error.notLoggedIn.desc");
				}
			});
	}
}
