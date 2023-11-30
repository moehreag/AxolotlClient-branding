package io.github.axolotlclient.api.requests;

import io.github.axolotlclient.api.util.BufferUtil;
import io.github.axolotlclient.api.util.Serializer;
import io.netty.buffer.ByteBuf;

public abstract class ServerResponse {

	@Serializer.Exclude
	public int identifier;

	public ByteBuf serialize(){
		return BufferUtil.wrap(this);
	}
}
