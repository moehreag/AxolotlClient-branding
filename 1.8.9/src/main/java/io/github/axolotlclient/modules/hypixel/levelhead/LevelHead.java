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

package io.github.axolotlclient.modules.hypixel.levelhead;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.ClientColors;
import lombok.Getter;

public class LevelHead implements AbstractHypixelMod {

	@Getter
	private static final LevelHead Instance = new LevelHead();
	public final BooleanOption enabled = new BooleanOption("enabled", "levelhead.enabled.tooltip", false);
	public final BooleanOption background = new BooleanOption("background", false);
	public final ColorOption textColor = new ColorOption("textcolor", ClientColors.GOLD);
	public final EnumOption<LevelHeadMode> mode = new EnumOption<>("levelHeadMode", LevelHeadMode.class,
		LevelHeadMode.NETWORK);
	private final OptionCategory category = OptionCategory.create("levelhead");

	@Override
	public void init() {
		category.add(enabled);
		category.add(textColor);
		category.add(background);
		category.add(mode);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}
}
