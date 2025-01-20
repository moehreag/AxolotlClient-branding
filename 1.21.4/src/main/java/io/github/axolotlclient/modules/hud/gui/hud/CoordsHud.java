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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CoordsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "coordshud");

	private final ColorOption secondColor = new ColorOption("secondtextcolor", ClientColors.WHITE);
	private final ColorOption firstColor = new ColorOption("firsttextcolor", ClientColors.SELECTOR_BLUE);
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);
	private final BooleanOption minimal = new BooleanOption("minimal", false);
	private final BooleanOption biome = new BooleanOption("show_biome", false);

	private final EnumOption<AnchorPoint> anchor =
		new EnumOption<>("anchorpoint", AnchorPoint.class, AnchorPoint.TOP_MIDDLE);

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
		double yaw = client.player.getYRot(0) + 180;
		int dir = getDirection(yaw);
		String direction = getWordedDirection(dir);
		Font textRenderer = client.font;
		int width, height;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			currPos = graphics.drawString(textRenderer, "XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(x), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = graphics.drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(y), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = graphics.drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(z), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			width = currPos - pos.x() + 2;
			height = 11;
		} else {
			int xEnd;
			int yEnd = pos.y() + 2;
			graphics.drawString(textRenderer, "X", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = graphics.drawString(textRenderer, df.format(x), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get());
			yEnd += 10;

			graphics.drawString(textRenderer, "Y", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = Math.max(xEnd, graphics.drawString(textRenderer, df.format(y), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;

			graphics.drawString(textRenderer, "Z", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());

			xEnd = Math.max(xEnd, graphics.drawString(textRenderer, df.format(z), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;


			xEnd = Math.max(pos.x() + 60, xEnd + 4);

			graphics.drawString(textRenderer, direction, xEnd, pos.y() + 12, firstColor.get().toInt(), shadow.get());

			graphics.drawString(textRenderer, getXDir(dir), xEnd, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			graphics.drawString(textRenderer, getZDir(dir), xEnd, pos.y() + 22, secondColor.get().toInt(),
				shadow.get());
			xEnd += 19;
			width = xEnd - pos.x();
			height = yEnd + 1 - pos.y();
		}
		if (biome.get() && y >= client.level.getMinY() && y < client.level.getMaxY()) {
			BlockPos b = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
			int bX = graphics.drawString(textRenderer, I18n.get("coordshud.biome"), pos.x() + 1, height + pos.y(), firstColor.get().toInt(), shadow.get());
			bX += 5;
			width = Math.max(width + pos.x() - 1, graphics.drawString(textRenderer, getBiomeName(this.client.level.getBiome(b).unwrap().left().orElse(null)), bX, height + pos.y(), secondColor.get().toInt(), shadow.get())) - pos.x() + 1;
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

	private String getBiomeName(ResourceKey<Biome> biome) {
		if (biome == null) {
			return "Unknown";
		}
		String path = biome.location().getPath();
		if (!biome.location().getNamespace().equals("minecraft")) {
			path += "("+biome.location().getNamespace()+")";
		}
		final String str = path.replace("_", " ");
		if (str.isEmpty()) {
			return str;
		}

		final int[] codepoints = str.codePoints().toArray();
		boolean capitalizeNext = true;
		for (int i = 0; i < codepoints.length; i++) {
			final int ch = codepoints[i];
			if (Character.isWhitespace(ch)) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				codepoints[i] = Character.toTitleCase(ch);
				capitalizeNext = false;
			}
		}
		return new String(codepoints, 0, codepoints.length);
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
		Font textRenderer = client.font;
		int width, height;
		if (minimal.get()) {
			int currPos = pos.x() + 1;
			String separator = ", ";
			currPos = graphics.drawString(textRenderer, "XYZ: ", currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(x), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = graphics.drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(y), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			currPos = graphics.drawString(textRenderer, separator, currPos, pos.y() + 2, firstColor.get().toInt(), shadow.get());
			currPos = graphics.drawString(textRenderer, df.format(z), currPos, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			width = currPos - pos.x() + 2;
			height = 11;
		} else {
			int xEnd;
			int yEnd = pos.y() + 2;
			graphics.drawString(textRenderer, "X", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = graphics.drawString(textRenderer, df.format(x), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get());
			yEnd += 10;

			graphics.drawString(textRenderer, "Y", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());
			xEnd = Math.max(xEnd, graphics.drawString(textRenderer, df.format(y), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;

			graphics.drawString(textRenderer, "Z", pos.x() + 1, yEnd, firstColor.get().toInt(), shadow.get());

			xEnd = Math.max(xEnd, graphics.drawString(textRenderer, df.format(z), pos.x() + 11, yEnd,
				secondColor.get().toInt(), shadow.get()));

			yEnd += 10;


			xEnd = Math.max(pos.x() + 60, xEnd + 4);

			graphics.drawString(textRenderer, direction, xEnd, pos.y() + 12, firstColor.get().toInt(), shadow.get());

			graphics.drawString(textRenderer, getXDir(dir), xEnd, pos.y() + 2, secondColor.get().toInt(),
				shadow.get());
			graphics.drawString(textRenderer, getZDir(dir), xEnd, pos.y() + 22, secondColor.get().toInt(),
				shadow.get());
			xEnd += 19;
			width = xEnd - pos.x();
			height = yEnd + 1 - pos.y();
		}
		if (biome.get()) {
			int bX = graphics.drawString(textRenderer, I18n.get("coordshud.biome"), pos.x() + 1, height + pos.y(), firstColor.get().toInt(), shadow.get());
			bX += 5;
			width = Math.max(width + pos.x() - 1, graphics.drawString(textRenderer, getBiomeName(Biomes.PLAINS), bX, height + pos.y(), secondColor.get().toInt(), shadow.get())) - pos.x() + 1;
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
	public ResourceLocation getId() {
		return ID;
	}

	public AnchorPoint getAnchor() {
		return (anchor.get());
	}
}
