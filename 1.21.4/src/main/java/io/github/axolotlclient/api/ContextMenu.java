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
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ContextMenu implements ContainerEventHandler, Renderable, NarratableEntry {

	private final List<AbstractButton> children;
	private boolean dragging;
	private GuiEventListener focused;

	private int x;
	private int y;
	private final int width, height;
	private boolean rendering;

	protected ContextMenu(List<AbstractButton> items) {
		children = items;
		int width = 0;
		int height = 0;
		for (AbstractButton d : children) {
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

	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}

	public List<AbstractButton> entries() {
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
	public GuiEventListener getFocused() {
		return focused;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener child) {
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
		final int yStart = Math.min(y + 2, graphics.guiHeight() - height - 2);
		final int xStart = Math.min(x + 2, graphics.guiWidth() - width - 2);
		int y = yStart + 1;
		for (AbstractButton d : children) {
			d.setX(xStart + 1);
			d.setY(y);
			y += d.getHeight();
		}
		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 200);
		graphics.fill(xStart, yStart, xStart + width + 1, y, 0xDD1E1F22);
		graphics.renderOutline(xStart, yStart, width + 1, y - yStart + 1, -1);
		for (AbstractButton c : children) {
			c.setWidth(width);
			c.render(graphics, mouseX, mouseY, delta);
		}
		graphics.pose().popPose();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getChildAt(mouseX, mouseY).isPresent();
	}

	@Override
	public NarrationPriority narrationPriority() {
		return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.HOVERED;
	}

	@Override
	public void updateNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, Component.translatable("api.context_menu"));
		entries().forEach(b -> builder.add(NarratedElementType.USAGE, b.getMessage()));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Optional<GuiEventListener> optional = this.getChildAt(mouseX, mouseY);
		if (optional.isPresent()) {
			GuiEventListener guiEventListener = optional.get();
			if (guiEventListener.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused(guiEventListener);
				if (button == 0) {
					this.setDragging(true);
				}
				return true;
			}
		}
		return false;
	}

	public static class Builder {

		private final Minecraft client = Minecraft.getInstance();

		private final List<AbstractButton> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(Component name, Button.OnPress action) {
			elements.add(new ContextMenuEntryWithAction(name, action));
			return this;
		}

		public Builder entry(AbstractButton widget) {
			elements.add(widget);
			return this;
		}

		public Builder spacer() {
			elements.add(new ContextMenuEntry(Component.literal("-----")) {
				@Override
				protected void updateWidgetNarration(NarrationElementOutput builder) {

				}
			});
			return this;
		}

		public Builder title(Component title) {
			elements.add(new ContextMenuEntry(title));
			return this;
		}

		public ContextMenu build() {
			return new ContextMenu(elements);
		}

	}

	public static class ContextMenuEntry extends AbstractButton {

		private final Minecraft client = Minecraft.getInstance();

		public ContextMenuEntry(Component content) {
			super(0, 0, Minecraft.getInstance().font.width(content), 11, content);
		}

		@Override
		public void onPress() {

		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.drawCenteredString(client.font, getMessage(), getX() + getWidth() / 2, getY(), 0xDDDDDD);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {
			builder.add(NarratedElementType.TITLE, getMessage());
		}
	}

	public static class ContextMenuEntryWithAction extends Button {

		private final Minecraft client = Minecraft.getInstance();

		public ContextMenuEntryWithAction(Component message, OnPress onPress) {
			super(0, 0, Minecraft.getInstance().font.width(message) + 4, 11, message, onPress, DEFAULT_NARRATION);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

			if (isHoveredOrFocused()) {
				graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x55ffffff);
			}

			int i = this.active ? 16777215 : 10526880;
			this.renderString(graphics, client.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}
}
