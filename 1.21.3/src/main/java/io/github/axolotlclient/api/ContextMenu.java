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
	private boolean rendering;

	protected ContextMenu(List<AbstractButton> items) {
		children = items;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void addEntry(AbstractButton entry) {
		children.add(entry);
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
		final int yStart = y + 2;
		final int xStart = x + 2;
		int y = yStart + 1;
		int width = 0;
		for (AbstractButton d : children) {
			d.setX(xStart + 1);
			d.setY(y);
			y += d.getHeight();
			width = Math.max(width, d.getWidth());
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
	public NarrationPriority narrationPriority() {
		return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.HOVERED;
	}

	@Override
	public void updateNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, Component.translatable("api.context_menu"));
		entries().forEach(b -> builder.add(NarratedElementType.USAGE, b.getMessage()));
	}

	public static class Builder {

		private final Minecraft client = Minecraft.getInstance();

		private final List<AbstractButton> elements = new ArrayList<>();

		public Builder() {

		}

		public Builder entry(Component name, Button.OnPress action) {
			elements.add(new ContextMenuEntryWidget(name, action));
			return this;
		}

		public Builder entry(AbstractButton widget) {
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

	public static class ContextMenuEntrySpacer extends AbstractButton {

		private final Minecraft client = Minecraft.getInstance();

		public ContextMenuEntrySpacer() {
			super(0, 0, 50, 11, Component.literal("-----"));
		}

		@Override
		public void onPress() {

		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			graphics.drawCenteredString(client.font, getMessage(), getX() + getWidth() / 2, getY(), 0xDDDDDD);
		}

		@Override
		protected boolean clicked(double mouseX, double mouseY) {
			return false;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}
	}

	public static class ContextMenuEntryWidget extends Button {

		private final Minecraft client = Minecraft.getInstance();

		protected ContextMenuEntryWidget(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narrationFactory) {
			super(x, y, width, height, message, onPress, narrationFactory);
		}

		public ContextMenuEntryWidget(Component message, OnPress onPress) {
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
