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

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import org.jetbrains.annotations.Nullable;

public class ContextMenuContainer extends DrawableHelper {

	@Getter
	@Setter
	@Nullable
	private ContextMenu menu;

	public ContextMenuContainer() {

	}

	public void removeMenu() {
		menu = null;
	}

	public boolean hasMenu() {
		return menu != null;
	}

	public void render(MinecraftClient client, int mouseX, int mouseY) {
		if (menu != null) {
			menu.render(client, mouseX, mouseY);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (menu != null) {
			return menu.mouseClicked(mouseX, mouseY, button);
		}
		return false;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		if (menu != null) {
			return menu.isMouseOver(mouseX, mouseY);
		}
		return false;
	}
}
