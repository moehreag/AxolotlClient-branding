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
import net.minecraft.client.render.TextRenderer;
import net.minecraft.resource.Identifier;

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

	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchor", AnchorPoint.class,
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
		switch (dir) {
			case 3:
				return "++";
			case 2:
			case 4:
				return "+";
			case 6:
			case 8:
				return "-";
			case 7:
				return "--";
			default:
				return "";
		}
	}

	public static String getZDir(int dir) {
		switch (dir) {
			case 5:
				return "++";
			case 4:
			case 6:
				return "+";
			case 8:
			case 2:
				return "-";
			case 1:
				return "--";
			default:
				return "";
		}
	}

	@Override
	public void renderComponent(float delta) {
		DrawPosition pos = getPos();
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			for (int i = 0; i < decimalPlaces.get(); i++) {
				format.append("0");
			}
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		df.setRoundingMode(RoundingMode.CEILING);
		double x = client.player.x;
		double y = client.player.y;
		double z = client.player.z;
		double yaw = client.player.yaw + 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		TextRenderer textRenderer = client.textRenderer;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			drawString(textRenderer, "XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth("XYZ: ");
			drawString(textRenderer, String.valueOf(df.format(x)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(x)));
			drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(separator);
			drawString(textRenderer, String.valueOf(df.format(y)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(y)));
			drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(separator);
			drawString(textRenderer, String.valueOf(df.format(z)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(z)));
			int width = currPos - pos.x() + 2;
			boolean changed = false;
			if (getWidth() != width) {
				setWidth(width);
				changed = true;
			}
			if (getHeight() != 11) {
				setHeight(11);
				changed = true;
			}
			if (changed) {
				onBoundsUpdate();
			}
		} else {
			drawString(textRenderer, "X", pos.x() + 1, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			drawString(textRenderer, String.valueOf(df.format(x)), pos.x() + 11, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());

			drawString(textRenderer, "Y", pos.x() + 1, pos.y() + 12, firstColor.get().toInt(), shadow.get());
			drawString(textRenderer, String.valueOf(df.format(y)), pos.x() + 11, pos.y() + 12,
				secondColor.get().toInt(), shadow.get());

			drawString(textRenderer, "Z", pos.x() + 1, pos.y() + 22, firstColor.get().toInt(), shadow.get());

			drawString(textRenderer, String.valueOf(df.format(z)), pos.x() + 11, pos.y() + 22,
				secondColor.get().toInt(), shadow.get());

			drawString(textRenderer, direction, pos.x() + 60, pos.y() + 12, firstColor.get().toInt(), shadow.get());

			drawString(textRenderer, getXDir(dir), pos.x() + 60, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			drawString(textRenderer, getZDir(dir), pos.x() + 60, pos.y() + 22, secondColor.get().toInt(),
				shadow.get());
			boolean changed = false;
			if (getWidth() != 79) {
				setWidth(79);
				changed = true;
			}
			if (getHeight() != 31) {
				setHeight(31);
				changed = true;
			}
			if (changed) {
				onBoundsUpdate();
			}
		}
	}

	public String getWordedDirection(int dir) {
		switch (dir) {
			case 1:
				return "N";
			case 2:
				return "NE";
			case 3:
				return "E";
			case 4:
				return "SE";
			case 5:
				return "S";
			case 6:
				return "SW";
			case 7:
				return "W";
			case 8:
				return "NW";
			case 0:
				return "?";
			default:
				return "";
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			for (int i = 0; i < decimalPlaces.get(); i++) {
				format.append("#");
			}
		}

		DecimalFormat df = new DecimalFormat(format.toString());
		df.setRoundingMode(RoundingMode.FLOOR);
		double x = 109.2325;
		double y = 180.8981;
		double z = -5098.32698;
		double yaw = 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		TextRenderer textRenderer = client.textRenderer;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";

			textRenderer.draw("XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt());
			currPos += textRenderer.getWidth("XYZ: ");
			textRenderer.draw(String.valueOf(df.format(x)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(x)));
			textRenderer.draw(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(separator);
			textRenderer.draw(String.valueOf(df.format(y)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(y)));
			textRenderer.draw(separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(separator);
			textRenderer.draw(String.valueOf(df.format(z)), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(z)));

			int width = currPos - pos.x() + 2;
			boolean changed = false;
			if (getWidth() != width) {
				setWidth(width);
				changed = true;
			}
			if (getHeight() != 11) {
				setHeight(11);
				changed = true;
			}
			if (changed) {
				onBoundsUpdate();
			}
		} else {
			textRenderer.drawWithShadow("X", pos.x() + 1, pos.y() + 2, firstColor.get().toInt());
			textRenderer.drawWithShadow(String.valueOf(df.format(x)), pos.x() + 11, pos.y() + 2,
				secondColor.get().toInt());
			textRenderer.drawWithShadow("Y", pos.x() + 1, pos.y() + 12, firstColor.get().toInt());
			textRenderer.drawWithShadow(String.valueOf(df.format(y)), pos.x() + 11, pos.y() + 12,
				secondColor.get().toInt());
			textRenderer.drawWithShadow("Z", pos.x() + 1, pos.y() + 22, firstColor.get().toInt());
			textRenderer.drawWithShadow(String.valueOf(df.format(z)), pos.x() + 11, pos.y() + 22,
				secondColor.get().toInt());
			textRenderer.drawWithShadow(direction, pos.x() + 60, pos.y() + 12, firstColor.get().toInt());
			textRenderer.drawWithShadow(getXDir(dir), pos.x() + 60, pos.y() + 2, secondColor.get().toInt());
			textRenderer.drawWithShadow(getZDir(dir), pos.x() + 60, pos.y() + 22, secondColor.get().toInt());
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(firstColor);
		options.add(secondColor);
		options.add(decimalPlaces);
		options.add(minimal);
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
