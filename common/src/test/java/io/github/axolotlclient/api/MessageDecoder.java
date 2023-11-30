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
			return;
		}

		CharSequence magic = in.readCharSequence(3, StandardCharsets.UTF_8);
		Class<? extends ServerRequest> c = Requests.fromType(in.readByte());
		byte protocolVersion = in.readByte();
		int identifier = in.readInt();
		if (!magic.equals("AXO")) {
			return;
		}

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
	}

}
