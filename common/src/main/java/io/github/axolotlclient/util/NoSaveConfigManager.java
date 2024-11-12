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

package io.github.axolotlclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;

public class NoSaveConfigManager implements ConfigManager {

	private final OptionCategory root;
	private final List<String> suppressed = new ArrayList<>();

	public NoSaveConfigManager(OptionCategory root) {
		this.root = root;
	}

	@Override
	public void save() {

	}

	@Override
	public void load() {

	}

	@Override
	public OptionCategory getRoot() {
		return root;
	}

	@Override
	public Collection<String> getSuppressedNames() {
		return suppressed;
	}

	@Override
	public void suppressName(String s) {
		suppressed.add(s);
	}
}
