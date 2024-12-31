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

package io.github.axolotlclient.modules.mcci;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MccIslandGameType {
	LOBBY("Lobby", "lobby"),
	PARKOUR_WARRIOR("Parkour Warrior", "parkour-warrior", "parkour_warrior"),
	TGTTOS("To Get To The Other Side", "tgttos", "tgttos"),
	HOLE_IN_THE_WALL("Hole In The Wall", "hole-in-the-wall", "hole_in_the_wall"),
	ROCKET_SPLEEF_RUSH("Rocket Spleef Rush", "rocket-spleef", "rocket_spleef"),
	DYNABALL("Dynaball", "dynaball", "dynaball"),
	SKY_BATTLE("Sky Battle", "sky-battle", "sky_battle"),
	BATTLE_BOX("Battle Box", "battle-box", "battle_box")
	;
	private final String name;
	private final String serverType;
	private final String gameName;

	MccIslandGameType(String name, String serverType) {
		this(name, serverType, "");
	}

	public static MccIslandGameType fromLocation(MccIslandLocationData data) {
		return Arrays.stream(values()).filter(t -> data.server().startsWith(t.getServerType())).findFirst().orElseThrow();
	}

	private static final Map<String, MccIslandGameType> byServerType = Arrays.stream(values()).collect(Collectors.toMap(MccIslandGameType::getServerType, Function.identity()));

	public static MccIslandGameType getServerType(String type) {
		return byServerType.get(type);
	}
}
