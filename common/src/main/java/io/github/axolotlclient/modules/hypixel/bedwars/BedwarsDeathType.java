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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author DarkKronicle
 */

@AllArgsConstructor
public enum BedwarsDeathType {
	COMBAT(createOption("combat", "rekt"), BedwarsMessages.COMBAT_KILL),
	VOID(createOption("void", "yeeted into void"), BedwarsMessages.VOID_KILL),
	PROJECTILE(createOption("projectile", "shot"), BedwarsMessages.PROJECTILE_KILL),
	FALL(createOption("fall", "fall"), BedwarsMessages.FALL_KILL),
	GOLEM(createOption("golem", "golem moment"), BedwarsMessages.GOLEM_KILL),
	SELF_VOID(createOption("self_void", "voided"), new Pattern[]{BedwarsMessages.SELF_VOID}),
	SELF_UNKNOWN(createOption("self_unknown", "died"), new Pattern[]{BedwarsMessages.SELF_UNKNOWN});

	@Getter
	private static final OptionCategory options = OptionCategory.create("bedwars.deathType");

	static {
		Arrays.stream(values()).map(BedwarsDeathType::getInner).forEach(options::add);
	}

	@Getter
	private final StringOption inner;
	@Getter
	private final Pattern[] patterns;

	public static boolean getDeath(String rawMessage, BedwarsDeathMatch ifPresent) {
		for (BedwarsDeathType type : values()) {
			if (BedwarsMessages.matched(type.getPatterns(), rawMessage, m -> ifPresent.onMatch(type, m))) {
				return true;
			}
		}
		return false;
	}

	private static StringOption createOption(String type, String def) {
		return new StringOption("bedwars.deathType." + type, def);
	}

	public interface BedwarsDeathMatch {

		void onMatch(BedwarsDeathType type, Matcher matcher);

	}
}
