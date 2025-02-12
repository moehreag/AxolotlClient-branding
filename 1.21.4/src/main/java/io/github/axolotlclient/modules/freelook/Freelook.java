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

package io.github.axolotlclient.modules.freelook;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.FeatureDisabler;
import io.github.axolotlclient.util.keybinds.KeyBinds;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class Freelook extends AbstractModule {

	private static final Freelook INSTANCE = new Freelook();
	private static final KeyMapping KEY = KeyBinds.getInstance().register(new KeyMapping("key.freelook", InputConstants.KEY_V, "category.axolotlclient"));
	private static final KeyMapping KEY_ALT = KeyBinds.getInstance().register(new KeyMapping("key.freelook.alt", InputConstants.UNKNOWN.getValue(), "category.axolotlclient"));
	public final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", false);
	private final Minecraft client = Minecraft.getInstance();
	private final OptionCategory category = OptionCategory.create("freelook");
	private final StringArrayOption mode =
		new StringArrayOption("mode", new String[]{"snap_perspective", "freelook"}, "freelook",
			value -> FeatureDisabler.update()
		);
	private final BooleanOption invert = new BooleanOption("invert", false);
	private final EnumOption<CameraType> perspective =
		new EnumOption<CameraType>("perspective", CameraType.class, CameraType.THIRD_PERSON_BACK);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	private final EnumOption<CameraType> perspectiveAlt = new EnumOption<>("perspective.alt", CameraType.class,
		CameraType.THIRD_PERSON_FRONT);
	private final BooleanOption toggleAlt = new BooleanOption("toggle.alt", false);
	private final WrappedValue active = new WrappedValue(), activeAlt = new WrappedValue();
	private float yaw, pitch;
	private final Deque<CameraType> previousPerspectives = new ArrayDeque<>();

	public static Freelook getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		category.add(enabled, mode, perspective, invert, toggle);
		category.add(perspectiveAlt, toggleAlt);
		AxolotlClient.CONFIG.addCategory(category);
	}

	@Override
	public void tick() {
		if (!enabled.get() || client.screen != null) return;
		tickSet(toggle, KEY, perspective, active);
		tickSet(toggleAlt, KEY_ALT, perspectiveAlt, activeAlt);
	}

	private void tickSet(BooleanOption toggle, KeyMapping key, EnumOption<CameraType> perspective, WrappedValue active) {
		if (toggle.get()) {
			if (key.consumeClick()) {
				if (active.val) {
					stop(active);
				} else {
					start(perspective.get(), active);
				}
			}
		} else {
			if (key.isDown()) {
				if (!active.val) {
					start(perspective.get(), active);
				}
			} else if (active.val) {
				stop(active);
			}
		}
	}

	private void stop(WrappedValue active) {
		active.val = false;
		client.levelRenderer.needsUpdate();
		setPerspective(previousPerspectives.pop());
	}

	private void start(CameraType perspective, WrappedValue active) {
		previousPerspectives.push(client.options.getCameraType());
		active.val = true;
		setPerspective(perspective);

		Entity camera = client.getCameraEntity();

		if (camera == null) camera = client.player;
		if (camera == null) return;

		yaw = camera.getYRot();
		pitch = camera.getXRot();
	}

	private void setPerspective(CameraType perspective) {
		Minecraft.getInstance().options.setCameraType(perspective);
	}

	public boolean consumeRotation(double dx, double dy) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook")) return false;

		if (!invert.get()) dy = -dy;

		if (Minecraft.getInstance().options.getCameraType().isMirrored() ||
			Minecraft.getInstance().options.getCameraType().isFirstPerson()) dy *= -1;

		yaw += (float) (dx * 0.15F);
		pitch += (float) (dy * 0.15F);

		if (pitch > 90) {
			pitch = 90;
		} else if (pitch < -90) {
			pitch = -90;
		}

		client.levelRenderer.needsUpdate();
		return true;
	}

	public float yaw(float defaultValue) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook")) return defaultValue;

		return yaw;
	}

	public float pitch(float defaultValue) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook")) return defaultValue;

		return pitch;
	}

	public boolean needsDisabling() {
		return mode.get().equals("freelook");
	}

	public boolean isActive(){
		return active.val || activeAlt.val;
	}

	private static class WrappedValue {
		boolean val;
	}
}
