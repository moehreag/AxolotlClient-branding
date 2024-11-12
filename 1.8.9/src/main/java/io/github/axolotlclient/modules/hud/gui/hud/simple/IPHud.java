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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class IPHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "iphud");

	public IPHud() {
		super(115, 13);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public String getValue() {
		Minecraft client = Minecraft.getInstance();
		if (client.isInSingleplayer()) {
			return "Singleplayer";
		}
		if (Util.getCurrentServerAddress() == null) {
			return "none";
		}
		return Util.getCurrentServerAddress();
	}

	@Override
	public String getPlaceholder() {
		return "Singleplayer";
	}
}
