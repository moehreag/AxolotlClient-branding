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

import java.util.List;
import java.util.UUID;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.resources.ResourceLocation;

public class CustomHudEntry extends SimpleTextHudEntry {

	private final ResourceLocation id;
	public final StringOption value = new StringOption("custom_hud.value", "Text");
	private final GenericOption removeEntry;

	public CustomHudEntry() {
		this.id = ResourceLocation.fromNamespaceAndPath("axolotlclient", "custom_hud/"+ UUID.randomUUID());
		removeEntry = new GenericOption("custom_hud.remove", "custom_hud.remove.label", () -> {
			HudManager.getInstance().removeEntry(this.id);
			HudManager.getInstance().saveCustomEntries();
		});
	}

	@Override
	public String getNameKey() {
		return "custom_hud";
	}

	@Override
	public String getPlaceholder() {
		return value.get();
	}

	@Override
	public String getValue() {
		return value.get();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(value);
		options.add(removeEntry);
		return options;
	}
}
