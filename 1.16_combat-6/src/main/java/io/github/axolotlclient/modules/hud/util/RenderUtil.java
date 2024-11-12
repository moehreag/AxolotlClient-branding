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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import lombok.experimental.UtilityClass;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@UtilityClass
public class RenderUtil {

	/**
	 * Fills an outline with x/y width/height values
	 */
	public void drawOutline(MatrixStack matrices, int x, int y, int width, int height, int color) {
		fillOutline(matrices, x, y, x + width, y + height, color);
	}

	/**
	 * Draws an outline with raw x/y values
	 */
	public void fillOutline(MatrixStack matrices, int x, int y, int x2, int y2, int color) {
		// Top line
		fill(matrices, x, y, x2, y + 1, color);
		// Left line
		fill(matrices, x, y + 1, x + 1, y2 - 1, color);
		// Right line
		fill(matrices, x2 - 1, y + 1, x2, y2 - 1, color);
		// Bottom line
		fill(matrices, x, y2 - 1, x2, y2, color);
	}

	/**
	 * Fills in a rectangle with a color. Uses raw x/y values. x/y
	 */
	public void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
		fill(matrices.peek().getModel(), x1, y1, x2, y2, color);
	}

	public void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
		int i;
		if (x1 < x2) {
			i = x1;
			x1 = x2;
			x2 = i;
		}
		if (y1 < y2) {
			i = y1;
			y1 = y2;
			y2 = i;
		}
		float a = (float) (color >> 24 & 0xFF) / 255.0f;
		float r = (float) (color >> 16 & 0xFF) / 255.0f;
		float g = (float) (color >> 8 & 0xFF) / 255.0f;
		float b = (float) (color & 0xFF) / 255.0f;
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(r, g, b, a).next();
		Tessellator.getInstance().draw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public void drawOutline(MatrixStack matrices, int x, int y, int width, int height, Color color) {
		fillOutline(matrices, x, y, x + width, y + height, color);
	}

	public void fillOutline(MatrixStack matrices, int x, int y, int x2, int y2, Color color) {
		// Top line
		fill(matrices, x, y, x2, y + 1, color);
		// Left line
		fill(matrices, x, y + 1, x + 1, y2 - 1, color);
		// Right line
		fill(matrices, x2 - 1, y + 1, x2, y2 - 1, color);
		// Bottom line
		fill(matrices, x, y2 - 1, x2, y2, color);
	}

	public void fill(MatrixStack matrix, int x1, int y1, int x2, int y2, Color color) {
		fill(matrix.peek().getModel(), x1, y1, x2, y2, color);
	}

	public void fill(Matrix4f matrix, int x1, int y1, int x2, int y2, Color color) {
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.color4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		int colorInt = colorPreRender(color);
		float a = (float) (colorInt >> 24 & 0xFF) / 255.0f;
		float r = (float) (colorInt >> 16 & 0xFF) / 255.0f;
		float g = (float) (colorInt >> 8 & 0xFF) / 255.0f;
		float b = (float) (colorInt & 0xFF) / 255.0f;
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y2, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y1, 0.0f).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(r, g, b, a).next();
		Tessellator.getInstance().draw();
		colorPostRender(color);
	}

	public int colorPreRender(Color color) {
		RenderSystem.color4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();

		return color.toInt();
	}

	public void colorPostRender(Color color) {
		RenderSystem.enableTexture();
		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.disableBlend();
	}

	/**
	 * Draws a vertical line
	 */
	public void drawVerticalLine(MatrixStack matrices, int x, int y, int height, int color) {
		drawRectangle(matrices, x, y, 1, height, color);
	}

	/**
	 * Fills in a rectangle with a color. x/y width/height
	 */
	public void drawRectangle(MatrixStack matrices, int x, int y, int width, int height, int color) {
		fill(matrices, x, y, x + width, y + height, color);
	}

	/**
	 * Draws a horizontal line
	 */
	public void drawHorizontalLine(MatrixStack matrices, int x, int y, int width, int color) {
		drawRectangle(matrices, x, y, width, 1, color);
	}

	public void drawRectangle(MatrixStack matrices, int x, int y, int width, int height, Color color) {
		fill(matrices, x, y, x + width, y + height, color);
	}

	public void fillBlend(MatrixStack matrices, Rectangle rect, Color color) {
		fillBlend(matrices, rect.x, rect.y, rect.width, rect.height, color);
	}

	public void fillBlend(MatrixStack matrices, int x, int y, int width, int height, Color color) {
		fillBlend(matrices.peek().getModel(), x, y, x + width, y + height, color.toInt());
	}

	public void fillBlend(Matrix4f matrix, int x1, int y1, int x2, int y2, int color) {
		float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
		float red = (float) (color >> 16 & 0xFF) / 255.0F;
		float green = (float) (color >> 8 & 0xFF) / 255.0F;
		float blue = (float) (color & 0xFF) / 255.0F;
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.disableTexture();
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(red, green, blue, alpha).next();
		bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(red, green, blue, alpha).next();
		bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(red, green, blue, alpha).next();
		bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(red, green, blue, alpha).next();
		Tessellator.getInstance().draw();
		RenderSystem.enableTexture();
	}
}
