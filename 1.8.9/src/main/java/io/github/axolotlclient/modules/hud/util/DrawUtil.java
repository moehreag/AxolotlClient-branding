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

	public static int drawString(String text, float x, float y, int color, boolean shadow) {
		GlStateManager.enableTexture();
		return Minecraft.getInstance().textRenderer.draw(text, x, y, color, shadow);
	}

	public static int drawString(String text, float x, float y, Color color, boolean shadow) {
		return drawString(text, x, y, color.toInt(), shadow);
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

	public record Stretch() implements GuiSpriteScaling {

	}

	public record Tile(int width, int height) implements GuiSpriteScaling {

	}

	public record NineSlice(int width, int height, Border border, boolean stretchInner) implements GuiSpriteScaling {
		public NineSlice(int width, int height, Border border) {
			this(width, height, border, false);
		}
		public NineSlice(int width, int height, int borderSize) {
			this(width, height, new Border(borderSize));
		}
	}

	public record Border(int left, int right, int top, int bottom) {
		public Border(int size) {
			this(size, size, size, size);
		}
	}

	public static void blitSprite(Identifier texture, int x, int y, int width, int height, GuiSpriteScaling guiSpriteScaling) {
		blitSprite(texture, x, y, width, height, -1, guiSpriteScaling);
	}

	public static void blitSprite(Identifier resourceLocation, int x, int y, int width, int height, int color, GuiSpriteScaling guiSpriteScaling) {
		if (guiSpriteScaling instanceof Stretch) {
			blitSprite(resourceLocation, x, y, width, height, color);
		} else if (guiSpriteScaling instanceof Tile tile) {
			blitTiledSprite(resourceLocation, x, y, width, height, 0, 0, tile.width(), tile.height(), tile.width(), tile.height(), color);
		} else if (guiSpriteScaling instanceof NineSlice nineSlice) {
			blitNineSlicedSprite(resourceLocation, nineSlice, x, y, width, height, color);
		}
	}

	public static void blitSprite(
		Identifier resourceLocation, int texWidth, int texHeight, int u, int v, int x, int y, int width, int height, GuiSpriteScaling guiSpriteScaling
	) {
		if (guiSpriteScaling instanceof Stretch) {
			blitSprite(resourceLocation, texWidth, texHeight, u, v, x, y, width, height, -1);
		} else {
			enableScissor(x, y, x + width, y + height);
			blitSprite(resourceLocation, x - u, y - v, texWidth, texHeight, -1);
			disableScissor();
		}
	}

	public static void blitSprite(Identifier texture, int x, int y, int width, int height) {
		blitSprite(texture, x, y, width, height, -1);
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
		Identifier texture, int texWidth, int texHeight, int u, int v, int x, int y, int width, int height, int color
	) {
		if (width != 0 && height != 0) {
			innerBlit(
				texture,
				x,
				x + width,
				y,
				y + height,
				(float) u / texWidth, (float) (u + width) / texWidth, (float) v / texHeight, (float) (v + height) / texHeight,
				color
			);
		}
	}

	private static void blitNineSlicedSprite(
		Identifier texture, NineSlice nineSlice, int x, int y, int width, int height, int color
	) {
		Border border = nineSlice.border();
		int borderLeft = Math.min(border.left(), width / 2);
		int borderRight = Math.min(border.right(), width / 2);
		int borderTop = Math.min(border.top(), height / 2);
		int borderBottom = Math.min(border.bottom(), height / 2);
		if (width == nineSlice.width() && height == nineSlice.height()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, height, color);
		} else if (height == nineSlice.height()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, x, y, borderLeft, height, color);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				x + borderLeft,
				y,
				width - borderRight - borderLeft,
				height,
				borderLeft,
				0,
				nineSlice.width() - borderRight - borderLeft,
				nineSlice.height(),
				nineSlice.width(),
				nineSlice.height(),
				color
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - borderRight, 0, x + width - borderRight, y, borderRight, height, color);
		} else if (width == nineSlice.width()) {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, x, y, width, borderTop, color);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				x,
				y + borderTop,
				width,
				height - borderBottom - borderTop,
				0,
				borderTop,
				nineSlice.width(),
				nineSlice.height() - borderBottom - borderTop,
				nineSlice.width(),
				nineSlice.height(),
				color
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - borderBottom, x, y + height - borderBottom, width, borderBottom, color);
		} else {
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, 0, x, y, borderLeft, borderTop, color);
			blitNineSliceInnerSegment(
				texture, nineSlice, x + borderLeft, y, width - borderRight - borderLeft, borderTop, borderLeft, 0, nineSlice.width() - borderRight - borderLeft, borderTop, nineSlice.width(), nineSlice.height(), color
			);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - borderRight, 0, x + width - borderRight, y, borderRight, borderTop, color);
			blitSprite(texture, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - borderBottom, x, y + height - borderBottom, borderLeft, borderBottom, color);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				x + borderLeft,
				y + height - borderBottom,
				width - borderRight - borderLeft,
				borderBottom,
				borderLeft,
				nineSlice.height() - borderBottom,
				nineSlice.width() - borderRight - borderLeft,
				borderBottom,
				nineSlice.width(),
				nineSlice.height(),
				color
			);
			blitSprite(
				texture, nineSlice.width(), nineSlice.height(), nineSlice.width() - borderRight, nineSlice.height() - borderBottom, x + width - borderRight, y + height - borderBottom, borderRight, borderBottom, color
			);
			blitNineSliceInnerSegment(
				texture, nineSlice, x, y + borderTop, borderLeft, height - borderBottom - borderTop, 0, borderTop, borderLeft, nineSlice.height() - borderBottom - borderTop, nineSlice.width(), nineSlice.height(), color
			);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				x + borderLeft,
				y + borderTop,
				width - borderRight - borderLeft,
				height - borderBottom - borderTop,
				borderLeft,
				borderTop,
				nineSlice.width() - borderRight - borderLeft,
				nineSlice.height() - borderBottom - borderTop,
				nineSlice.width(),
				nineSlice.height(),
				color
			);
			blitNineSliceInnerSegment(
				texture,
				nineSlice,
				x + width - borderRight,
				y + borderTop,
				borderRight,
				height - borderBottom - borderTop,
				nineSlice.width() - borderRight,
				borderTop,
				borderRight,
				nineSlice.height() - borderBottom - borderTop,
				nineSlice.width(),
				nineSlice.height(),
				color
			);
		}
	}

	private static void blitNineSliceInnerSegment(
		Identifier texture,
		NineSlice nineSlice,
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int regionWidth,
		int regionHeight,
		int texWidth,
		int texHeight,
		int color
	) {
		if (width > 0 && height > 0) {
			if (nineSlice.stretchInner()) {
				innerBlit(
					texture,
					x,
					x + width,
					y,
					y + height,
					(float) u / texWidth, (float) (u + regionWidth) / texWidth, (float) v / texHeight, (float) (v + regionHeight) / texHeight,
					color
				);
			} else {
				blitTiledSprite(texture, x, y, width, height, u, v, regionWidth, regionHeight, texWidth, texHeight, color);
			}
		}
	}

	private static void blitTiledSprite(
		Identifier texture,
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int spriteWidth,
		int spriteHeight,
		int nineSliceWidth,
		int nineSliceHeight,
		int color
	) {
		if (width > 0 && height > 0) {
			if (spriteWidth > 0 && spriteHeight > 0) {
				for (int xStep = 0; xStep < width; xStep += spriteWidth) {
					int i = Math.min(spriteWidth, width - xStep);

					for (int yStep = 0; yStep < height; yStep += spriteHeight) {
						int w = Math.min(spriteHeight, height - yStep);
						blitSprite(texture, nineSliceWidth, nineSliceHeight, u, v, x + xStep, y + yStep, i, w, color);
					}
				}
			} else {
				throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + spriteWidth + "x" + spriteHeight);
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
