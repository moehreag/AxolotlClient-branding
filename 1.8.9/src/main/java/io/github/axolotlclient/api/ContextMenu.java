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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;

public class ContextMenu {

	private final List<ButtonWidget> children;
	private int x;
	private int y;

	private int width, height = 0;
	private boolean rendering;

	protected ContextMenu(List<ButtonWidget> items) {
		children = items;
		int width = 0;
		int height = 0;
		for (ButtonWidget d : children) {
			d.y = height;
			height += 11;
			width = Math.max(width, d.getWidth());
		}
		this.width = width;
		this.height = height;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addEntry(ButtonWidget entry) {
		children.add(entry);
	}

	public void render(Minecraft client, int mouseX, int mouseY) {
		if (!rendering) {
			y = mouseY;
			x = mouseX;
			rendering = true;
		}
		final int yStart = (int) Math.min(y + 2, Util.getWindow().getScaledHeight() - height - 2);
		final int xStart = (int) Math.min(x + 2, Util.getWindow().getScaledWidth() - width - 2);
		int y = yStart + 1;
		width = 0;
		for (ButtonWidget d : children) {
			d.x = (xStart + 1);
			d.y = (y);
			y += 11;
			width = Math.max(width, d.getWidth());
		}
		height = y;
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 200);
		GuiElement.fill(xStart, yStart, xStart + width + 1, y, 0xDD1E1F22);
		DrawUtil.outlineRect(xStart, yStart, width + 1, y - yStart + 1, -1);
		for (ButtonWidget c : children) {
			c.setWidth(width);
			c.render(client, mouseX, mouseY);
		}
		GlStateManager.popMatrix();
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<Pressable> stream = children.stream().filter(b -> b instanceof Pressable)
			.map(b -> (Pressable) b).toList();
		boolean clicked = false;
		for (Pressable c : stream) {
			clicked = c.onPress(mouseX, mouseY, button);
			if (clicked) {
				break;
			}
		}
		return clicked;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public interface PressAction {
		void onPress(ButtonWidget buttonWidget);
	}

	public static class Builder {

		private final List<ButtonWidget> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(String name, PressAction action) {
			elements.add(new ContextMenuEntryWithAction(I18n.translate(name), action));
			return this;
		}

		public Builder entry(ButtonWidget widget) {
			elements.add(widget);
			return this;
		}

		public Builder spacer() {
			elements.add(new ContextMenuEntry("-----"));
			return this;
		}

		public Builder title(String title) {
			elements.add(new ContextMenuEntry(title));
			return this;
		}

		public ContextMenu build() {
			return new ContextMenu(elements);
		}

	}

	public static class ContextMenuEntry extends ButtonWidget implements Pressable {

		public ContextMenuEntry(String content) {
			super(0, 0, 0, Minecraft.getInstance().textRenderer.getWidth(content), 11, content);
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			drawCenteredString(client.textRenderer, message, x + getWidth() / 2, y, 0xDDDDDD);
		}

		@Override
		public boolean onPress(double mouseX, double mouseY, int button) {
			return false;
		}
	}

	public static class ContextMenuEntryWithAction extends ButtonWidget implements Pressable {

		final PressAction action;

		private final Minecraft client = Minecraft.getInstance();

		public ContextMenuEntryWithAction(String message, PressAction onPress) {
			super(0, 0, 0, Minecraft.getInstance().textRenderer.getWidth(message) + 4, 11, message);
			this.action = onPress;
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			if (isHovered()) {
				fill(x, y, x + getWidth(), y + height, 0x55ffffff);
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.active ? 16777215 : 10526880;
			drawScrollableText(Minecraft.getInstance().textRenderer, 2, i);
		}

		protected void drawScrollableText(TextRenderer textRenderer, int xOffset, int color) {
			int x = this.x + xOffset;
			int xEnd = this.x + this.getWidth() - xOffset;
			DrawUtil.drawScrollableText(textRenderer, message, x, this.y, xEnd, this.y + height, color);
		}

		public boolean onPress(double mouseX, double mouseY, int button) {
			if (isMouseOver(client, (int) mouseX, (int) mouseY) && button == 0) {
				playClickSound(client.getSoundManager());
				action.onPress(this);
				return true;
			}
			return false;
		}
	}

	private interface Pressable {
		boolean onPress(double mouseX, double mouseY, int button);
	}
}
