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

import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 * <p>
 * An interface that represents an object that contains an Identifier, as well as ways to translate itself into a
 */
public interface Identifiable {

	/**
	 * The translated name of the object
	 *
	 * @return String containing the name
	 */
	default String getName() {
		return I18n.translate(getNameKey());
	}

	/**
	 * Gets the display name key
	 *
	 * @return The display name key
	 */
	default String getNameKey() {
		return getId().getPath();
	}

	/**
	 * Returns a unique identifier for this object
	 *
	 * @return The identifier
	 */
	Identifier getId();
}
