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

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class NewsScreen extends Screen {

	private final Screen parent;

	private int scrollAmount;
	private static final int SCROLL_STEP = 5;

	private List<OrderedText> lines;

	public NewsScreen(Screen parent) {
		super(Text.translatable("api.notes.title"));

		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);

		client.textRenderer.drawWithShadow(matrices, title, width/2F, 20, -1);

		RenderSystem.enableBlend();

		matrices.push();
		matrices.translate(0, scrollAmount, 0);

		enableScissor(0, 35, width, height-65);
		int y = 35;
		for (OrderedText t : lines) {
			client.textRenderer.drawWithShadow(matrices, t, 25, y, -1);
			y+=client.textRenderer.fontHeight;
		}
		disableScissor();
		matrices.pop();


		int scrollbarY = 35 + ((height - 65) - 35)/(lines.size()) * -(scrollAmount/SCROLL_STEP);
		int scrollbarHeight = (height-65 - 35) / SCROLL_STEP;
		fill(matrices, width-15, 35, width-9, height-65, -16777216);
		fill(matrices, width-15, scrollbarY, width-9, scrollbarY + scrollbarHeight, -8355712);
		fill(matrices, width-15, scrollbarY, width-10, scrollbarY + scrollbarHeight-1, -4144960);

		super.render(matrices, mouseX, mouseY, delta);


	}

	@Override
	protected void init() {
		lines = client.textRenderer.wrapLines(StringVisitable.plain(GlobalDataRequest.get().getNotes()), width-50);

		addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, buttonWidget -> client.setScreen(parent))
			.positionAndSize(width/2-100, height-45, 200, 20)
			.build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollAmount = (int) MathHelper.clamp(scrollAmount + amount*SCROLL_STEP,
			Math.min(0, -((lines.size()+3)*client.textRenderer.fontHeight-(height-65))),
			0);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
}
