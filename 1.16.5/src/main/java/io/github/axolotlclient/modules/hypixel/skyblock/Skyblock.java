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

package io.github.axolotlclient.modules.hypixel.skyblock;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Skyblock implements AbstractHypixelMod {

	@Getter
	private final static Skyblock Instance = new Skyblock();
	public final BooleanOption rotationLocked = new BooleanOption("rotationLocked", false);
	private final OptionCategory category = OptionCategory.create("skyblock");

	@Override
	public void init() {
		category.add(rotationLocked);
		KeyBinding binding = new KeyBinding("lockRotation", GLFW.GLFW_KEY_UNKNOWN, "category.axolotlclient");
		KeyBindingHelper.registerKeyBinding(binding);
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (binding.wasPressed()){
				rotationLocked.toggle();
			}
		});
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}
}
