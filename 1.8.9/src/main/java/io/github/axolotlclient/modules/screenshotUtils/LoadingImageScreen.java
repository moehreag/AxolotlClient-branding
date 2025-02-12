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

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class LoadingImageScreen extends Screen {

	private static final int bgColor = Colors.DARK_GRAY.toInt();
	private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

	private final Screen parent;
	private final CompletableFuture<Void> future;
	private final boolean freeOnClose;
	private final float loadStart = Minecraft.getTime();
	private final String title;

	LoadingImageScreen(Screen parent, CompletableFuture<Void> future, boolean freeOnClose) {
		super();
		this.title = I18n.translate("gallery.image.loading.title");
		this.parent = parent;
		this.future = future;
		this.freeOnClose = freeOnClose;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, title, width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {

		int buttonWidth = 75;
		int imageWidth = width - 20 - buttonWidth - 4;
		int imageHeight = height - 33 * 2;

		buttons.add(new LoadingWidget(imageWidth, imageHeight).setPosition(10, 36));
		buttons.add(new LoadingWidget(buttonWidth, 20).setPosition(10 + imageWidth + 4, 36));
		buttons.add(new LoadingWidget(buttonWidth, 20).setPosition(10 + imageWidth + 4, 36 + 20 + 4));

		buttons.add(new ButtonWidget(0, width / 2 - 75, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			if (freeOnClose) {
				future.cancel(false);
			}
			minecraft.openScreen(parent);
		}
	}

	private void drawHorizontalGradient(int x1, int y1, int y2, int x2) {
		BufferBuilder consumer = Tessellator.getInstance().getBuilder();
		consumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		consumer.vertex(x1, y1, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
		consumer.vertex(x1, y2, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
		consumer.vertex(x2, y2, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
		consumer.vertex(x2, y1, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
		Tessellator.getInstance().end();
	}

	private double easeInOutCubic(double x) {
		return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
	}

	private int lerp(float delta, int start, int end) {
		return (int) MathHelper.clamp(Util.lerp(delta, start, end), start, end);
	}

	private class LoadingWidget extends ButtonWidget {

		public LoadingWidget(int width, int height) {
			super(99, 0, 0, width, height, "");
			active = false;
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			fill(x, y, x + getWidth(), y + getHeight(), bgColor);
			drawHorizontalGradient(x, y, y + getHeight(), lerp((float) easeInOutCubic((Minecraft.getTime() - loadStart) % 1000f / 1000f), x, x + getWidth()));
		}

		private int getHeight() {
			return height;
		}

		public LoadingWidget setPosition(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}
	}
}
