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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class NewsScreen extends Screen {

	private static final int SCROLL_STEP = 5;
	private final Screen parent;
	private int scrollAmount;
	private List<FormattedCharSequence> lines;

	public NewsScreen(Screen parent) {
		super(Component.translatable("api.notes.title"));

		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredString(font, title, width / 2, 20, -1);

		RenderSystem.enableBlend();

		graphics.pose().pushPose();
		graphics.pose().translate(0, scrollAmount, 0);

		graphics.enableScissor(0, 35, width, height - 65);
		int y = 35;
		for (FormattedCharSequence t : lines) {
			graphics.drawString(font, t, 25, y, -1);
			y += font.lineHeight;
		}
		graphics.disableScissor();
		graphics.pose().popPose();


		int scrollbarY = 35 + ((height - 65) - 35) / (lines.size()) * -(scrollAmount / SCROLL_STEP);
		int scrollbarHeight = (height - 65 - 35) / SCROLL_STEP;
		graphics.fill(width - 15, 35, width - 9, height - 65, -16777216);
		graphics.fill(width - 15, scrollbarY, width - 9, scrollbarY + scrollbarHeight, -8355712);
		graphics.fill(width - 15, scrollbarY, width - 10, scrollbarY + scrollbarHeight - 1, -4144960);

	}

	@Override
	protected void init() {
		lines = minecraft.font.split(FormattedText.of(GlobalDataRequest.get().notes()), width - 50);

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, buttonWidget -> minecraft.setScreen(parent))
								.bounds(width / 2 - 100, height - 45, 200, 20).build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
		scrollAmount = (int) Mth.clamp(scrollAmount + amountY * SCROLL_STEP,
									   Math.min(0, -((lines.size() + 3) * font.lineHeight - (height - 65))), 0
									  );
		return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
	}
}
