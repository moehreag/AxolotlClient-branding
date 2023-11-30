package io.github.axolotlclient.api.requests.s2c;

import io.github.axolotlclient.api.requests.ServerResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HandshakeS2C extends ServerResponse {
	public byte status;
}
