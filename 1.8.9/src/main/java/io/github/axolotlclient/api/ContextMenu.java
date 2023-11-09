/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class ContextMenu {

	private final List<ButtonWidget> children;
	private int x;
	private int y;

	private int width, height = 0;
	private boolean rendering;

	protected ContextMenu(List<ButtonWidget> items) {
		children = items;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addEntry(ButtonWidget entry) {
		children.add(entry);
	}

	public void render(MinecraftClient client, int mouseX, int mouseY) {
		if (!rendering) {
			y = mouseY;
			x = mouseX;
			rendering = true;
		}
		final int yStart = y + 2;
		final int xStart = x + 2;
		int y = yStart + 1;
		width = 0;
		for (ButtonWidget d : children) {
			d.x = (xStart + 1);
			d.y = (y);
			y += 11;
			width = Math.max(width, d.getWidth());
		}
		height = y;
		DrawableHelper.fill(xStart, yStart, xStart + width + 1, y, 0xDD1E1F22);
		DrawUtil.outlineRect(xStart, yStart, width + 1, y - yStart + 1, -1);
		for (ButtonWidget c : children) {
			c.setWidth(width);
			c.render(MinecraftClient.getInstance(), mouseX, mouseY);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<ContextMenuEntryWidget> stream = children.stream().filter(b -> b instanceof ContextMenuEntryWidget)
			.map(b -> (ContextMenuEntryWidget) b).filter(ButtonWidget::isHovered).collect(Collectors.toList());
		boolean clicked = false;
		for (ContextMenuEntryWidget c : stream) {
			c.onPress(mouseX, mouseY, button);
			clicked = true;
		}
		return clicked;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public static class Builder {

		private final List<ButtonWidget> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(String name, PressAction action) {
			elements.add(new ContextMenuEntryWidget(I18n.translate(name), action));
			return this;
		}

		public Builder entry(ButtonWidget widget) {
			elements.add(widget);
			return this;
		}

		public Builder spacer() {
			elements.add(new ContextMenuEntrySpacer());
			return this;
		}

		public ContextMenu build() {
			return new ContextMenu(elements);
		}

	}

	public static class ContextMenuEntrySpacer extends ButtonWidget {

		public ContextMenuEntrySpacer() {
			super(0, 0, 0, 50, 11, "-----");
		}

		@Override
		public void render(MinecraftClient client, int mouseX, int mouseY) {
			drawCenteredString(client.textRenderer, message, x + getWidth() / 2, y, 0xDDDDDD);
		}
	}

	public static class ContextMenuEntryWidget extends ButtonWidget {

		final PressAction action;

		private final MinecraftClient client = MinecraftClient.getInstance();

		public ContextMenuEntryWidget(String message, PressAction onPress) {
			super(0, 0, 0, MinecraftClient.getInstance().textRenderer.getStringWidth(message) + 4, 11, message);
			this.action = onPress;
		}

		@Override
		public void render(MinecraftClient client, int mouseX, int mouseY) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			if (isHovered()) {
				fill(x, y, x + getWidth(), y + height, 0x55ffffff);
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.active ? 16777215 : 10526880;
			drawScrollableText(MinecraftClient.getInstance().textRenderer, 2, i);
		}

		protected void drawScrollableText(TextRenderer textRenderer, int xOffset, int color) {
			int x = this.x + xOffset;
			int xEnd = this.x + this.getWidth() - xOffset;
			DrawUtil.drawScrollableText(textRenderer, message, x, this.y, xEnd, this.y + height, color);
		}

		public void onPress(double mouseX, double mouseY, int button) {
			playDownSound(client.getSoundManager());
			if (isMouseOver(client, (int) mouseX, (int) mouseY) && button == 0) {
				action.onPress(this);
			}
		}
	}

	public interface PressAction {
		void onPress(ButtonWidget buttonWidget);
	}
}
