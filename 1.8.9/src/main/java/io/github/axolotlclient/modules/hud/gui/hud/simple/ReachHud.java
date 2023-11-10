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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec3d;

// https://github.com/AxolotlClient/AxolotlClient-mod/blob/4ae2678bfe9e0908be1a7a34e61e689c8005ae0a/src/main/java/io/github/axolotlclient/modules/hud/gui/hud/ReachDisplayHud.java
// https://github.com/DarkKronicle/KronHUD/blob/703b87a7c938ba25da9105d731b70d3bc66efd1e/src/main/java/io/github/darkkronicle/kronhud/gui/hud/simple/ReachHud.java
public class ReachHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "reachhud");
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);

	private String currentDist;
	private long lastTime = 0;

	public static double getAttackDistance(Entity attacking, Entity receiving) {

		Vec3d camera = attacking.getRotationVec(1);
		return camera.distanceTo(receiving.getRotationVec(1));
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public void updateDistance(Entity attacking, Entity receiving) {
		double distance = getAttackDistance(attacking, receiving);
		if (distance < 0) {
			distance *= -1;
			// This should not happen...
			currentDist = "NaN";
			//return;
		}

		StringBuilder format = new StringBuilder("0");
		if (decimalPlaces.get() > 0) {
			format.append(".");
			for (int i = 0; i < decimalPlaces.get(); i++) {
				format.append("0");
			}
		}
		DecimalFormat formatter = new DecimalFormat(format.toString());
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		currentDist = formatter.format(distance) + " " + I18n.translate("blocks");
		lastTime = Minecraft.getTime();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(decimalPlaces);
		return options;
	}

	@Override
	public String getValue() {
		if (currentDist == null) {
			return "0 " + I18n.translate("blocks");
		} else if (lastTime + 2000 < Minecraft.getTime()) {
			currentDist = null;
			return "0 " + I18n.translate("blocks");
		}
		return currentDist;
	}

	@Override
	public String getPlaceholder() {
		return "3.45 " + I18n.translate("blocks");
	}
}
