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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Perspective;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class Freelook extends AbstractModule {

	private static final Freelook Instance = new Freelook();

	public final ForceableBooleanOption enabled = new ForceableBooleanOption("enabled", false);
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final OptionCategory category = OptionCategory.create("freelook");
	private static KeyBinding KEY = new KeyBinding("key.freelook", GLFW.GLFW_KEY_V, "category.axolotlclient");
	private final StringArrayOption mode = new StringArrayOption("mode",
		new String[]{"snap_perspective", "freelook"},
		"freelook", value -> FeatureDisabler.update());
	private final EnumOption<Perspective> perspective = new EnumOption<>("perspective", Perspective.class,
		Perspective.THIRD_PERSON_BACK);
	private final BooleanOption invert = new BooleanOption("invert", false);
	private final BooleanOption toggle = new BooleanOption("toggle", false);
	public boolean active;
	private float yaw, pitch;
	private Perspective previousPerspective;

	public static Freelook getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		KeyBindingHelper.registerKeyBinding(KEY);
		category.add(enabled, mode, perspective, invert, toggle);
		AxolotlClient.CONFIG.addCategory(category);
	}

	@Override
	public void tick() {
		if (!enabled.get())
			return;

		if (toggle.get()) {
			if (KEY.wasPressed()) {
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
		client.worldRenderer.scheduleTerrainUpdate();
		setPerspective(previousPerspective);
	}

	private void start() {
		active = true;


		previousPerspective = client.options.getPerspective();
		setPerspective(perspective.get());

		Entity camera = client.getCameraEntity();

		if (camera == null)
			camera = client.player;
		if (camera == null)
			return;

		yaw = camera.getYaw(0);
		pitch = camera.getPitch(0);
	}

	private void setPerspective(Perspective perspective) {
		MinecraftClient.getInstance().options.method_31043(perspective);
	}

	public boolean consumeRotation(double dx, double dy) {
		if (!active || !enabled.get() || !mode.get().equals("freelook"))
			return false;

		if (!invert.get())
			dy = -dy;

		if (MinecraftClient.getInstance().options.getPerspective().isFrontView()
			|| MinecraftClient.getInstance().options.getPerspective().isFirstPerson())
			dy *= -1;

		yaw += dx * 0.15F;
		pitch += dy * 0.15F;

		if (pitch > 90) {
			pitch = 90;
		} else if (pitch < -90) {
			pitch = -90;
		}

		client.worldRenderer.scheduleTerrainUpdate();
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
