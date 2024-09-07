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

package io.github.axolotlclient.modules.hud.gui.layout;

import lombok.AllArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@AllArgsConstructor
public enum Justification {

	LEFT, CENTER, RIGHT;

	public int getXOffset(Text text, int width) {
		if (this == LEFT) {
			return 0;
		}
		return getXOffset(MinecraftClient.getInstance().textRenderer.getWidth(text), width);
	}

	public int getXOffset(int textWidth, int width) {
		if (this == LEFT) {
			return 0;
		}
		if (this == RIGHT) {
			return width - textWidth;
		}
		return (width - textWidth) / 2;
	}

	public int getXOffset(String text, int width) {
		if (this == LEFT) {
			return 0;
		}
		return getXOffset(MinecraftClient.getInstance().textRenderer.getWidth(text), width);
	}
}
