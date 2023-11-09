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
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ElementPath;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationEvent;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

public class ContextMenuContainer implements Drawable, Selectable, Element {

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

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (menu != null) {
			menu.render(matrices, mouseX, mouseY, delta);
		}
	}

	@Override
	public void setFocused(boolean focused) {
		if (menu != null) {
			menu.setFocused(focused);
		}
	}

	@Override
	public boolean isFocused() {
		if (menu != null) {
			return menu.isFocused();
		}
		return false;
	}

	@Override
	public SelectionType getType() {
		if (menu != null) {
			return menu.getType();
		}
		return SelectionType.NONE;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		if (menu != null) {
			menu.appendNarrations(builder);
		}
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if (menu != null) {
			menu.mouseMoved(mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (menu != null) {
			return menu.mouseClicked(mouseX, mouseY, button);
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (menu != null) {
			menu.mouseReleased(mouseX, mouseY, button);
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (menu != null) {
			menu.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (menu != null) {
			menu.mouseScrolled(mouseX, mouseY, amount);
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (menu != null) {
			menu.keyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (menu != null) {
			menu.keyReleased(keyCode, scanCode, modifiers);
		}
		return false;
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (menu != null) {
			menu.charTyped(chr, modifiers);
		}
		return false;
	}

	@Nullable
	@Override
	public ElementPath nextFocusPath(GuiNavigationEvent event) {
		if (menu != null) {
			menu.nextFocusPath(event);
		}
		return Element.super.nextFocusPath(event);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (menu != null) {
			menu.isMouseOver(mouseX, mouseY);
		}
		return false;
	}

	@Nullable
	@Override
	public ElementPath getCurrentFocusPath() {
		if (menu != null) {
			menu.getCurrentFocusPath();
		}
		return Element.super.getCurrentFocusPath();
	}

	@Override
	public ScreenArea getArea() {
		if (menu != null) {
			menu.getArea();
		}
		return Element.super.getArea();
	}
}
