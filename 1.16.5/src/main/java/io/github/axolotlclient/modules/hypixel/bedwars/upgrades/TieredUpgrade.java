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

package io.github.axolotlclient.modules.hypixel.bedwars.upgrades;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMode;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

/**
 * @author DarkKronicle
 */

public class TieredUpgrade extends TeamUpgrade {

	private final int[] doublesPrice;
	private final int[] foursPrice;
	private final TeamUpgradeRenderer drawer;
	@Getter
	private int level = 0;

	public TieredUpgrade(String name, Pattern regex, int[] foursPrice, int[] doublesPrice, TeamUpgradeRenderer drawer) {
		super(name, regex);
		this.foursPrice = foursPrice;
		this.doublesPrice = doublesPrice;
		this.drawer = drawer;
	}

	@Override
	public void draw(MatrixStack matrices, int x, int y, int width, int height) {
		drawer.render(matrices, x, y, width, height, level);
	}

	@Override
	public boolean isPurchased() {
		return level > 0;
	}

	@Override
	protected void onMatch(TeamUpgrade upgrade, Matcher matcher) {
		level += 1;
	}

	public boolean isMaxedOut(BedwarsMode mode) {
		if (mode.getTeams().length == 8) {
			return level >= doublesPrice.length;
		}
		return level >= foursPrice.length;
	}

	@Override
	public int getPrice(BedwarsMode mode) {
		if (mode.getTeams().length == 8) {
			return doublesPrice[level];
		}
		return foursPrice[level];
	}
}
