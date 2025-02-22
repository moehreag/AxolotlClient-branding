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

package io.github.axolotlclient.modules.hud.util;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class DrawUtil {

	public static void fillRect(GuiGraphics graphics, Rectangle rectangle, Color color) {
		fillRect(graphics, rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + height, color);
	}

	public static void outlineRect(GuiGraphics graphics, Rectangle rectangle, Color color) {
		outlineRect(graphics, rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void outlineRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.renderOutline(x, y, width, height, color);
	}

	public static void drawCenteredString(GuiGraphics graphics, Font renderer, String text, int x, int y, Color color, boolean shadow) {
		drawCenteredString(graphics, renderer, text, x, y, color.toInt(), shadow);
	}

	public static void drawCenteredString(GuiGraphics graphics, Font renderer, String text, int x, int y, int color, boolean shadow) {
		if (shadow) {
			graphics.drawCenteredString(renderer, text, x, y, color);
		} else drawString(graphics, text, (float) (x - renderer.width(text) / 2), (float) y, color, shadow);
	}


	public static void drawString(GuiGraphics graphics, String text, float x, float y, int color, boolean shadow) {
		graphics.drawString(Minecraft.getInstance().font, text, (int) x, (int) y, color, shadow);
	}

	public static void drawString(GuiGraphics graphics, String text, float x, float y, Color color, boolean shadow) {
		drawString(graphics, text, x, y, color.toInt(), shadow);
	}
}
