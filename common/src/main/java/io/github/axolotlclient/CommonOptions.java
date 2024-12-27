package io.github.axolotlclient;

import java.time.format.DateTimeFormatter;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;

public class CommonOptions {
	public static final StringOption datetimeFormat = new StringOption("datetime_format", "yyyy/MM/dd HH:mm:ss", s -> AxolotlClientCommon.getInstance().formatter = DateTimeFormatter.ofPattern(s));

}
