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

package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

// https://github.com/AxolotlClient/AxolotlClient-mod/blob/4ae2678bfe9e0908be1a7a34e61e689c8005ae0a/src/main/java/io/github/axolotlclient/modules/hud/gui/hud/ReachDisplayHud.java
public class ReachHud extends SimpleTextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "reachhud");
	private final IntegerOption decimalPlaces = new IntegerOption("decimalplaces", 0, 0, 15);

	private String currentDist;
	private long lastTime = 0;

	public static double getAttackDistance(Entity attacking, Entity receiving) {
		Vec3 camera = attacking.getEyePosition();
		Vec3 rotation = attacking.getViewVector(1);

		Vec3 maxPos = receiving.position();
		AtomicDouble max = new AtomicDouble(0);

		maxPos = compareTo(camera, maxPos.add(0, 0, receiving.getBoundingBox().maxZ), max);
		maxPos = compareTo(camera, maxPos.add(0, 0, receiving.getBoundingBox().minZ), max);
		maxPos = compareTo(camera, maxPos.add(0, receiving.getBoundingBox().maxY, 0), max);
		maxPos = compareTo(camera, maxPos.add(0, receiving.getBoundingBox().minY, 0), max);
		maxPos = compareTo(camera, maxPos.add(receiving.getBoundingBox().maxX, 0, 0), max);
		maxPos = compareTo(camera, maxPos.add(receiving.getBoundingBox().minX, 0, 0), max);

		// Max reach distance that want to account for
		double d = max.get() + .5;
		Vec3 possibleHits = camera.add(rotation.x * d, rotation.y * d, rotation.z * d);
		AABB box = attacking.getBoundingBox().expandTowards(rotation.scale(d)).inflate(1.0, 1.0, 1.0);

		EntityHitResult result = ProjectileUtil.getEntityHitResult(attacking, camera, possibleHits, box,
			entity -> entity.getId() == receiving.getId(), d);
		if (result == null || result.getEntity() == null) {
			// This should not happen...
			return -1;
		}
		return camera.distanceTo(result.getLocation());
	}

	private static Vec3 compareTo(Vec3 compare, Vec3 test, AtomicDouble max) {
		double dist = compare.distanceTo(test);
		if (dist > max.get()) {
			max.set(dist);
			return test;
		}
		return compare;
	}

	@Override
	public ResourceLocation getId() {
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
			format.append("0".repeat(Math.max(0, decimalPlaces.get())));
		}
		DecimalFormat formatter = new DecimalFormat(format.toString());
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		currentDist = formatter.format(distance) + " " + I18n.get("blocks");
		lastTime = Util.getMillis();
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
			return "0 " + I18n.get("blocks");
		} else if (lastTime + 2000 < Util.getMillis()) {
			currentDist = null;
			return "0 " + I18n.get("blocks");
		}
		return currentDist;
	}

	@Override
	public String getPlaceholder() {
		return "3.45 " + I18n.get("blocks");
	}
}
