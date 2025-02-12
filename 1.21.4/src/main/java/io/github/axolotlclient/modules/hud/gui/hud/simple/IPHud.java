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

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class IPHud extends SimpleTextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "iphud");
	private final BooleanOption showIcon = new BooleanOption("iphud.show_icon", false);
	private FaviconTexture icon;

	public IPHud() {
		super(115, 13);
		ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
			if (icon != null) {
				icon.close();
				icon = null;
			}
		});
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public String getValue() {
		if (client.isLocalServer()) {
			return "Singleplayer";
		}
		if (client.getCurrentServer() == null) {
			return "none";
		}
		return client.getCurrentServer().ip;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();

		return options;
	}

	@Override
	public String getPlaceholder() {
		return "Singleplayer";
	}
}
