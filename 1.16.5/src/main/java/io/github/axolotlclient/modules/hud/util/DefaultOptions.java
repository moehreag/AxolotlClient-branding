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

package io.github.axolotlclient.modules.hud.util;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.CardinalOrder;
import lombok.experimental.UtilityClass;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@UtilityClass
public class DefaultOptions {

	public static DoubleOption getX(double defaultX, HudEntry entry) {
		return new DoubleOption("x",  defaultX, value -> entry.onBoundsUpdate(), 0d, 1d);
	}

	public static DoubleOption getY(double defaultY, HudEntry entry) {
		return new DoubleOption("y", defaultY, value -> entry.onBoundsUpdate(), 0d, 1d);
	}

	public static DoubleOption getScale(HudEntry entry) {
		return new DoubleOption("scale", 1d, value -> entry.onBoundsUpdate(), 0d, 2d);
	}

	public static BooleanOption getEnabled() {
		return new BooleanOption("enabled", false);
	}

	public static EnumOption<AnchorPoint> getAnchorPoint() {
		return getAnchorPoint(AnchorPoint.TOP_LEFT);
	}

	public static EnumOption<AnchorPoint> getAnchorPoint(AnchorPoint defaultValue) {
		return new EnumOption<>("anchorpoint", AnchorPoint.class, defaultValue);
	}

	public static EnumOption<CardinalOrder> getCardinalOrder(CardinalOrder defaultValue) {
		return new EnumOption<>("cardinalorder", CardinalOrder.class, defaultValue);
	}
}
