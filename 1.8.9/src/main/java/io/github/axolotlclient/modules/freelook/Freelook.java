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

package io.github.axolotlclient.modules.freelook;

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
	public final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", false);
	private final Minecraft client = Minecraft.getInstance();
	private final OptionCategory category = OptionCategory.create("freelook");
	private final StringArrayOption mode = new StringArrayOption("mode",
		new String[]{"snap_perspective", "freelook"},
		"freelook", value -> FeatureDisabler.update());
	private final EnumOption<Perspective> perspective = new EnumOption<>("perspective", Perspective.class,
		Perspective.THIRD_PERSON_BACK);
	private final BooleanOption invert = new BooleanOption("invert", false);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	private float yaw, pitch;
	private boolean active;
	private int previousPerspective;

	public static Freelook getInstance() {
		return INSTANCE;
	}

	@Override
	public void init() {
		KeyBindingEvents.REGISTER_KEYBINDS.register(r -> r.register(KEY));
		category.add(enabled, mode, perspective, invert, toggle);
		AxolotlClient.CONFIG.addCategory(category);
	}

	@Override
	public void tick() {
		if (!enabled.get())
			return;

		if (toggle.get()) {
			if (KEY.consumeClick()) {
				if (active) {
					stop();
				} else {
					start();
				}
			}
		} else {
			if (KEY.isPressed()) {
				if (!active) {
					start();
				}
			} else if (active) {
				stop();
			}
		}
	}

	private void stop() {
		active = false;
		client.worldRenderer.onViewChanged();
		client.gameRenderer.updateShader(client.getCamera());
		client.options.perspective = previousPerspective;
	}

	private void start() {
		active = true;

		previousPerspective = client.options.perspective;
		client.options.perspective = perspective.get().ordinal();

		Entity camera = client.getCamera();

		if (camera == null)
			camera = client.player;
		if (camera == null)
			return;

		yaw = camera.yaw;
		pitch = camera.pitch;
	}

	public boolean consumeRotation(float dx, float dy) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
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
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return yaw;
	}

	public float pitch(float defaultValue) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return defaultValue;

		return pitch;
	}

	public boolean needsDisabling() {
		return mode.get().equals("freelook");
	}
}
