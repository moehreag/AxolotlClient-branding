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

package io.github.axolotlclient.modules.screenshotUtils;

import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class LoadingImageScreen extends Screen {

	private static final int bgColor = Colors.DARK_GRAY.toInt();
	private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

	private final Screen parent;
	private final CompletableFuture<Void> future;
	private final boolean freeOnClose;
	private final float loadStart = Util.getMeasuringTimeMs();

	LoadingImageScreen(Screen parent, CompletableFuture<Void> future, boolean freeOnClose) {
		super(new TranslatableText("gallery.image.loading.title"));
		this.parent = parent;
		this.future = future;
		this.freeOnClose = freeOnClose;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	protected void init() {

		int buttonWidth = 75;
		int imageWidth = width - 20 - buttonWidth - 4;
		int imageHeight = height - 33 * 2;

		addButton(new LoadingWidget(imageWidth, imageHeight)).setPosition(10, 36);
		addButton(new LoadingWidget(buttonWidth, 20)).setPosition(10 + imageWidth + 4, 36);
		addButton(new LoadingWidget(buttonWidth, 20)).setPosition(10 + imageWidth + 4, 36 + 20 + 4);

		addButton(new ButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, ScreenTexts.BACK, b -> onClose()));
	}

	@Override
	public void onClose() {
		if (freeOnClose) {
			future.cancel(false);
		}
		client.openScreen(parent);
	}

	private void drawHorizontalGradient(MatrixStack guiGraphics, int x1, int y1, int y2, int x2) {
		BufferBuilder consumer = Tessellator.getInstance().getBuffer();
		Matrix4f matrix4f = guiGraphics.peek().getModel();
		consumer.vertex(matrix4f, x1, y1, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
		consumer.vertex(matrix4f, x1, y2, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
		consumer.vertex(matrix4f, x2, y2, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
		consumer.vertex(matrix4f, x2, y1, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
		Tessellator.getInstance().draw();
	}

	private double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
	}

	private int lerp(float delta, int start, int end) {
		return (int) MathHelper.clamp(MathHelper.lerp(delta, start, end), start, end);
	}

	private class LoadingWidget extends AbstractButtonWidget {

		public LoadingWidget(int width, int height) {
			super(0, 0, width, height, LiteralText.EMPTY);
			active = false;
		}

		@Override
		public void renderButton(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
			fill(guiGraphics, x, y, x + getWidth(), y + getHeight(), bgColor);
			drawHorizontalGradient(guiGraphics, x, y, y + getHeight(), lerp((float) easeInOutCubic((Util.getMeasuringTimeMs() - loadStart) % 1000f / 1000f), x, x + getWidth()));
		}

		@Override
		protected MutableText getNarrationMessage() {
			return LiteralText.EMPTY.copy();
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
