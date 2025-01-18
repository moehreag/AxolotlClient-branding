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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.OptionCategoryImpl;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.BooleanWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.CategoryWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.util.ButtonWidgetTextures;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class QuickToggleCategoryWidget extends CategoryWidget {
	private BooleanWidget enabledButton;

	public QuickToggleCategoryWidget(int x, int y, int width, int height, OptionCategoryImpl category) {
		super(x, y, width, height, category);
		category.getOptions().stream()
			.filter(o -> o instanceof BooleanOption)
			.map(o -> (BooleanOption) o)
			.filter(o -> "enabled".equals(o.getName())).findFirst()
			.ifPresent(booleanOption -> {
				enabledButton = new BooleanWidget(x + (width - 33), y + 3, 30, height - 5, booleanOption) {
					@Override
					public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
						Identifier tex = ButtonWidgetTextures.get(!this.active ? 0 : (this.isHovered() ? 2 : 1));
						RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
						io.github.axolotlclient.modules.hud.util.DrawUtil.blitSprite(tex, this.getX(), this.getY(), this.getWidth(), this.getHeight(), new io.github.axolotlclient.modules.hud.util.DrawUtil.NineSlice(200, 20, 3));

						int textColor = this.active ? 16777215 : 10526880;
						this.drawScrollableText(matrices, MinecraftClient.getInstance().textRenderer, textColor | MathHelper.ceil(this.alpha * 255.0F) << 24);
					}
				};
				enabledButton.active = !(booleanOption instanceof ForceableBooleanOption o && o.isForceOff());
			});
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
		int center = this.getX() + this.getWidth()/2;
		if (enabledButton != null) {
			j -= enabledButton.getWidth() + 4;
			center -= enabledButton.getWidth() / 2 + 2;
		}
		DrawUtil.drawScrollingText(matrices, textRenderer, this.getMessage(), center, i, this.getY(), j, this.getY() + this.getHeight(), color);
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
