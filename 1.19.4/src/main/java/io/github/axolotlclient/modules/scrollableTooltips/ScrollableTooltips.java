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

package io.github.axolotlclient.modules.scrollableTooltips;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.client.gui.screen.Screen;

public class ScrollableTooltips extends AbstractModule {

	private static final ScrollableTooltips Instance = new ScrollableTooltips();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final BooleanOption enableShiftHorizontalScroll = new BooleanOption("shiftHorizontalScroll", true);
	protected final IntegerOption scrollAmount = new IntegerOption("scrollAmount", 5, 1, 20);
	protected final BooleanOption inverse = new BooleanOption("inverse", false);
	private final OptionCategory category = new OptionCategory("scrollableTooltips");
	public int tooltipOffsetX;
	public int tooltipOffsetY;

	public static ScrollableTooltips getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled);
		category.add(enableShiftHorizontalScroll);
		category.add(scrollAmount);
		category.add(inverse);

		AxolotlClient.CONFIG.rendering.addSubCategory(category);
	}

	public void onScroll(boolean reverse) {
		if (Screen.hasShiftDown()) {
			if (applyInverse(reverse)) {
				tooltipOffsetX -= scrollAmount.get();
			} else {
				tooltipOffsetX += scrollAmount.get();
			}
		} else {
			if (applyInverse(reverse)) {
				tooltipOffsetY -= scrollAmount.get();
			} else {
				tooltipOffsetY += scrollAmount.get();
			}
		}
	}

	protected boolean applyInverse(boolean value) {
		return inverse.get() != value;
	}

	public void resetScroll() {
		tooltipOffsetY = tooltipOffsetX = 0;
	}
}
