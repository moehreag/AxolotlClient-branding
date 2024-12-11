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
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.resource.Identifier;

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

	public sealed interface GuiSpriteScaling {
	}
	public record Stretch() implements GuiSpriteScaling{

	}
	public record Tile(int width, int height) implements GuiSpriteScaling{

	}
	public record NineSlice(int width, int height, Border border, boolean stretchInner) implements GuiSpriteScaling {
	}
	public record Border(int left, int right, int top, int bottom){}

	public static void blitSprite(Identifier texture, int i, int j, int k, int l, GuiSpriteScaling guiSpriteScaling) {
		blitSprite(texture, i, j, k, l, -1, guiSpriteScaling);
	}

	public static void blitSprite(Identifier resourceLocation, int i, int j, int k, int l, int m, GuiSpriteScaling guiSpriteScaling) {
		if (guiSpriteScaling instanceof Stretch) {
			blitSprite(resourceLocation, i, j, k, l, m);
		} else if (guiSpriteScaling instanceof Tile tile) {
			blitTiledSprite(resourceLocation, i, j, k, l, 0, 0, tile.width(), tile.height(), tile.width(), tile.height(), m);
		} else if (guiSpriteScaling instanceof NineSlice nineSlice) {
			blitNineSlicedSprite(resourceLocation, nineSlice, i, j, k, l, m);
		}
	}

	public static void blitSprite(
		Identifier resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, GuiSpriteScaling guiSpriteScaling
	) {
		if (guiSpriteScaling instanceof Stretch) {
			blitSprite(resourceLocation, i, j, k, l, m, n, o, p, -1);
		} else {
			enableScissor(m, n, m + o, n + p);
			blitSprite(resourceLocation, m - k, n - l, i, j, -1);
			disableScissor();
		}
	}

	public static void blitSprite(Identifier texture, int i, int j, int k, int l) {
		blitSprite(texture, i, j, k, l, -1);
	}

	public static void blitSprite(Identifier texture, int x, int y, int width, int height, int color) {
		if (width != 0 && height != 0) {
			innerBlit(
				texture,
				x,
				x + width,
				y,
				y + height,
				0, 1, 0, 1,
				color
			);
		}
	}

	private static void blitSprite(
		Identifier texture, int i, int j, int k, int l, int m, int n, int o, int p, int q
	) {
		if (o != 0 && p != 0) {
			innerBlit(
				texture,
				m,
				m + o,
				n,
				n + p,
				(float) k /i, (float) (k + o) /i, (float) l /j, (float) (l + p) /j,
				q
			);
		}
	}

	private static void blitNineSlicedSprite(
		Identifier texture, NineSlice nineSlice, int i, int j, int k, int l, int m
	) {
		Border border = nineSlice.border();
		int n = Math.min(border.left(), k / 2);
		int o = Math.min(border.right(), k / 2);
		int p = Math.min(border.top(), l / 2);
		int q = Math.min(border.bottom(), l / 2);
		if (k == nineSlice.width() && l == nineSlice.height()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, l, m);
		} else if (l == nineSlice.height()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, l, m);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				i + n,
				j,
				k - o - n,
				l,
				n,
				0,
				nineSlice.width() - o - n,
				nineSlice.height(),
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, i + k - o, j, o, l, m);
		} else if (k == nineSlice.width()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, i, j, k, p, m);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				i,
				j + p,
				k,
				l - q - p,
				0,
				p,
				nineSlice.width(),
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, i, j + l - q, k, q, m);
		} else {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, i, j, n, p, m);
			blitNineSliceInnerSegment(
				texture, nineSlice, i + n, j, k - o - n, p, n, 0, nineSlice.width() - o - n, p, nineSlice.width(), nineSlice.height(), m
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, 0, i + k - o, j, o, p, m);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - q, i, j + l - q, n, q, m);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				i + n,
				j + l - q,
				k - o - n,
				q,
				n,
				nineSlice.height() - q,
				nineSlice.width() - o - n,
				q,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			blitSprite(
				texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - o, nineSlice.height() - q, i + k - o, j + l - q, o, q, m
			);
			blitNineSliceInnerSegment(
				texture, nineSlice, i, j + p, n, l - q - p, 0, p, n, nineSlice.height() - q - p, nineSlice.width(), nineSlice.height(), m
			);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				i + n,
				j + p,
				k - o - n,
				l - q - p,
				n,
				p,
				nineSlice.width() - o - n,
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				i + k - o,
				j + p,
				o,
				l - q - p,
				nineSlice.width() - o,
				p,
				o,
				nineSlice.height() - q - p,
				nineSlice.width(),
				nineSlice.height(),
				m
			);
		}
	}

	private static void blitNineSliceInnerSegment(
		Identifier texture,
		NineSlice nineSlice,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int o,
		int p,
		int q,
		int r,
		int s
	) {
		if (k > 0 && l > 0) {
			if (nineSlice.stretchInner()) {
				innerBlit(
					texture,
					i,
					i + k,
					j,
					j + l,
					(float) m /q, (float) (m + o) /q, (float) n /r, (float) (n + p) /r,
					s
				);
			} else {
				blitTiledSprite(texture, i, j, k, l, m, n, o, p, q, r, s);
			}
		}
	}

	private static void blitTiledSprite(
		Identifier texture,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		int width,
		int height,
		int q,
		int r,
		int s
	) {
		if (k > 0 && l > 0) {
			if (width > 0 && height > 0) {
				for (int t = 0; t < k; t += width) {
					int u = Math.min(width, k - t);

					for (int v = 0; v < l; v += height) {
						int w = Math.min(height, l - v);
						blitSprite(texture, q, r, m, n, i + t, j + v, u, w, s);
					}
				}
			} else {
				throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + width + "x" + height);
			}
		}
	}

	private static void innerBlit(
		Identifier resourceLocation, int x, int x2, int y, int y2, float u, float u2, float v, float v2, int color
	) {
		Minecraft.getInstance().getTextureManager().bind(resourceLocation);
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		int a = color >> 24 & 255;
		GlStateManager.color4f(r, g, b, a);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y2, 0.0F).texture(u, v2).nextVertex();
		bufferBuilder.vertex(x2, y2, 0.0F).texture(u2, v2).nextVertex();
		bufferBuilder.vertex(x2, y, 0.0F).texture(u2, v).nextVertex();
		bufferBuilder.vertex(x, y, 0.0F).texture(u, v).nextVertex();
		tessellator.end();
	}
}
