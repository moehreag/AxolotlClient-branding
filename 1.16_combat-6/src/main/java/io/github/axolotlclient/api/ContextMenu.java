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

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class ContextMenu implements ParentElement, Drawable {

	private final List<AbstractButtonWidget> children;
	private boolean dragging;
	private Element focused;

	private int x;
	private int y;
	private final int width, height;
	private boolean rendering;

	protected ContextMenu(List<AbstractButtonWidget> items) {
		children = items;
		int width = 0;
		int height = 0;
		for (AbstractButtonWidget d : children) {
			d.y = height;
			height += d.getHeight();
			width = Math.max(width, d.getWidth());
		}
		this.width = width;
		this.height = height;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addEntry(AbstractButtonWidget entry) {
		children.add(entry);
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	public List<AbstractButtonWidget> entries() {
		return children;
	}

	@Override
	public boolean isDragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Nullable
	@Override
	public Element getFocused() {
		return focused;
	}

	@Override
	public void setFocused(@Nullable Element child) {
		this.focused = child;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!rendering) {
			y = mouseY;
			x = mouseX;
			rendering = true;
		}
		final int yStart = Math.min(y + 2, MinecraftClient.getInstance().getWindow().getScaledHeight() - height - 2);
		final int xStart = Math.min(x + 2, MinecraftClient.getInstance().getWindow().getScaledWidth() - width - 2);
		int y = yStart + 1;
		int width = 0;
		for (AbstractButtonWidget d : children) {
			d.x = (xStart + 1);
			d.y = (y);
			y += d.getHeight();
			width = Math.max(width, d.getWidth());
		}
		matrices.push();
		matrices.translate(0, 0, 200);
		DrawableHelper.fill(matrices, xStart, yStart, xStart + width + 1, y, 0xDD1E1F22);
		DrawUtil.outlineRect(matrices, xStart, yStart, width + 1, y - yStart + 1, -1);
		for (AbstractButtonWidget c : children) {
			c.setWidth(width);
			c.render(matrices, mouseX, mouseY, delta);
		}
		matrices.pop();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoveredElement(mouseX, mouseY).isPresent();
	}

	public static class Builder {

		private final MinecraftClient client = MinecraftClient.getInstance();

		private final List<AbstractButtonWidget> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(Text name, ButtonWidget.PressAction action) {
			elements.add(new ContextMenuEntryWithAction(name, action));
			return this;
		}

		public Builder entry(AbstractButtonWidget widget) {
			elements.add(widget);
			return this;
		}

		public Builder spacer() {
			elements.add(new ContextMenuEntry(new LiteralText("-----")) {
				@Override
				protected MutableText getNarrationMessage() {
					return LiteralText.EMPTY.copy();
				}
			});
			return this;
		}

		public Builder title(Text title) {
			elements.add(new ContextMenuEntry(title));
			return this;
		}

		public ContextMenu build() {
			return new ContextMenu(elements);
		}

	}

	public static class ContextMenuEntry extends AbstractButtonWidget {

		private final MinecraftClient client = MinecraftClient.getInstance();

		public ContextMenuEntry(Text content) {
			super(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(content), 11, content);
		}

		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			drawCenteredText(matrices, client.textRenderer, getMessage(), x + getWidth() / 2, y, 0xDDDDDD);
		}

		@Override
		protected MutableText getNarrationMessage() {
			return getMessage().copy();
		}

		@Override
		protected boolean clicked(double mouseX, double mouseY) {
			return false;
		}
	}

	public static class ContextMenuEntryWithAction extends ButtonWidget {

		public ContextMenuEntryWithAction(Text message, PressAction onPress) {
			super(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(message) + 4, 11, message, onPress);
		}

		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {

			if (isHovered()) {
				fill(matrices, x, y, x + getWidth(), y + getHeight(), 0x55ffffff);
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.active ? 16777215 : 10526880;
			drawScrollableText(matrices, MinecraftClient.getInstance().textRenderer, 2, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
		}

		protected void drawScrollableText(MatrixStack matrices, TextRenderer textRenderer, int xOffset, int color) {
			int x = this.x + xOffset;
			int xEnd = this.x + this.getWidth() - xOffset;
			DrawUtil.drawScrollableText(matrices, textRenderer, this.getMessage(), x, this.y, xEnd, this.y + this.getHeight(), color);
		}
	}
}
