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

package io.github.axolotlclient.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.*;
import io.github.axolotlclient.CommonOptions;
import io.github.axolotlclient.config.screen.CreditsScreen;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.Minecraft;

public class AxolotlClientConfig {

	public final BooleanOption showOwnNametag = new BooleanOption("showOwnNametag", false);
	public final BooleanOption useShadows = new BooleanOption("useShadows", false);
	public final BooleanOption nametagBackground = new BooleanOption("nametagBackground", true);

	public final BooleanOption showBadges = new BooleanOption("showBadges", true);
	public final BooleanOption customBadge = new BooleanOption("customBadge", false);
	public final StringOption badgeText = new StringOption("badgeText", "");

	public final ForceableBooleanOption timeChangerEnabled = new ForceableBooleanOption("enabled", false);
	public final IntegerOption customTime = new IntegerOption("time", 0, 0, 24000);
	public final BooleanOption customSky = new BooleanOption("customSky", true);
	public final IntegerOption cloudHeight = new IntegerOption("cloudHeight", 128, 100, 512);
	public final BooleanOption dynamicFOV = new BooleanOption("dynamicFov", true);
	public final ForceableBooleanOption fullBright = new ForceableBooleanOption("fullBright", false);
	public final BooleanOption removeVignette = new BooleanOption("removeVignette", false);
	public final ForceableBooleanOption lowFire = new ForceableBooleanOption("lowFire", false);
	public final ColorOption hitColor = new ColorOption("hitColor", new Color(255, 0, 0, 77));
	public final BooleanOption minimalViewBob = new BooleanOption("minimalViewBob", false);
	public final BooleanOption noHurtCam = new BooleanOption("noHurtCam", false);
	public final BooleanOption flatItems = new BooleanOption("flatItems", false);
	public final BooleanOption inventoryPotionEffectOffset = new BooleanOption("inventory.potion_effect_offset", true);

	public final ColorOption loadingScreenColor = new ColorOption("loadingBgColor", new Color(-1));
	public final BooleanOption nightMode = new BooleanOption("nightMode", false);
	public final BooleanOption rawMouseInput = new BooleanOption("rawMouseInput", false);

	public final BooleanOption enableCustomOutlines = new BooleanOption("enabled", false);
	public final ColorOption outlineColor = new ColorOption("color", Color.parse("#DD000000"));
	public final IntegerOption outlineWidth = new IntegerOption("outlineWidth", 1, 1, 10);

	public final BooleanOption noRain = new BooleanOption("noRain", false);

	public final BooleanOption debugLogOutput = new BooleanOption("debugLogOutput", false);
	public final GenericOption openCredits = new GenericOption("Credits", "Open Credits",
		() -> Minecraft.getInstance()
			.openScreen(new CreditsScreen(Minecraft.getInstance().screen)));
	public final BooleanOption creditsBGM = new BooleanOption("creditsBGM", true);
	public final BooleanOption customWindowTitle = new BooleanOption("customWindowTitle", true);

	public final OptionCategory general = OptionCategory.create("general");
	public final OptionCategory nametagOptions = OptionCategory.create("nametagOptions");
	public final OptionCategory rendering = OptionCategory.create("rendering");
	public final OptionCategory outlines = OptionCategory.create("blockOutlines");
	public final OptionCategory timeChanger = OptionCategory.create("timeChanger");
	@Getter
	private final OptionCategory config = OptionCategory.create("config");

	@Getter
	private final List<Option<?>> options = new ArrayList<>();

	public void add(Option<?> option) {
		options.add(option);
	}

	public void addCategory(OptionCategory cat) {
		config.add(cat);
	}

	public void init() {
		config.add(general);
		config.add(nametagOptions);
		config.add(rendering);

		nametagOptions.add(showOwnNametag);
		nametagOptions.add(useShadows);
		nametagOptions.add(nametagBackground);

		nametagOptions.add(showBadges);
		nametagOptions.add(customBadge);
		nametagOptions.add(badgeText);

		general.add(loadingScreenColor);
		general.add(nightMode);
		general.add(customWindowTitle);
		general.add(rawMouseInput);
		general.add(openCredits);
		general.add(debugLogOutput);
		general.add(CommonOptions.datetimeFormat);
		ConfigUI.getInstance().runWhenLoaded(() -> {
			general.getOptions().removeIf(o -> "configStyle".equals(o.getName()));
			boolean isPojavLauncher = Objects.requireNonNullElse(System.getenv("TMPDIR"), "").contains("/Android/data/net.kdt.pojavlaunch/");
			String[] themes = ConfigUI.getInstance().getStyleNames().stream().map(s -> "configStyle." + s)
				.filter(s -> !isPojavLauncher || !s.startsWith("rounded"))
				.toArray(String[]::new);
			if (themes.length > 1) {
				StringArrayOption configStyle;
				general.add(configStyle = new StringArrayOption("configStyle", themes,
					"configStyle." + ConfigUI.getInstance().getCurrentStyle().getName(), s -> {
					ConfigUI.getInstance().setStyle(s.split("\\.")[1]);
					Minecraft.getInstance().openScreen(null);
				}));
				AxolotlClient.configManager.load();
				ConfigUI.getInstance().setStyle(configStyle.get().split("\\.")[1]);
			}
		});

		rendering.add(customSky,
			cloudHeight,
			dynamicFOV,
			fullBright,
			removeVignette,
			lowFire,
			hitColor,
			minimalViewBob,
			flatItems,
			noHurtCam,
			inventoryPotionEffectOffset);

		timeChanger.add(timeChangerEnabled);
		timeChanger.add(customTime);
		rendering.add(timeChanger);

		outlines.add(enableCustomOutlines);
		outlines.add(outlineColor);
		outlines.add(outlineWidth);
		rendering.add(outlines);

		rendering.add(noRain);

		AxolotlClient.config.add(creditsBGM);
	}
}
