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

import com.google.common.util.concurrent.AtomicDouble;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

// https://github.com/AxolotlClient/AxolotlClient-mod/blob/4ae2678bfe9e0908be1a7a34e61e689c8005ae0a/src/main/java/io/github/axolotlclient/modules/hud/gui/hud/ReachDisplayHud.java
public class ReachHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "reachhud");
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);

	private String currentDist;
	private long lastTime = 0;

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
		lastTime = Util.getMeasuringTimeMs();
	}

	public static double getAttackDistance(Entity attacking, Entity receiving) {
		Vec3d camera = attacking.getCameraPosVec(1);
		Vec3d rotation = attacking.getRotationVec(1);

		Vec3d maxPos = receiving.getPos();
		AtomicDouble max = new AtomicDouble(0);

		maxPos = compareTo(camera, maxPos.add(0, 0, receiving.getBoundingBox().maxZ), max);
		maxPos = compareTo(camera, maxPos.add(0, 0, receiving.getBoundingBox().minZ), max);
		maxPos = compareTo(camera, maxPos.add(0, receiving.getBoundingBox().maxY, 0), max);
		maxPos = compareTo(camera, maxPos.add(0, receiving.getBoundingBox().minY, 0), max);
		maxPos = compareTo(camera, maxPos.add(receiving.getBoundingBox().maxX, 0, 0), max);
		maxPos = compareTo(camera, maxPos.add(receiving.getBoundingBox().minX, 0, 0), max);

		// Max reach distance that want to account for
		double d = max.get() + .5;
		Vec3d possibleHits = camera.add(rotation.x * d, rotation.y * d, rotation.z * d);
		Box box = attacking.getBoundingBox().stretch(rotation.multiply(d)).expand(1.0, 1.0, 1.0);

		EntityHitResult result = ProjectileUtil.raycast(attacking, camera, possibleHits, box,
			entity -> entity.getEntityId() == receiving.getEntityId(), d);
		if (result == null || result.getEntity() == null) {
			// This should not happen...
			return -1;
		}
		return camera.distanceTo(result.getPos());
	}

	private static Vec3d compareTo(Vec3d compare, Vec3d test, AtomicDouble max) {
		double dist = compare.distanceTo(test);
		if (dist > max.get()) {
			max.set(dist);
			return test;
		}
		return compare;
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
		} else if (lastTime + 2000 < Util.getMeasuringTimeMs()) {
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
