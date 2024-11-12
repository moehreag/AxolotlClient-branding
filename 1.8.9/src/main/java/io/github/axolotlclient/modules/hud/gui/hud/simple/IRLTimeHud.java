/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class IRLTimeHud extends SimpleTextHudEntry {
	// https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

	public static final Identifier ID = new Identifier("kronhud", "irltimehud");

	private DateTimeFormatter formatter = null;
	private boolean error = false;

	private final StringOption format = new StringOption("dateformat",
		"HH:mm:ss", this::updateDateTimeFormatter);

	@Override
	public Identifier getId() {
		return ID;
	}

	public void updateDateTimeFormatter(String value) {
		try {
			formatter = DateTimeFormatter.ofPattern(value);
			error = false;
		} catch (Exception e) {
			error = true;
			formatter = null;
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(format);
		return options;
	}

	@Override
	public String getValue() {
		if (error) {
			return "Error Compiling!";
		}
		if (formatter == null) {
			updateDateTimeFormatter(format.get());
			return getValue();
		}
		return formatter.format(LocalDateTime.now());
	}

	@Override
	public String getPlaceholder() {
		if (error) {
			return "Error Compiling!";
		}
		if (formatter == null) {
			updateDateTimeFormatter(format.get());
			return getValue();
		}
		return formatter.format(LocalDateTime.of(2020, Month.AUGUST, 22, 14, 28, 32, 1595135));
	}
}
