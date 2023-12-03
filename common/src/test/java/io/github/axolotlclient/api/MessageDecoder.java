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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

		int readableBytes = in.readableBytes();
		if (readableBytes < 9) {
			System.out.println(in.toString(StandardCharsets.UTF_8));
			return;
		}

		CharSequence magic = in.readCharSequence(3, StandardCharsets.UTF_8);
		Class<? extends ServerRequest> c = Requests.fromType(in.readByte());
		byte protocolVersion = in.readByte();
		int identifier = in.readInt();
		if (!magic.equals("AXO")) {
			System.out.println("Unrecognized magic: "+magic);
			return;
		}

		System.out.println("Got packet: "+identifier+"("+(c == null ? "null" : c.getSimpleName())+")");

		if (c != null) {
			ApiTestServer.getInstance().getLogger().info("Unwrapping packet: "+c.getSimpleName());
			try {
				ServerRequest request = BufferUtil.unwrap(in, c);
				request.identifier = identifier;
				out.add(request);
			} catch (Exception e){
				ApiTestServer.getInstance().getLogger().log(Level.SEVERE, "", e);
			}
		} else {
			ApiTestServer.getInstance().getLogger().warning("Unrecognized packet type: " + identifier);
		}
		in.setIndex(readableBytes, readableBytes);
	}

}
