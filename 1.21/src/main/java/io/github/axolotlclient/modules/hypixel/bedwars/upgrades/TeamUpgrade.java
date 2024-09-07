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

package io.github.axolotlclient.modules.hypixel.bedwars.upgrades;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMessages;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMode;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;

/**
 * @author DarkKronicle
 */


public abstract class TeamUpgrade {
	@Getter
	protected final String name;
	protected final Pattern[] regex;

	public TeamUpgrade(String name, Pattern pattern) {
		this(name, new Pattern[]{pattern});
	}

	public TeamUpgrade(String name, Pattern[] pattern) {
		this.name = name;
		this.regex = pattern;
	}

	public boolean match(String unformatedMessage) {
		return BedwarsMessages.matched(regex, unformatedMessage, matcher -> onMatch(this, matcher));
	}

	public abstract void draw(GuiGraphics graphics, int x, int y, int width, int height);

	public boolean isMultiUpgrade() {
		// Basically only trap
		return false;
	}

	protected abstract void onMatch(TeamUpgrade upgrade, Matcher matcher);

	public abstract int getPrice(BedwarsMode mode);


	public abstract boolean isPurchased();
}

