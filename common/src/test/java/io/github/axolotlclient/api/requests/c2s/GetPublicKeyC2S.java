package io.github.axolotlclient.api.requests.c2s;

import io.github.axolotlclient.api.requests.ServerRequest;
import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.requests.s2c.GetPublicKeyS2C;

public class GetPublicKeyC2S extends ServerRequest {
	@Override
	public ServerResponse handle() {
		return new GetPublicKeyS2C();
	}
}
