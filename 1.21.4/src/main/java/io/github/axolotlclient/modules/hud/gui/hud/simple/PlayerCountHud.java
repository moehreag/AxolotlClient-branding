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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class PlayerCountHud extends SimpleTextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("axolotlclient", "playercounthud");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public String getValue() {
		return client.level.players().size() + " " + I18n.get("players");
	}

	@Override
	public String getPlaceholder() {
		return 3.141592 + " " + I18n.get("players");
	}
}
