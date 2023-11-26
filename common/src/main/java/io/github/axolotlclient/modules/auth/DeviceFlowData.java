package io.github.axolotlclient.modules.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class DeviceFlowData {
	@Getter
	private final String message;
	@Getter
	private final String verificationUri;
	@Getter
	private final String deviceCode;
	@Getter
	private final String userCode;
	@Getter
	private final int expiresIn;
	@Getter
	private final int interval;

	@Setter
	private StatusConsumer statusConsumer;

	public void setStatus(String status) {
		if (statusConsumer != null) {
			statusConsumer.emit(status);
		}
	}

	public interface StatusConsumer {
		void emit(String status);
	}
}
