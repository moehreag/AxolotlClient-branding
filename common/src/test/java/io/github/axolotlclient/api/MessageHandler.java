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

import java.util.logging.Level;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.requests.c2s.HandshakeC2S;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class MessageHandler extends SimpleChannelInboundHandler<ServerRequest> {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ApiTestServer.getInstance().getLogger().log(Level.SEVERE, "Handler error", cause);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ServerRequest msg) {

		handleMessage(ctx, msg);

	}

	private void handleMessage(ChannelHandlerContext ctx, ServerRequest msg) {

		ApiTestServer.getInstance().getLogger().info("Received: " + msg.getClass().getSimpleName());

		if (msg instanceof HandshakeC2S){
			Connections.put(((HandshakeC2S) msg).uuid, ctx.channel());
		}

		ApiTestServer.getInstance().getLogger().info("Handling message with id: "+msg.identifier);
		ServerResponse response = msg.handle(Connections.get(ctx.channel()));
		if (response != null) {
			response.identifier = msg.identifier;
			ByteBuf data = ApiTestServer.getInstance().prependMetadata(response);
			ApiTestServer.getInstance().getLogger().info("Replying: " + response.getClass().getSimpleName() + ":"+msg.identifier);
			ctx.writeAndFlush(data);
		}
	}

}
