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

package io.github.axolotlclient.util.options.vanilla;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.OptionCategoryImpl;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.BooleanWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.CategoryWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class QuickToggleCategoryWidget extends CategoryWidget {
	private BooleanWidget enabledButton;

	public QuickToggleCategoryWidget(int x, int y, int width, int height, OptionCategoryImpl category) {
		super(x, y, width, height, category);
		if (AxolotlClient.CONFIG.showQuickToggles.get()) {
			category.getOptions().stream()
				.filter(o -> o instanceof BooleanOption)
				.map(o -> (BooleanOption) o)
				.filter(o -> "enabled".equals(o.getName())).findFirst()
				.ifPresent(booleanOption -> enabledButton = new BooleanWidget(x + (width - 33), y + 3, 30, height - 5, booleanOption));
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {

		if (enabledButton != null && enabledButton.isMouseOver(mouseX, mouseY)) {
			this.hovered = false;
			return true;
		}
		return super.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void renderButton(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		super.renderButton(graphics, mouseX, mouseY, delta);

		if (enabledButton != null) {
			enabledButton.y = (getY() + 2);
			enabledButton.update();
			enabledButton.render(graphics, mouseX, mouseY, delta);
		}
	}

	@Override
	protected void drawScrollableText(MatrixStack matrices, TextRenderer textRenderer, int xOffset, int color) {
		int i = this.getX() + xOffset;
		int j = this.getX() + this.getWidth() - xOffset;
		if (enabledButton != null) {
			j -= enabledButton.getWidth() + 4;
		}
		DrawUtil.drawScrollingText(matrices, textRenderer, this.getMessage(), this.getX() + this.getWidth() / 2, i, this.getY(), j, this.getY() + this.getHeight(), color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {

		if (enabledButton != null &&
			enabledButton.isHovered()) {
			playDownSound(MinecraftClient.getInstance().getSoundManager());
			enabledButton.onPress();
			return true;
		}
		return this.hovered && super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.active || !this.visible) {
			return false;
		} else if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
			return false;
		} else {
			this.playDownSound(MinecraftClient.getInstance().getSoundManager());
			mouseClicked(0, 0, 0);
			return true;
		}
	}
}
