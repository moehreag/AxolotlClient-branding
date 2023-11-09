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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.KeyBindOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.options.StringOption;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ToggleSprintHud extends SimpleTextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "togglesprint");
	public final BooleanOption toggleSneak = new BooleanOption("toggleSneak", ID.getPath(), false);
	private final BooleanOption toggleSprint = new BooleanOption("toggleSprint", ID.getPath(), false);
	private final BooleanOption randomPlaceholder = new BooleanOption("randomPlaceholder", ID.getPath(), false);
	private final StringOption placeholder = new StringOption("placeholder", ID.getPath(), "No keys pressed");

	private final KeyBinding sprintToggle = new KeyBinding("key.toggleSprint", GLFW.GLFW_KEY_K, "category.axolotlclient");
	private final KeyBindOption sprintKey = new KeyBindOption("key.toggleSprint", sprintToggle, (key) -> {
	});
	private final KeyBinding sneakToggle = new KeyBinding("key.toggleSneak", GLFW.GLFW_KEY_I, "category.axolotlclient");
	private final KeyBindOption sneakKey = new KeyBindOption("key.toggleSneak", sneakToggle, (key) -> {
	});

	@Getter
	private final BooleanOption sprintToggled = new BooleanOption("sprintToggled", ID.getPath(), false);
	@Getter
	private final BooleanOption sneakToggled = new BooleanOption("sneakToggled", ID.getPath(), false);
	private final List<String> texts = new ArrayList<>();
	private boolean sprintWasPressed = false;
	private boolean sneakWasPressed = false;
	private String text = "";

	public ToggleSprintHud() {
		super(100, 20, false);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (sprintToggle.isPressed() != sprintWasPressed && sprintToggle.isPressed() && toggleSprint.get()) {
			sprintToggled.toggle();
			sprintWasPressed = sprintToggle.isPressed();
		} else if (!sprintToggle.isPressed()) {
			sprintWasPressed = false;
		}
		if (sneakToggle.isPressed() != sneakWasPressed && sneakToggle.isPressed() && toggleSneak.get()) {
			sneakToggled.toggle();
			sneakWasPressed = sneakToggle.isPressed();
		} else if (!sneakToggle.isPressed()) {
			sneakWasPressed = false;
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(toggleSprint);
		options.add(sprintKey);
		options.add(toggleSneak);
		options.add(sneakKey);
		options.add(randomPlaceholder);
		options.add(placeholder);
		return options;
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public String getValue() {
		if (client.options.keySneak.isPressed()) {
			return I18n.translate("sneaking_pressed");
		}
		if (client.options.keySprint.isPressed()) {
			return I18n.translate("sprinting_pressed");
		}

		if (toggleSneak.get() && sneakToggled.get()) {
			return I18n.translate("sneaking_toggled");
		}
		if (toggleSprint.get() && sprintToggled.get()) {
			return I18n.translate("sprinting_toggled");
		}
		return getPlaceholder();
	}

	private String getRandomPlaceholder() {
		if (Objects.equals(text, "")) {
			loadRandomPlaceholder();
		}
		return text;
	}

	private void loadRandomPlaceholder() {
		try {
			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(
					MinecraftClient.getInstance().getResourceManager()
						.getResource(new Identifier("texts/splashes.txt")).getInputStream(),
					StandardCharsets.UTF_8));
			String string;
			while ((string = bufferedReader.readLine()) != null) {
				string = string.trim();
				if (!string.isEmpty()) {
					texts.add(string);
				}
			}

			text = texts.get(new Random().nextInt(texts.size()));
		} catch (Exception e) {
			text = "";
		}
	}

	@Override
	public String getPlaceholder() {
		return randomPlaceholder.get() ? getRandomPlaceholder() : placeholder.get();
	}

	@Override
	public List<Option<?>> getSaveOptions() {
		List<Option<?>> options = super.getSaveOptions();
		options.add(sprintToggled);
		options.add(sneakToggled);
		return options;
	}
}
