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

package io.github.axolotlclient.api;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ClickableWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.system.SoundManager;
import net.minecraft.util.math.MathHelper;

public class NewsScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {


	private final Screen parent;

	public NewsScreen(Screen parent) {
		super(I18n.translate("api.notes.title"));

		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);

		drawCenteredString(textRenderer, title, width / 2, 20, -1);
	}

	@Override
	public void init() {
		addDrawableChild(new NewsWidget(25, 35, width - 50, height - 100, GlobalDataRequest.get().notes().replaceAll("([^\n])\n([^\n])", "$1 $2")));

		addDrawableChild(new VanillaButtonWidget(width / 2 - 100, height - 45, 200, 20, I18n.translate("gui.back"), buttonWidget -> minecraft.openScreen(parent)));
	}

	private class NewsWidget extends AbstractTextAreaWidget {

		private final List<String> lines;
		private final int contentHeight;

		public NewsWidget(int x, int y, int width, int height, String component) {
			super(x, y, width, height, component);
			lines = textRenderer.split(getMessage(), getWidth() - 4);
			contentHeight = lines.size() * textRenderer.fontHeight;
		}

		@Override
		protected int getInnerHeight() {
			return contentHeight;
		}

		@Override
		protected void renderContents(int mouseX, int mouseY, float partialTick) {
			int y = getY() + 2;
			for (String chsq : lines) {
				drawString(textRenderer, chsq, getX() + 2, y, -1);
				y += textRenderer.fontHeight;
			}
		}

		@Override
		protected double scrollRate() {
			return textRenderer.fontHeight;
		}
	}

	public abstract static class AbstractTextAreaWidget extends AbstractScrollArea {
		private static final int INNER_PADDING = 4;

		public AbstractTextAreaWidget(int i, int j, int k, int l, String component) {
			super(i, j, k, l, component);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean bl = this.updateScrolling(mouseX, mouseY, button);
			return super.mouseClicked(mouseX, mouseY, button) || bl;
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			boolean bl = keyCode == 265;
			boolean bl2 = keyCode == 264;
			if (bl || bl2) {
				double d = this.scrollAmount();
				this.setScrollAmount(this.scrollAmount() + (double) (bl ? -1 : 1) * this.scrollRate());
				if (d != this.scrollAmount()) {
					return true;
				}
			}

			return super.keyPressed(keyCode, scanCode, modifiers);
		}

		@Override
		public void drawWidget(int mouseX, int mouseY, float partialTick) {
			if (this.visible) {
				this.renderBackground();
				DrawUtil.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1);
				GlStateManager.pushMatrix();
				GlStateManager.translated(0.0, -this.scrollAmount(), 0.0);
				this.renderContents(mouseX, mouseY, partialTick);
				GlStateManager.popMatrix();
				DrawUtil.disableScissor();
				renderDecorations();
			}
		}

		protected void renderDecorations() {
			this.renderScrollbar();
		}

		protected int innerPadding() {
			return INNER_PADDING;
		}

		protected int totalInnerPadding() {
			return this.innerPadding() * 2;
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return this.active
				&& this.visible
				&& mouseX >= (double) this.getX()
				&& mouseY >= (double) this.getY()
				&& mouseX < (double) (this.getXEnd() + 6)
				&& mouseY < (double) this.getYEnd();
		}

		@Override
		protected int scrollBarX() {
			return this.getXEnd();
		}

		@Override
		protected int contentHeight() {
			return this.getInnerHeight() + this.totalInnerPadding();
		}

		protected void renderBackground() {
			this.renderBorder(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}

		protected void renderBorder(int x, int y, int width, int height) {
			int i = this.isFocused() ? -1 : -6250336;
			fill(this.getX(), this.getY(), this.getXEnd(), this.getYEnd(), i);
			fill(this.getX() + 1, this.getY() + 1, this.getXEnd() - 1, this.getYEnd() - 1, -16777216);
		}

		protected boolean withinContentAreaTopBottom(int top, int bottom) {
			return (double) bottom - this.scrollAmount() >= (double) this.getY() && (double) top - this.scrollAmount() <= (double) (this.getY() + this.getHeight());
		}

		protected abstract int getInnerHeight();

		protected abstract void renderContents(int mouseX, int mouseY, float partialTick);

		protected int getInnerLeft() {
			return this.getX() + this.innerPadding();
		}

		protected int getInnerTop() {
			return this.getY() + this.innerPadding();
		}

		@Override
		public void playDownSound(SoundManager handler) {
		}
	}

	public abstract static class AbstractScrollArea extends ClickableWidget {
		public static final int SCROLLBAR_WIDTH = 6;
		private double scrollAmount;
		private boolean scrolling;

		public AbstractScrollArea(int i, int j, int k, int l, String component) {
			super(i, j, k, l, component);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double scrollY) {
			if (!this.visible) {
				return false;
			} else {
				this.setScrollAmount(this.scrollAmount() - scrollY * this.scrollRate());
				return true;
			}
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			if (this.scrolling) {
				if (mouseY < (double) this.getY()) {
					this.setScrollAmount(0.0);
				} else if (mouseY > (double) this.getYEnd()) {
					this.setScrollAmount(this.maxScrollAmount());
				} else {
					double d = Math.max(1, this.maxScrollAmount());
					int i = this.scrollerHeight();
					double e = Math.max(1.0, d / (double) (this.getHeight() - i));
					this.setScrollAmount(this.scrollAmount() + dragY * e);
				}

				return true;
			} else {
				return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
			}
		}

		@Override
		public void onRelease(double mouseX, double mouseY) {
			this.scrolling = false;
		}

		public double scrollAmount() {
			return this.scrollAmount;
		}

		public void setScrollAmount(double scrollAmount) {
			this.scrollAmount = MathHelper.clamp(scrollAmount, 0.0, this.maxScrollAmount());
		}

		public boolean updateScrolling(double mouseX, double mouseY, int button) {
			this.scrolling = this.scrollbarVisible()
				&& this.isValidClickButton(button)
				&& mouseX >= (double) this.scrollBarX()
				&& mouseX <= (double) (this.scrollBarX() + 6)
				&& mouseY >= (double) this.getY()
				&& mouseY < (double) this.getYEnd();
			return this.scrolling;
		}

		public void refreshScrollAmount() {
			this.setScrollAmount(this.scrollAmount);
		}

		public int maxScrollAmount() {
			return Math.max(0, this.contentHeight() - this.getHeight());
		}

		protected boolean scrollbarVisible() {
			return this.maxScrollAmount() > 0;
		}

		protected int scrollerHeight() {
			return MathHelper.clamp((int) ((float) (this.getHeight() * this.getHeight()) / (float) this.contentHeight()), 32, this.getHeight() - 8);
		}

		protected int scrollBarX() {
			return this.getXEnd() - 6;
		}

		protected int scrollBarY() {
			return Math.max(this.getY(), (int) this.scrollAmount * (this.getHeight() - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
		}

		protected void renderScrollbar() {
			if (this.scrollbarVisible()) {
				int i = this.scrollBarX();
				int j = this.scrollerHeight();
				int k = this.scrollBarY();
				fill(i, getY(), i + SCROLLBAR_WIDTH, getYEnd(), -16777216);
				fill(i, k, i + SCROLLBAR_WIDTH, k + j, -8355712);
				fill(i, k, i + SCROLLBAR_WIDTH - 1, k + j - 1, -4144960);
			}
		}

		protected int getYEnd() {
			return getY() + getHeight();
		}

		protected abstract int contentHeight();

		protected abstract double scrollRate();

		protected int getXEnd() {
			return getX() + getWidth();
		}
	}
}
