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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class SpeedHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "speedhud");
	private final static NumberFormat FORMATTER = new DecimalFormat("#0.00");
	private final BooleanOption horizontal = new BooleanOption("horizontal", true);

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(horizontal);
		return options;
	}

	@Override
	public String getValue() {
		if (client.player == null) {
			return getPlaceholder();
		}
		Entity entity = client.player.getVehicle() == null ? client.player : client.player.getVehicle();
		Vec3d vec = entity.getVelocity();
		if (entity.isOnGround() && vec.y < 0) {
			vec = new Vec3d(vec.x, 0, vec.z);
		}
		double speed;
		if (horizontal.get()) {
			speed = Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ());
		} else {
			speed = vec.length();
		}
		return FORMATTER.format(speed * 20) + " BPS";
	}

	@Override
	public String getPlaceholder() {
		return "4.35 BPS";
	}
}
