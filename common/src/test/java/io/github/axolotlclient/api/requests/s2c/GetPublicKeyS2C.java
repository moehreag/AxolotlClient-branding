package io.github.axolotlclient.api.requests.s2c;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import io.github.axolotlclient.api.requests.ServerResponse;
import io.github.axolotlclient.api.util.Serializer;

public class GetPublicKeyS2C extends ServerResponse {
	public byte[] key;
	@Serializer.Exclude
	KeyPair pair;

	public GetPublicKeyS2C(){
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			pair = generator.generateKeyPair();
			key = pair.getPublic().getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
