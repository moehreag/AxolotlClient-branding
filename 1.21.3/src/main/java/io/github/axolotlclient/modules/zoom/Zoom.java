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

package io.github.axolotlclient.modules.zoom;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.keybinds.KeyBinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Based on
 * <a href="https://github.com/LogicalGeekBoy/logical_zoom/blob/master/src/main/java/com/logicalgeekboy/logical_zoom/LogicalZoom.java">Logical Zoom</a>
 */

public class Zoom extends AbstractModule {

	public static final KeyMapping key = new KeyMapping("key.zoom", InputConstants.KEY_C, "category.axolotlclient");
	public static final FloatOption zoomDivisor = new FloatOption("zoomDivisor", 4F, 1F, 16F);
	public static final FloatOption zoomSpeed = new FloatOption("zoomSpeed", 7.5F, 1F, 10F);
	public static final BooleanOption zoomScrolling = new BooleanOption("zoomScrolling", false);
	public static final BooleanOption decreaseSensitivity = new BooleanOption("decreaseSensitivity", true);
	public static final BooleanOption smoothCamera = new BooleanOption("smoothCamera", false);
	private static final Zoom Instance = new Zoom();
	public static boolean active;
	private static Double originalSensitivity;
	private static boolean originalSmoothCamera;
	private static double targetFactor = 1;
	private static double divisor;
	private static float lastAnimatedFactor = 1;
	private static float animatedFactor = 1;
	private static double lastReturnedFov;
	public final OptionCategory zoom = OptionCategory.create("zoom");

	public static Zoom getInstance() {
		return Instance;
	}

	public static double getFov(double current, float tickDelta) {
		double result =
			current * (zoomSpeed.get() == 10 ? targetFactor : Util.lerp(lastAnimatedFactor, animatedFactor, tickDelta));

		if (lastReturnedFov != 0 && lastReturnedFov != result) {
			Minecraft.getInstance().levelRenderer.needsUpdate();
		}
		lastReturnedFov = result;

		return result;
	}

	public static void update() {
		if (shouldStart()) {
			start();
		} else if (shouldStop()) {
			stop();
		}
	}

	private static boolean shouldStart() {
		return keyHeld() && !active;
	}

	private static void start() {
		active = true;
		setDivisor(zoomDivisor.get());
		setOptions();
	}

	private static boolean shouldStop() {
		return !keyHeld() && active;
	}

	private static void stop() {
		active = false;
		targetFactor = 1;
		restoreOptions();
	}

	private static boolean keyHeld() {
		return key.isDown();
	}

	private static void setDivisor(double value) {
		divisor = value;
		targetFactor = 1F / value;
	}

	public static void setOptions() {
		originalSensitivity = Minecraft.getInstance().options.sensitivity().get();

		if (smoothCamera.get()) {
			originalSmoothCamera = Minecraft.getInstance().options.smoothCamera;
			Minecraft.getInstance().options.smoothCamera = true;
		}

		updateSensitivity();
	}

	public static void restoreOptions() {
		Minecraft.getInstance().options.sensitivity().set(originalSensitivity);
		Minecraft.getInstance().options.smoothCamera = originalSmoothCamera;
	}

	private static void updateSensitivity() {
		if (decreaseSensitivity.get()) {
			Minecraft.getInstance().options.sensitivity().set(originalSensitivity / (divisor * divisor));
		}
	}

	public static boolean scroll(double amount) {
		if (active && zoomScrolling.get() && amount != 0) {
			setDivisor(Math.max(1, divisor + (amount / Math.abs(amount))));
			updateSensitivity();
			return true;
		}

		return false;
	}

	@Override
	public void init() {
		zoom.add(zoomDivisor);
		zoom.add(zoomSpeed);
		zoom.add(zoomScrolling);
		zoom.add(decreaseSensitivity);
		zoom.add(smoothCamera);
		KeyBinds.getInstance().register(key);

		AxolotlClient.CONFIG.rendering.add(zoom);

		active = false;

		KeyBinds.getInstance().registerWithSimpleAction(
			new KeyMapping("key.zoom.increase", InputConstants.UNKNOWN.getValue(), "category.axolotlclient"),
			() -> scroll(zoomSpeed.get() / 2)
													   );
		KeyBinds.getInstance().registerWithSimpleAction(
			new KeyMapping("key.zoom.decrease", InputConstants.UNKNOWN.getValue(), "category.axolotlclient"),
			() -> scroll(-zoomSpeed.get() / 2)
													   );
	}

	public void tick() {
		lastAnimatedFactor = animatedFactor;
		animatedFactor += (float) ((targetFactor - animatedFactor) * (zoomSpeed.get() / 10F));
	}
}
