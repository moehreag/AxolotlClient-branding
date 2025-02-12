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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CoordsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "coordshud");

	private final ColorOption secondColor = new ColorOption("secondtextcolor", ClientColors.WHITE);
	private final ColorOption firstColor = new ColorOption("firsttextcolor", ClientColors.SELECTOR_BLUE);
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);
	private final BooleanOption minimal = new BooleanOption("minimal", false);
	private final BooleanOption biome = new BooleanOption("show_biome", false);

	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_MIDDLE);

	public CoordsHud() {
		super(79, 31, true);
	}

	/**
	 * Get direction. 1 = North, 2 North East, 3 East, 4 South East...
	 *
	 * @param yaw the player's yaw
	 * @return the direction, 0-360 degrees.
	 */
	public static int getDirection(double yaw) {
		yaw %= 360;

		if (yaw < 0) {
			yaw += 360;
		}
		int[] directions = {0, 23, 68, 113, 158, 203, 248, 293, 338, 360};
		for (int i = 0; i < directions.length; i++) {
			int min = directions[i];
			int max;
			if (i + 1 >= directions.length) {
				max = directions[0];
			} else {
				max = directions[i + 1];
			}
			if (yaw >= min && yaw < max) {
				if (i >= 8) {
					return 1;
				}
				return i + 1;
			}
		}
		return 0;
	}

	public static String getXDir(int dir) {
		return switch (dir) {
			case 3 -> "++";
			case 2, 4 -> "+";
			case 6, 8 -> "-";
			case 7 -> "--";
			default -> "";
		};
	}

	public static String getZDir(int dir) {
		return switch (dir) {
			case 5 -> "++";
			case 4, 6 -> "+";
			case 8, 2 -> "-";
			case 1 -> "--";
			default -> "";
		};
	}

	@Override
	public void renderComponent(float delta) {
		DrawPosition pos = getPos();
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			format.append("0".repeat(Math.max(0, decimalPlaces.get())));
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		df.setRoundingMode(RoundingMode.CEILING);
		double x = client.player.x;
		double y = client.player.y;
		double z = client.player.z;
		double yaw = client.player.yaw + 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		int width, height;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			currPos = drawString("XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(x), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = drawString(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(y), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = drawString(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(z), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			width = currPos - pos.x() + 2;
			height = 11;
		} else {
			int xEnd;
			int yEnd = pos.y() + 2;
			drawString("X", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = drawString(df.format(x), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get());
			yEnd += 10;

			drawString("Y", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = Math.max(xEnd, drawString(df.format(y), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;

			drawString("Z", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());

			xEnd = Math.max(xEnd, drawString(df.format(z), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;


			xEnd = Math.max(pos.x() + 60, xEnd + 4);

			drawString(direction, xEnd, pos.y() + 12, firstColor.get().toInt(), shadow.get());

			drawString(getXDir(dir), xEnd, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			drawString(getZDir(dir), xEnd, pos.y() + 22, secondColor.get().toInt(),
				shadow.get());
			xEnd += 19;
			width = xEnd - pos.x();
			height = yEnd + 1 - pos.y();
		}
		if (biome.get()) {
			BlockPos b = new BlockPos(x, y, z);
			int bX = drawString(I18n.translate("coordshud.biome"), pos.x() + 1, height + pos.y(), firstColor.get().toInt(), shadow.get());
			bX += 5;
			width = Math.max(width + pos.x() - 1, drawString(client.world.getBiome(b).name, bX, height + pos.y(), secondColor.get().toInt(), shadow.get())) - pos.x() + 1;
			height += 10;
		}
		boolean changed = false;
		if (getWidth() != width) {
			setWidth(width);
			changed = true;
		}
		if (getHeight() != height) {
			setHeight(height);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
	}

	public String getWordedDirection(int dir) {
		return switch (dir) {
			case 1 -> "N";
			case 2 -> "NE";
			case 3 -> "E";
			case 4 -> "SE";
			case 5 -> "S";
			case 6 -> "SW";
			case 7 -> "W";
			case 8 -> "NW";
			case 0 -> "?";
			default -> "";
		};
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			format.append("#".repeat(Math.max(0, decimalPlaces.get())));
		}

		DecimalFormat df = new DecimalFormat(format.toString());
		df.setRoundingMode(RoundingMode.FLOOR);
		double x = 109.2325;
		double y = 180.8981;
		double z = -5098.32698;
		double yaw = 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		int width, height;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			currPos = drawString("XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(x), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = drawString(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(y), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = drawString(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = drawString(df.format(z), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			width = currPos - pos.x() + 2;
			height = 11;
		} else {
			int xEnd;
			int yEnd = pos.y() + 2;
			drawString("X", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = drawString(df.format(x), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get());
			yEnd += 10;

			drawString("Y", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = Math.max(xEnd, drawString(df.format(y), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;

			drawString("Z", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());

			xEnd = Math.max(xEnd, drawString(df.format(z), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;

			xEnd = Math.max(pos.x() + 60, xEnd + 4);

			drawString(direction, xEnd, pos.y() + 12, firstColor.get().toInt(), shadow.get());

			drawString(getXDir(dir), xEnd, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			drawString(getZDir(dir), xEnd, pos.y() + 22, secondColor.get().toInt(),
				shadow.get());
			xEnd += 19;
			width = xEnd - pos.x();
			height = yEnd + 1 - pos.y();
		}
		if (biome.get()) {
			int bX = drawString(I18n.translate("coordshud.biome"), pos.x() + 1, height + pos.y(), firstColor.get().toInt(), shadow.get());
			bX += 5;
			width = Math.max(width + pos.x() - 1, drawString(Biome.PLAINS.name, bX, height + pos.y(), secondColor.get().toInt(), shadow.get())) - pos.x() + 1;
			height += 10;
		}
		boolean changed = false;
		if (getWidth() != width) {
			setWidth(width);
			changed = true;
		}
		if (getHeight() != height) {
			setHeight(height);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.remove(textColor);
		options.add(firstColor);
		options.add(secondColor);
		options.add(decimalPlaces);
		options.add(minimal);
		options.add(biome);
		options.add(anchor);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
