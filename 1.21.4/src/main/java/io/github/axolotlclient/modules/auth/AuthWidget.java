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

import io.github.axolotlclient.api.API;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AuthWidget extends Button {

	public AuthWidget(int x, int y) {
		super(x, y, Minecraft.getInstance().font.width(Auth.getInstance().getCurrent().getName()) + 28, 20,
			Component.literal("    " + Auth.getInstance().getCurrent().getName()),
			buttonWidget -> Minecraft.getInstance().setScreen(new AccountsScreen(Minecraft.getInstance().screen)),
			DEFAULT_NARRATION
		);
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(graphics, mouseX, mouseY, delta);
		ResourceLocation texture = Auth.getInstance().getSkinTexture(Auth.getInstance().getCurrent());
		PlayerFaceRenderer.draw(graphics, texture, getX() + 1, getY() + 1, getHeight() - 2, true, false, -1);
		if (API.getInstance().getApiOptions().enabled.get()) {
			graphics.pose().pushPose();
			graphics.pose().translate(getX() + getHeight() - 1, getY() + getHeight() - 1, 0);
			graphics.pose().scale(0.25f, 0.25f, 1);
			graphics.pose().translate(-8, -8, 0);
			int color = API.getInstance().getIndicatorColor();
			graphics.fill(0, 4, 16, 12, color);
			graphics.fill(4, 0, 12, 16, color);
			graphics.fill(2, 2, 14, 14, color);
			graphics.pose().popPose();
		}
	}
}
