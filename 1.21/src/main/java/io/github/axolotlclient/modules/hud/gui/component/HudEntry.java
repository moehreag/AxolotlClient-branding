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

package io.github.axolotlclient.modules.hud.gui.component;

import net.minecraft.client.gui.GuiGraphics;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public interface HudEntry extends Identifiable, Configurable, Positionable {

	boolean isEnabled();

	void setEnabled(boolean value);

	default boolean tickable() {
		return false;
	}

	default boolean overridesF3() {
		return false;
	}

	default void tick() {
	}

	default void init() {
	}

	default double getDefaultX() {
		return 0;
	}

	default double getDefaultY() {
		return 0;
	}

	void render(GuiGraphics graphics, float delta);

	void renderPlaceholder(GuiGraphics graphics, float delta);

	void setHovered(boolean hovered);
}
