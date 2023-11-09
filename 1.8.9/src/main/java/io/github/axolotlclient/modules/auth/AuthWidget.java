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

package io.github.axolotlclient.modules.auth;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

public class AuthWidget extends ButtonWidget {

	public AuthWidget() {
		super(242, 10, 10,
			MinecraftClient.getInstance().textRenderer.getStringWidth(Auth.getInstance().getCurrent().getName()) + 28,
			20, "    " + Auth.getInstance().getCurrent().getName());
	}

	@Override
	public void render(MinecraftClient minecraftClient, int i, int j) {
		super.render(minecraftClient, i, j);
		GlStateManager.color(1, 1, 1, 1);
		MinecraftClient.getInstance().getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(Auth.getInstance().getCurrent()));
		GlStateManager.enableBlend();
		drawTexture(x + 1, y + 1, 8, 8, 8, 8, height - 2, height - 2, 64, 64);
		drawTexture(x + 1, y + 1, 40, 8, 8, 8, height - 2, height - 2, 64, 64);
		GlStateManager.disableBlend();
	}
}
