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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.NonNull;

public class ClientEndpoint {

	private static EventLoopGroup group;

	public void run(String url, int port) {
		boolean epollAvailable = Epoll.isAvailable();
		group = epollAvailable ?
			new EpollEventLoopGroup(0,
				new ThreadFactoryBuilder().setNameFormat("AxolotlClient Netty Epoll Client IO #%d").setDaemon(true).build()) :
			new NioEventLoopGroup(3,
				new ThreadFactoryBuilder().setNameFormat("AxolotlClient Netty Client IO #%d").setDaemon(true).build());

		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(epollAvailable ? EpollSocketChannel.class : NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(@NonNull SocketChannel ch) {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
							@Override
							protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
								onMessage(msg);
							}
						});
					}


				});
			ChannelFuture f = b.connect(url, port).sync();

			Channel channel = f.channel();
			onOpen(channel);

		} catch (Throwable e) {
			onError(e);
		} finally {
			group.shutdownGracefully();
			onClose();
		}
	}


	public void onMessage(ByteBuf message) {
		API.getInstance().onMessage(message);
	}

	public void onOpen(Channel channel) {
		API.getInstance().onOpen(channel);
	}

	public void onError(Throwable throwable) {
		API.getInstance().onError(throwable);
	}

	public void onClose() {
		API.getInstance().onClose();
	}

	public static void shutdown() {
		if (group != null && !group.isTerminated()) {
			group.shutdownGracefully();
			group = null;
		}
	}
}
