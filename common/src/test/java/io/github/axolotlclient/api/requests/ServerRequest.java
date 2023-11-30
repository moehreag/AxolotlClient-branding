package io.github.axolotlclient.api.requests;

public abstract class ServerRequest {

	public int identifier;

	public ServerResponse handle(){
		return null;
	}

}
