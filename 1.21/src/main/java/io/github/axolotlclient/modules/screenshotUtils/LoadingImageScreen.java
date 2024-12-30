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
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
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
	protected void init() {
		HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
		LinearLayoutWidget header = layout.addToHeader(LinearLayoutWidget.createVertical()).setSpacing(4);
		header.getDefaultSettings().alignHorizontallyCenter();
		header.add(new TextWidget(getTitle(), textRenderer));

		int buttonWidth = 75;
		int imageWidth = layout.getWidth() - 20 - buttonWidth - 4;
		int imageHeight = layout.getContentsHeight();

		var contents = layout.addToContents(LinearLayoutWidget.createHorizontal().setSpacing(4));
		var footer = layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(4));
		contents.add(new LoadingWidget(imageWidth, imageHeight));
		var actions = contents.add(LinearLayoutWidget.createVertical()).setSpacing(4);
		actions.add(new LoadingWidget(buttonWidth, 20));
		actions.add(new LoadingWidget(buttonWidth, 20));

		footer.add(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addDrawableSelectableElement);
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
		consumer.xyz(matrix4f, x1, y1, 0).color(LoadingImageScreen.bgColor);
		consumer.xyz(matrix4f, x1, y2, 0).color(LoadingImageScreen.bgColor);
		consumer.xyz(matrix4f, x2, y2, 0).color(LoadingImageScreen.accent);
		consumer.xyz(matrix4f, x2, y1, 0).color(LoadingImageScreen.accent);
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
			guiGraphics.fill(getX(), getY(), getXEnd(), getYEnd(), bgColor);
			drawHorizontalGradient(guiGraphics, getX(), getY(), getYEnd(), lerp((float) easeInOutCubic((Util.getMeasuringTimeMs() - loadStart) % 1000f / 1000f), getX(), getXEnd()));
		}

		@Override
		protected void updateNarration(NarrationMessageBuilder builder) {

		}
	}
}
