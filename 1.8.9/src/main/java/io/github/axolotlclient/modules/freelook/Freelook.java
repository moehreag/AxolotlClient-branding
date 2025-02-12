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

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.FeatureDisabler;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import org.lwjgl.input.Keyboard;

public class Freelook extends AbstractModule {

	private static final Freelook INSTANCE = new Freelook();
	private static final KeyBinding KEY = new KeyBinding("key.freelook", Keyboard.KEY_V,
		"category.axolotlclient");
	private static final KeyBinding KEY_ALT = new KeyBinding("key.freelook.alt", Keyboard.KEY_NONE,
		"category.axolotlclient");
	public final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", false);
	private final Minecraft client = Minecraft.getInstance();
	private final OptionCategory category = OptionCategory.create("freelook");
	private final StringArrayOption mode = new StringArrayOption("mode",
		new String[]{"snap_perspective", "freelook"},
		"freelook", value -> FeatureDisabler.update());
	private final BooleanOption invert = new BooleanOption("invert", false);
	private final EnumOption<Perspective> perspective = new EnumOption<>("perspective", Perspective.class,
		Perspective.THIRD_PERSON_BACK);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	private final EnumOption<Perspective> perspectiveAlt = new EnumOption<>("perspective.alt", Perspective.class,
		Perspective.THIRD_PERSON_FRONT);
	private final BooleanOption toggleAlt = new BooleanOption("toggle.alt", false);
	private float yaw, pitch;
	private final WrappedValue active = new WrappedValue(), activeAlt = new WrappedValue();
	private final Deque<Integer> previousPerspectives = new ArrayDeque<>();

	public static Freelook getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		KeyBindingEvents.REGISTER_KEYBINDS.register(r -> {
			r.register(KEY);
			r.register(KEY_ALT);
		});
		category.add(enabled, mode, invert, perspective, toggle);
		category.add(perspectiveAlt, toggleAlt);
		AxolotlClient.CONFIG.addCategory(category);
	}

	@Override
	public void tick() {
		if (!enabled.get() || client.screen != null) return;
		tickSet(toggle, KEY, perspective, active);
		tickSet(toggleAlt, KEY_ALT, perspectiveAlt, activeAlt);
	}

	private void tickSet(BooleanOption toggle, KeyBinding key, EnumOption<Perspective> perspective, WrappedValue active) {
		if (toggle.get()) {
			if (key.consumeClick()) {
				if (active.val) {
					stop(active);
				} else {
					start(perspective.get(), active);
				}
			}
		} else {
			if (key.isPressed()) {
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
		client.worldRenderer.onViewChanged();
		client.gameRenderer.updateShader(client.getCamera());
		client.options.perspective = previousPerspectives.pop();
	}

	private void start(Perspective perspective, WrappedValue active) {
		previousPerspectives.push(client.options.perspective);
		active.val = true;
		client.options.perspective = perspective.ordinal();

		Entity camera = client.getCamera();

		if (camera == null)
			camera = client.player;
		if (camera == null)
			return;

		yaw = camera.yaw;
		pitch = camera.pitch;
	}

	public boolean consumeRotation(float dx, float dy) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook"))
			return false;

		if (!invert.get())
			dy = -dy;

		yaw += dx * 0.15F;
		pitch += dy * 0.15F;

		if (pitch > 90) {
			pitch = 90;
		} else if (pitch < -90) {
			pitch = -90;
		}

		client.worldRenderer.onViewChanged();
		return true;
	}

	public float yaw(float defaultValue) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return yaw;
	}

	public float pitch(float defaultValue) {
		if (!(active.val || activeAlt.val) || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return pitch;
	}

	public boolean needsDisabling() {
		return mode.get().equals("freelook");
	}

	public boolean isActive() {
		return active.val || activeAlt.val;
	}

	private static class WrappedValue {
		boolean val;
	}
}
