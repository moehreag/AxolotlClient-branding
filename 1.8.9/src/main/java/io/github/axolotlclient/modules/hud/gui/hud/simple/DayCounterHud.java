/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;

public class DayCounterHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("axolotlclient", "daycounterhud");

	@Override
	public String getPlaceholder() {
		return I18n.translate("daycounterhud.days", 35);
	}

	@Override
	public String getValue() {
		if (client.world == null) {
			return getPlaceholder();
		}
		return I18n.translate("daycounterhud.days", client.world.getTimeOfDay()/24000);
	}

	@Override
	public Identifier getId() {
		return ID;
	}
}
