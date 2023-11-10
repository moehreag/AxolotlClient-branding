/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.tnttime;

import java.text.DecimalFormat;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TntTime extends AbstractModule {

	private static final TntTime Instance = new TntTime();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	private final OptionCategory category = OptionCategory.create("tnttime");
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 2, 0, 6);
	private DecimalFormat format;
	private int decimals;

	public static TntTime getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled, decimalPlaces);
		AxolotlClient.CONFIG.rendering.add(category);
	}

	@Override
	public void tick() {
		if (decimalPlaces.get() != decimals || format == null) {
			StringBuilder string = new StringBuilder("#0");
			if (decimalPlaces.get() > 0) {
				string.append(".");
				for (int i = 0; i < decimalPlaces.get(); i++) {
					string.append("0");
				}
			}
			format = new DecimalFormat(string.toString());
			decimals = decimalPlaces.get();
		}
	}

	public Text getFuseTime(int time) {
		float secs = time / 20F;
		return Text.of(String.valueOf(format.format(secs))).copy()
			.setStyle(Style.EMPTY.withColor(getCurrentColor(secs)));
	}

	private Formatting getCurrentColor(float seconds) {
		if (seconds > 7d) {
			return Formatting.DARK_AQUA;
		} else if (seconds > 6d) {
			return Formatting.AQUA;
		} else if (seconds > 4d) {
			return Formatting.DARK_GREEN;
		} else if (seconds > 3d) {
			return Formatting.GREEN;
		} else if (seconds > 2d) {
			return Formatting.GOLD;
		} else if (seconds > 1d) {
			return Formatting.RED;
		} else if (seconds > 0d) {
			return Formatting.DARK_RED;
		} else {
			return Formatting.WHITE;
		}
	}
}
