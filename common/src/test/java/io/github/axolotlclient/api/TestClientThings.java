package io.github.axolotlclient.api;

import java.util.Arrays;

import io.github.axolotlclient.api.util.StatusUpdateProvider;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import io.github.axolotlclient.util.translation.TranslationProvider;
import lombok.Getter;

public class TestClientThings extends Options implements Logger, StatusUpdateProvider, TranslationProvider, NotificationProvider {
	@Getter
	private static final TestClientThings instance = new TestClientThings();

	private final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("TestClient");

	@Override
	public void initialize() {

	}

	@Override
	public Request getStatus() {
		return new Request(Request.Type.STATUS_UPDATE, "Playing around with the API!");
	}

	@Override
	public void info(String msg, Object... args) {
		logger.info("Client: "+ msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void warn(String msg, Object... args) {
		logger.warning("Client: "+msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void error(String msg, Object... args) {
		logger.severe("Client: "+msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void debug(String msg, Object... args) {
		logger.info("Client: "+"[DEBUG] " + msg + "\n" + Arrays.toString(args));
	}

	@Override
	public void addStatus(String titleKey, String descKey, Object... args) {
		info(titleKey + ":", String.format(descKey, args));
	}

	@Override
	public String translate(String key, Object... args) {
		return key;
	}
}
