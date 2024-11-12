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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.Locale;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author DarkKronicle
 */

@AllArgsConstructor
public enum BedwarsTeam {
	RED('c', 'R'),
	BLUE('9', 'B'),
	GREEN('a', 'G'),
	YELLOW('e', 'Y'),
	AQUA('b', 'A'),
	WHITE('f', 'W'),
	PINK('d', 'P'),
	GRAY('8', 'S'),
	;

	@Getter
	private final char code;

	@Getter
	private final char prefix;

	public static Optional<BedwarsTeam> fromPrefix(char prefix) {
		for (BedwarsTeam t : values()) {
			if (t.getPrefix() == prefix) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}

	public static Optional<BedwarsTeam> fromName(String name) {
		for (BedwarsTeam t : values()) {
			if (name.equalsIgnoreCase(t.name())) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}

	public String getColorSection() {
		return "§" + code;
	}

	public String getName() {
		return name().substring(0, 1).toUpperCase(Locale.ROOT) + name().substring(1).toLowerCase(Locale.ROOT);
	}

}
