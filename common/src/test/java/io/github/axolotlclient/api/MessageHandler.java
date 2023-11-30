package io.github.axolotlclient.api;

import java.util.logging.Level;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
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

		ApiTestServer.getInstance().getLogger().info("Handling message with id: "+msg.identifier);
		ServerResponse response = msg.handle();
		if (response != null) {
			response.identifier = msg.identifier;
			ByteBuf data = ApiTestServer.getInstance().prependMetadata(response);
			ApiTestServer.getInstance().getLogger().info("Replying: " + response.getClass().getSimpleName() + ":");
			ctx.writeAndFlush(data);
		}
	}

}
