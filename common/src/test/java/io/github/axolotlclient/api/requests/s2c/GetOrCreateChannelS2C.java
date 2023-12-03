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

package io.github.axolotlclient.api.requests.s2c;

import java.nio.charset.StandardCharsets;

import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetOrCreateChannelS2C extends ServerResponse {

	private final String uuid;

	@Override
	public ByteBuf serialize() {
		ByteBuf buf = Unpooled.buffer();
		buf.writeCharSequence("00000", StandardCharsets.UTF_8);
		buf.writeCharSequence(BufferUtil.padString("Loopback Channel", 64), StandardCharsets.UTF_8);
		buf.writeInt(2);
		buf.writeCharSequence(uuid, StandardCharsets.UTF_8);
		buf.writeCharSequence(uuid, StandardCharsets.UTF_8);
		buf.writeInt(0);
		return buf;
	}
}
