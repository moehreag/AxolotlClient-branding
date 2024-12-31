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

package io.github.axolotlclient.api.handlers;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.ChannelInvite;
import io.github.axolotlclient.api.util.SocketMessageHandler;
import io.github.axolotlclient.api.util.UUIDHelper;

public class ChannelInviteHandler implements SocketMessageHandler {
	@Override
	public boolean isApplicable(String target) {
		return "channel_invite".equals(target) || "channel_invite_reaction".equals(target);
	}

	@Override
	public void handle(Response response) {
		String id = response.getBody("channel", l -> Long.toUnsignedString((long) l));
		String target = response.getBody("target");
		String channelName = response.getBody("channel_name");
		switch (target) {
			case "channel_invite" -> {
				String from = response.getBody("from");
				if (API.getInstance().getApiOptions().channelInvitesEnabled.get()) {
					notification("api.channels.invite", "api.channels.invite.desc", UUIDHelper.getUsername(from), channelName);
				} else {
					ChannelRequest.ignoreChannelInvite(new ChannelInvite(id, channelName, from));
				}
			}
			case "channel_invite_reaction" -> {
				String player = response.getBody("player");
				boolean accepted = response.getBody("accepted");
				if (accepted) {
					notification("api.channels.invite.accepted", "api.channels.invite.accepted.desc", UUIDHelper.getUsername(player), channelName);
				} else {
					notification("api.channels.invite.denied", "api.channels.invite.denied.desc", UUIDHelper.getUsername(player), channelName);
				}
			}
		}
	}
}
