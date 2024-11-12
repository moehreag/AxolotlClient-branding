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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class DrawUtil extends GuiElement {

	public static void fillRect(Rectangle rectangle, Color color) {
		fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void fillRect(int x, int y, int width, int height, int color) {
		GuiElement.fill(x, y, x + width, y + height, color);
	}

	public static void fillRect(int x, int y, int width, int height, Color color) {
		fillRect(x, y, x + width, y + height, color.toInt());
	}

	public static void outlineRect(Rectangle rectangle, Color color) {
		outlineRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, color.toInt());
	}

	public static void outlineRect(int x, int y, int width, int height, int color) {
		fillRect(x, y, 1, height - 1, color);
		fillRect(x + width - 1, y + 1, 1, height - 1, color);
		fillRect(x + 1, y, width - 1, 1, color);
		fillRect(x, y + height - 1, width - 1, 1, color);
	}

	public static void drawCenteredString(TextRenderer renderer, String text, int x, int y, Color color,
										  boolean shadow) {
		drawCenteredString(renderer, text, x, y, color.toInt(), shadow);
	}

	public static void drawCenteredString(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
		drawString(text, (float) (x - renderer.getWidth(text) / 2), (float) y, color, shadow);
	}

	public static void drawString(String text, float x, float y, int color, boolean shadow) {
		GlStateManager.enableTexture();
		Minecraft.getInstance().textRenderer.draw(text, x, y, color, shadow);
	}

	public static void drawString(String text, float x, float y, Color color, boolean shadow) {
		drawString(text, x, y, color.toInt(), shadow);
	}

	public static void drawString(TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow) {
		drawString(text, x, y, color, shadow);
	}

	public static void drawScrollableText(TextRenderer textRenderer, String text, int left, int top, int right, int bottom, int color) {
		int i = textRenderer.getWidth(text);
		int j = (top + bottom - 9) / 2 + 1;
		int k = right - left;
		if (i > k) {
			int l = i - k;
			double d = (double) Minecraft.getTime() / 1000.0;
			double e = Math.max((double) l * 0.5, 3.0);
			double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
			double g = f * l;
			enableScissor(left, top, right, bottom);
			textRenderer.drawWithShadow(text, left - (int) g, j, color);
			disableScissor();
		} else {
			drawCenteredString(textRenderer, text, (left + right) / 2, j, color, true);
		}
	}

	public static void enableScissor(int x1, int y1, int x2, int y2) {
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.pushScissor(x1, y1, x2 - x1, y2 - y1);
	}

	public static void disableScissor() {
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.popScissor();
	}
}
