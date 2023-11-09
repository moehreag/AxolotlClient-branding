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

package io.github.axolotlclient.credits;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;

public class Credits {

	@Getter
	private static final Set<Credits> contributors = new LinkedHashSet<>(), otherPeople = new LinkedHashSet<>();

	static {
		contributor("moehreag", "Author, Programming", "https://github.com/moehreag");
		contributor("YakisikliBaran", "Turkish Translation");
		contributor("TheKodeToad", "Contributor", "Motion Blur", "Freelook", "Zoom");
		contributor("DragonEggBedrockBreaking", "Bugfixes", "Inspiration of new Features");
		contributor("gart", "gartbin dev and host", "Image sharing help", "https://gart.sh", "Backend developer");
		contributor("xlingy", "Website author");
		contributor("DarkKronicle", "Bedwars Overlay", "Author of KronHUD, the best HUD mod!");

		otherPerson("AMereBagatelle", "Author of the excellent FabricSkyBoxes Mod");
	}

	@Getter
	private final String name;
	@Getter
	private final String[] things;

	public Credits(String name, String... things) {
		this.name = name;
		this.things = things;
	}

	public static void contributor(String name, String... things) {
		Credits c = new Credits(name, things);
		contributors.add(c);
	}

	public static void otherPerson(String name, String... things) {
		Credits c = new Credits(name, things);
		otherPeople.add(c);
	}
}
