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

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class LoadingImageScreen extends Screen {

	private static final int bgColor = Colors.DARK_GRAY.toInt();
	private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

	private final Screen parent;
	private final CompletableFuture<Void> future;
	private final boolean freeOnClose;
	private final float loadStart = Util.getMeasuringTimeMs();

	LoadingImageScreen(Screen parent, CompletableFuture<Void> future, boolean freeOnClose) {
		super(Text.translatable("gallery.image.loading.title"));
		this.parent = parent;
		this.future = future;
		this.freeOnClose = freeOnClose;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), width/2, 33/2 - textRenderer.fontHeight/2, -1);
	}

	@Override
	protected void init() {

		int buttonWidth = 75;
		int imageWidth = width - 20 - buttonWidth - 4;
		int imageHeight = height - 33*2;

		addDrawableChild(new LoadingWidget(imageWidth, imageHeight)).setPosition(10, 36);
		addDrawableChild(new LoadingWidget(buttonWidth, 20)).setPosition(10 + imageWidth + 4, 36);
		addDrawableChild(new LoadingWidget(buttonWidth, 20)).setPosition(10 + imageWidth + 4, 36 +20 + 4);

		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).positionAndSize(width/2-75, height-33/2 - 10, 150, 20).build());
	}

	@Override
	public void closeScreen() {
		if (freeOnClose) {
			future.cancel(false);
		}
		client.setScreen(parent);
	}

	private void drawHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int y2, int x2) {
		VertexConsumer consumer = client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getGui());
		Matrix4f matrix4f = guiGraphics.getMatrices().peek().getModel();
		consumer.vertex(matrix4f, x1, y1, 0).color(LoadingImageScreen.bgColor);
		consumer.vertex(matrix4f, x1, y2, 0).color(LoadingImageScreen.bgColor);
		consumer.vertex(matrix4f, x2, y2, 0).color(LoadingImageScreen.accent);
		consumer.vertex(matrix4f, x2, y1, 0).color(LoadingImageScreen.accent);
	}

	private double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
	}

	private int lerp(float delta, int start, int end) {
		return MathHelper.clamp(MathHelper.lerp(delta, start, end), start, end);
	}

	private class LoadingWidget extends ClickableWidget {

		public LoadingWidget(int width, int height) {
			super(0, 0, width, height, Text.empty());
			active = false;
		}

		@Override
		protected void drawWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
			drawHorizontalGradient(guiGraphics, getX(), getY(), getY() + getHeight(), lerp((float) easeInOutCubic((Util.getMeasuringTimeMs() - loadStart) % 1000f / 1000f), getX(), getX() + getWidth()));
		}

		@Override
		protected void updateNarration(NarrationMessageBuilder builder) {

		}
	}
}
