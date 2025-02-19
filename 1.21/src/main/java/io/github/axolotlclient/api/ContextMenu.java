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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class ContextMenu implements ParentElement, Drawable, Selectable {

	private final List<ClickableWidget> children;
	private boolean dragging;
	private Element focused;

	private int x;
	private int y;
	private final int width, height;
	private boolean rendering;

	protected ContextMenu(List<ClickableWidget> items) {
		children = items;
		int width = 0;
		int height = 0;
		for (ClickableWidget d : children) {
			d.setY(height);
			height += d.getHeight();
			width = Math.max(width, d.getWidth());
		}
		this.width = width;
		this.height = height;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addEntry(ClickableWidget entry) {
		children.add(entry);
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	public List<ClickableWidget> entries() {
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
	public void setFocusedChild(@Nullable Element child) {
		if (focused != null) {
			focused.setFocused(false);
		}
		this.focused = child;
		if (focused != null) {
			focused.setFocused(true);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (!rendering) {
			y = mouseY;
			x = mouseX;
			rendering = true;
		}
		final int yStart = Math.min(y + 2, graphics.getScaledWindowHeight() - height - 2);
		final int xStart = Math.min(x + 2, graphics.getScaledWindowWidth() - width - 2);
		int y = yStart + 1;
		int width = 0;
		for (ClickableWidget d : children) {
			d.setX(xStart + 1);
			d.setY(y);
			y += d.getHeight();
			width = Math.max(width, d.getWidth());
		}
		graphics.getMatrices().push();
		graphics.getMatrices().translate(0, 0, 200);
		graphics.fill(xStart, yStart, xStart + width + 1, y, 0xDD1E1F22);
		graphics.drawBorder(xStart, yStart, width + 1, y - yStart + 1, -1);
		for (ClickableWidget c : children) {
			c.setWidth(width);
			c.render(graphics, mouseX, mouseY, delta);
		}
		graphics.getMatrices().pop();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoveredElement(mouseX, mouseY).isPresent();
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {

	}

	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}

	public static class Builder {

		private final MinecraftClient client = MinecraftClient.getInstance();

		private final List<ClickableWidget> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(Text name, ButtonWidget.PressAction action) {
			elements.add(new ContextMenuEntryWithAction(name, action));
			return this;
		}

		public Builder entry(ClickableWidget widget) {
			elements.add(widget);
			return this;
		}

		public Builder spacer() {
			elements.add(new ContextMenuEntry(Text.literal("-----")) {
				@Override
				protected void updateNarration(NarrationMessageBuilder builder) {
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

	public static class ContextMenuEntry extends ClickableWidget {

		private final MinecraftClient client = MinecraftClient.getInstance();

		public ContextMenuEntry(Text content) {
			super(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(content), 11, content);
		}

		@Override
		public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.drawCenteredShadowedText(client.textRenderer, getMessage(), getX() + getWidth() / 2, getY(), 0xDDDDDD);
		}

		@Override
		protected void updateNarration(NarrationMessageBuilder builder) {
			builder.put(NarrationPart.TITLE, getMessage());
		}

		@Override
		protected boolean clicked(double mouseX, double mouseY) {
			return false;
		}
	}

	public static class ContextMenuEntryWithAction extends ButtonWidget {

		public ContextMenuEntryWithAction(Text message, PressAction onPress) {
			super(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(message) + 4, 11, message, onPress, DEFAULT_NARRATION);
		}

		@Override
		public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

			if (isHoveredOrFocused()) {
				graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x55ffffff);
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.active ? 16777215 : 10526880;
			this.drawScrollableText(graphics, MinecraftClient.getInstance().textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
		}
	}
}
