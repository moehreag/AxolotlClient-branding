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

package io.github.axolotlclient.modules.auth;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.API;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class AuthWidget extends ButtonWidget {

	public AuthWidget(int x, int y) {
		super(x, y,
			MinecraftClient.getInstance().textRenderer.getWidth(Auth.getInstance().getCurrent().getName()) + 28,
			20, Text.of("    " + Auth.getInstance().getCurrent().getName()), buttonWidget -> MinecraftClient.getInstance().openScreen(new AccountsScreen(MinecraftClient.getInstance().currentScreen)));
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderButton(matrices, mouseX, mouseY, delta);
		MinecraftClient.getInstance().getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(Auth.getInstance().getCurrent()));
		RenderSystem.enableBlend();
		drawTexture(matrices, x + 1, y + 1, getHeight() - 2, getHeight() - 2, 8, 8, 8, 8, 64, 64);
		drawTexture(matrices, x + 1, y + 1, getHeight() - 2, getHeight() - 2, 40, 8, 8, 8, 64, 64);
		RenderSystem.disableBlend();
		if (API.getInstance().getApiOptions().enabled.get()) {
			matrices.push();
			matrices.translate(x + getHeight() - 1, y + getHeight() - 1, 0);
			matrices.scale(0.25f, 0.25f, 1);
			matrices.translate(-8, -8, 0);
			int color = API.getInstance().getIndicatorColor();
			fill(matrices, 0, 4, 16, 12, color);
			fill(matrices, 4, 0, 12, 16, color);
			fill(matrices, 2, 2, 14, 14, color);
			matrices.pop();
		}
	}
}
