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

package io.github.axolotlclient.util.keybinds;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import io.github.axolotlclient.mixin.KeyBindAccessor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBind;

public class KeyBinds {
	@Getter
	private final static KeyBinds instance = new KeyBinds();

	private final List<KeyBind> binds = new ArrayList<>();

	public KeyBind register(KeyBind bind) {
		binds.add(bind);

		if (!KeyBindAccessor.getOrderByCategories().containsKey(bind.getCategory())) {
			int index = KeyBindAccessor.getOrderByCategories().values().stream().max(Integer::compareTo).get() + 1;
			KeyBindAccessor.getOrderByCategories().put(bind.getCategory(), index);
		}

		return bind;
	}

	public KeyBind registerWithSimpleAction(KeyBind bind, Runnable action) {
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (bind.wasPressed()) {
				action.run();
			}
		});
		return register(bind);
	}

	public KeyBind[] process(KeyBind[] keys) {
		List<KeyBind> keyBinds = Lists.newArrayList(keys);
		keyBinds.addAll(binds);
		return keyBinds.toArray(KeyBind[]::new);
	}
}
