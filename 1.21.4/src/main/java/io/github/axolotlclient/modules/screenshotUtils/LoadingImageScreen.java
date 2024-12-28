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
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class LoadingImageScreen extends Screen {

	private static final int bgColor = Colors.DARK_GRAY.toInt();
	private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

	private final Screen parent;
	private final CompletableFuture<Void> future;
	private final boolean freeOnClose;
	private final float loadStart = Util.getMillis();

	LoadingImageScreen(Screen parent, CompletableFuture<Void> future, boolean freeOnClose) {
		super(Component.translatable("gallery.image.loading.title"));
		this.parent = parent;
		this.future = future;
		this.freeOnClose = freeOnClose;
	}

	@Override
	protected void init() {
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		LinearLayout header = layout.addToHeader(LinearLayout.vertical()).spacing(4);
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(getTitle(), font));

		int buttonWidth = 75;
		int imageWidth = layout.getWidth() - 20 - buttonWidth - 4;
		int imageHeight = layout.getContentHeight();

		var contents = layout.addToContents(LinearLayout.horizontal().spacing(4));
		var footer = layout.addToFooter(LinearLayout.horizontal().spacing(4));
		contents.addChild(new LoadingWidget(imageWidth, imageHeight));
		var actions = contents.addChild(LinearLayout.vertical()).spacing(4);
		actions.addChild(new LoadingWidget(buttonWidth, 20));
		actions.addChild(new LoadingWidget(buttonWidth, 20));

		footer.addChild(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void onClose() {
		if (freeOnClose) {
			future.cancel(false);
		}
		minecraft.setScreen(parent);
	}

	private void drawHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int y2, int x2) {
		VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.gui());
		Matrix4f matrix4f = guiGraphics.pose().last().pose();
		consumer.addVertex(matrix4f, x1, y1, 0).setColor(LoadingImageScreen.bgColor);
		consumer.addVertex(matrix4f, x1, y2, 0).setColor(LoadingImageScreen.bgColor);
		consumer.addVertex(matrix4f, x2, y2, 0).setColor(LoadingImageScreen.accent);
		consumer.addVertex(matrix4f, x2, y1, 0).setColor(LoadingImageScreen.accent);
	}

	private double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
	}

	private int lerp(float delta, int start, int end) {
		return (int) Mth.clamp(Mth.lerp(delta, start, end), start, end);
	}

	private class LoadingWidget extends AbstractWidget {

		public LoadingWidget(int width, int height) {
			super(0, 0, width, height, Component.empty());
			active = false;
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.fill(getX(), getY(), getRight(), getBottom(), bgColor);
			drawHorizontalGradient(guiGraphics, getX(), getY(), getBottom(), lerp((float) easeInOutCubic((Util.getMillis() - loadStart) % 1000f / 1000f), getX(), getRight()));
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}
	}
}
