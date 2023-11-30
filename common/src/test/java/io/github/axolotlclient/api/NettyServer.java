package io.github.axolotlclient.api;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyServer {

	private final EventLoopGroup bossLoopGroup;

	private final EventLoopGroup workerLoopGroup;

	private final ChannelGroup channelGroup;

	public NettyServer() {
		// Initialization private members
		boolean epollAvailable = Epoll.isAvailable();
		bossLoopGroup = epollAvailable ?
			new EpollEventLoopGroup(3,
				new DefaultThreadFactory("AxolotlClient Netty Epoll Server IO")) :
			new NioEventLoopGroup(3,
				new DefaultThreadFactory("AxolotlClient Netty Epoll Server IO"));

		workerLoopGroup = epollAvailable ?
			new EpollEventLoopGroup(3,
				new DefaultThreadFactory("AxolotlClient Netty Epoll Server IO")) :
			new NioEventLoopGroup(3,
				new DefaultThreadFactory("AxolotlClient Netty Epoll Server IO"));

		this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	}


	public final void startup(int port) throws Exception {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossLoopGroup, workerLoopGroup)
			.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.option(ChannelOption.AUTO_CLOSE, true)
			.option(ChannelOption.SO_REUSEADDR, true)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);

		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) {
				ChannelPipeline pipeline = ch.pipeline();

				final MessageDecoder decoder = new MessageDecoder();

				pipeline.addLast("decoder", decoder);

				final MessageHandler handler = new MessageHandler();

				pipeline.addLast(new DefaultEventExecutorGroup(Math.max(Runtime.getRuntime().availableProcessors(), 6)),
					"handler", handler);
			}

		});

		try {
			ChannelFuture channelFuture = bootstrap.bind(port).sync();
			channelGroup.add(channelFuture.channel());
		} catch (Exception e) {
			shutdown();
			throw e;
		}
	}


	public final void shutdown() {
		channelGroup.close();
		bossLoopGroup.shutdownGracefully();
		workerLoopGroup.shutdownGracefully();
	}

}
