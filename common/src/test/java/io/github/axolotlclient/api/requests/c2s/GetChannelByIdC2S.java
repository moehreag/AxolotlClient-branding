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

package io.github.axolotlclient.api.requests.c2s;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.requests.s2c.GetChannelByIdS2C;
import io.github.axolotlclient.api.util.Serializer;

public class GetChannelByIdC2S extends ServerRequest {

	private final String channelId;

	public GetChannelByIdC2S(@Serializer.Length(5) String channelId) {
		this.channelId = channelId;
	}

	@Override
	public ServerResponse handle(String senderUuid) {
		return new GetChannelByIdS2C(senderUuid, channelId);
	}
}
