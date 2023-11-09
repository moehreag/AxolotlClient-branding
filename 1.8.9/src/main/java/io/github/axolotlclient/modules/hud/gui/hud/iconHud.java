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

package io.github.axolotlclient.modules.hud.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public class iconHud extends BoxHudEntry {

	public final Identifier ID = new Identifier("axolotlclient", "iconhud");

	public iconHud() {
		super(15, 15, false);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public void renderComponent(float delta) {
		GlStateManager.color(1, 1, 1, 1);
		MinecraftClient.getInstance().getTextureManager().bindTexture(AxolotlClient.badgeIcon);
		GlStateManager.enableBlend();
		drawTexture(getX(), getY(), 0, 0, width, height, width, height);
		GlStateManager.disableBlend();
	}

	@Override
	public void renderPlaceholder(float delta) {
		GlStateManager.pushMatrix();
		scale();
		GlStateManager.color(1, 1, 1, 1);
		renderComponent(delta);
		GlStateManager.popMatrix();
		hovered = false;
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
	}


	@Override
	public boolean movable() {
		return true;
	}
}
