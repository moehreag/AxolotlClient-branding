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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CoordsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = Identifier.of("kronhud", "coordshud");

	private final ColorOption secondColor = new ColorOption("secondtextcolor", ClientColors.WHITE);
	private final ColorOption firstColor = new ColorOption("firsttextcolor", ClientColors.SELECTOR_BLUE);
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);
	private final BooleanOption minimal = new BooleanOption("minimal", false);

	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchor", AnchorPoint.class, AnchorPoint.TOP_MIDDLE);

	public CoordsHud() {
		super(79, 31, true);
	}

	/**
	 * Get direction. 1 = North, 2 North East, 3 East, 4 South East...
	 *
	 * @param yaw the player's yaw
	 * @return a Direction value of 0-360 degrees.
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
	public void renderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			format.append("0".repeat(Math.max(0, decimalPlaces.get())));
		}
		DecimalFormat df = new DecimalFormat(format.toString());
		df.setRoundingMode(RoundingMode.CEILING);
		double x = client.player.getX();
		double y = client.player.getY();
		double z = client.player.getZ();
		double yaw = client.player.getYaw(0) + 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		TextRenderer textRenderer = client.textRenderer;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			drawString(graphics, textRenderer, "XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth("XYZ: ");
			drawString(graphics, textRenderer, String.valueOf(df.format(x)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(x)));
			drawString(graphics, textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(separator);
			drawString(graphics, textRenderer, String.valueOf(df.format(y)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(y)));
			drawString(graphics, textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(separator);
			drawString(graphics, textRenderer, String.valueOf(df.format(z)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
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
			drawString(graphics, textRenderer, "X", pos.x() + 1, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			drawString(graphics, textRenderer, String.valueOf(df.format(x)), pos.x() + 11, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());

			drawString(graphics, textRenderer, "Y", pos.x() + 1, pos.y() + 12, firstColor.get().toInt(),
				shadow.get());
			drawString(graphics, textRenderer, String.valueOf(df.format(y)), pos.x() + 11, pos.y() + 12,
				secondColor.get().toInt(), shadow.get());

			drawString(graphics, textRenderer, "Z", pos.x() + 1, pos.y() + 22, firstColor.get().toInt(),
				shadow.get());

			drawString(graphics, textRenderer, String.valueOf(df.format(z)), pos.x() + 11, pos.y() + 22,
				secondColor.get().toInt(), shadow.get());

			drawString(graphics, textRenderer, direction, pos.x() + 60, pos.y() + 12, firstColor.get().toInt(),
				shadow.get());

			drawString(graphics, textRenderer, getXDir(dir), pos.x() + 60, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			graphics.drawText(client.textRenderer, getZDir(dir), pos.x() + 60, pos.y() + 22, secondColor.get().toInt(), shadow.get());
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
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
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
		TextRenderer textRenderer = client.textRenderer;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";

			graphics.drawShadowedText(client.textRenderer, "XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt());
			currPos += textRenderer.getWidth("XYZ: ");
			graphics.drawText(client.textRenderer, String.valueOf(df.format(x)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(x)));
			graphics.drawText(client.textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(separator);
			graphics.drawText(client.textRenderer, String.valueOf(df.format(y)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
			currPos += textRenderer.getWidth(String.valueOf(df.format(y)));
			graphics.drawText(client.textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(),
				shadow.get());
			currPos += textRenderer.getWidth(separator);
			graphics.drawText(client.textRenderer, String.valueOf(df.format(z)), currPos, pos.y() + 2,
				secondColor.get().toInt(), shadow.get());
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
			graphics.drawShadowedText(client.textRenderer, "X", pos.x() + 1, pos.y() + 2, firstColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, String.valueOf(df.format(x)), pos.x() + 11, pos.y() + 2,
				secondColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, "Y", pos.x() + 1, pos.y() + 12, firstColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, String.valueOf(df.format(y)), pos.x() + 11, pos.y() + 12,
				secondColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, "Z", pos.x() + 1, pos.y() + 22, firstColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, String.valueOf(df.format(z)), pos.x() + 11, pos.y() + 22,
				secondColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, direction, pos.x() + 60, pos.y() + 12, firstColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, getXDir(dir), pos.x() + 60, pos.y() + 2,
				secondColor.get().toInt());
			graphics.drawShadowedText(client.textRenderer, getZDir(dir), pos.x() + 60, pos.y() + 22,
				secondColor.get().toInt());
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
		return (anchor.get());
	}
}
