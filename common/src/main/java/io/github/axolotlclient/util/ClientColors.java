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

package io.github.axolotlclient.util;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientColors {
	public static Color WHITE = Colors.WHITE;
	public static Color BLACK = Colors.BLACK;
	public static Color GRAY = Colors.GRAY;
	public static Color DARK_GRAY = Colors.DARK_GRAY;
	public static Color SELECTOR_RED = new Color(191, 34, 34).immutable();
	public static Color GOLD = Color.parse("#b8860b").immutable();
	public static Color SELECTOR_GREEN = new Color(53, 219, 103).immutable();
	public static Color SELECTOR_BLUE = new Color(51, 153, 255, 255).immutable();
	public static Color ERROR = new Color(255, 0, 255).immutable();

	/**
	 * Blends two {@link Color}s based off of a percentage.
	 *
	 * @param original   color to start the blend with
	 * @param blend      color that when fully blended, will be this
	 * @param percentage the percentage to blend
	 * @return the simple color
	 */
	public static Color blend(Color original, Color blend, float percentage) {
		if (percentage >= 1) {
			return blend;
		}
		if (percentage <= 0) {
			return original;
		}
		int red = blendInt(original.getRed(), blend.getRed(), percentage);
		int green = blendInt(original.getGreen(), blend.getGreen(), percentage);
		int blue = blendInt(original.getBlue(), blend.getBlue(), percentage);
		int alpha = blendInt(original.getAlpha(), blend.getAlpha(), percentage);
		return new Color(red, green, blue, alpha);
	}

	/**
	 * Blends two ints together based off of a percent.
	 *
	 * @param start   starting int
	 * @param end     end int
	 * @param percent percent to blend
	 * @return the blended int
	 */
	public static int blendInt(int start, int end, float percent) {
		if (percent <= 0) {
			return start;
		}
		if (start == end || percent >= 1) {
			return end;
		}
		int dif = end - start;
		int add = Math.round((float) dif * percent);
		return start + (add);
	}
}
