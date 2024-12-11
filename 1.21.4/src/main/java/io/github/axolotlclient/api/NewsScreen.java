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

import io.github.axolotlclient.api.requests.GlobalDataRequest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class NewsScreen extends Screen {

	private final Screen parent;

	public NewsScreen(Screen parent) {
		super(Component.translatable("api.notes.title"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredString(font, title, width / 2, 20, -1);
	}

	@Override
	protected void init() {
		addRenderableWidget(new NewsWidget(25, 35, width - 50, height - 100, Component.literal(GlobalDataRequest.get().notes().replaceAll("([^\n])\n([^\n])", "$1 $2"))));
		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, buttonWidget -> minecraft.setScreen(parent)).bounds(width / 2 - 100, height - 45, 200, 20).build());
	}

	private class NewsWidget extends AbstractTextAreaWidget {

		private final List<FormattedCharSequence> lines;
		private final int contentHeight;

		public NewsWidget(int x, int y, int width, int height, Component component) {
			super(x, y, width, height, component);
			lines = font.split(getMessage(), getWidth() - 4);
			contentHeight = lines.size() * font.lineHeight;
		}

		@Override
		protected int getInnerHeight() {
			return contentHeight;
		}

		@Override
		protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int y = getY() + 2;
			for (FormattedCharSequence chsq : lines) {
				graphics.drawString(font, chsq, getX() + 2, y, -1);
				y += font.lineHeight;
			}
		}

		@Override
		protected double scrollRate() {
			return font.lineHeight;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}
	}
}
