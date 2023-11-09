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

package io.github.axolotlclient.config;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.AxolotlClientConfigConfig;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.common.ConfigHolder;
import io.github.axolotlclient.AxolotlClientConfig.options.*;
import io.github.axolotlclient.config.screen.CreditsScreen;
import net.minecraft.client.MinecraftClient;

public class AxolotlClientConfig extends ConfigHolder {

	public final BooleanOption showOwnNametag = new BooleanOption("showOwnNametag", false);
	public final BooleanOption useShadows = new BooleanOption("useShadows", false);
	public final BooleanOption nametagBackground = new BooleanOption("nametagBackground", true);

	public final BooleanOption showBadges = new BooleanOption("showBadges", true);
	public final BooleanOption customBadge = new BooleanOption("customBadge", false);
	public final StringOption badgeText = new StringOption("badgeText", "");

	public final BooleanOption timeChangerEnabled = new BooleanOption("enabled", false);
	public final IntegerOption customTime = new IntegerOption("time", 0, 0, 24000);
	public final BooleanOption customSky = new BooleanOption("customSky", true);
	public final IntegerOption cloudHeight = new IntegerOption("cloudHeight", 128, 100, 512);
	public final BooleanOption dynamicFOV = new BooleanOption("dynamicFov", true);
	public final BooleanOption fullBright = new BooleanOption("fullBright", false);
	public final BooleanOption removeVignette = new BooleanOption("removeVignette", false);
	public final BooleanOption lowFire = new BooleanOption("lowFire", false);
	public final ColorOption hitColor = new ColorOption("hitColor", new Color(255, 0, 0, 77));
	public final BooleanOption minimalViewBob = new BooleanOption("minimalViewBob", false);
	public final BooleanOption noHurtCam = new BooleanOption("noHurtCam", false);
	public final BooleanOption flatItems = new BooleanOption("flatItems", false);

	public final ColorOption loadingScreenColor = new ColorOption("loadingBgColor", new Color(-1));
	public final BooleanOption nightMode = new BooleanOption("nightMode", false);
	public final BooleanOption rawMouseInput = new BooleanOption("rawMouseInput", false);

	public final BooleanOption enableCustomOutlines = new BooleanOption("enabled", false);
	public final ColorOption outlineColor = new ColorOption("color", Color.parse("#DD000000"));
	public final IntegerOption outlineWidth = new IntegerOption("outlineWidth", 1, 1, 10);

	public final BooleanOption noRain = new BooleanOption("noRain", false);
	public final BooleanOption showSaturation = new BooleanOption("showSaturation", false);

	public final BooleanOption debugLogOutput = new BooleanOption("debugLogOutput", false);
	public final GenericOption openCredits = new GenericOption("Credits", "Open Credits",
		(mouseX, mouseY) -> MinecraftClient.getInstance()
			.setScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen)));
	public final BooleanOption creditsBGM = new BooleanOption("creditsBGM", true);
	public final BooleanOption customWindowTitle = new BooleanOption("customWindowTitle", true);

	public final OptionCategory general = new OptionCategory("general");
	public final OptionCategory nametagOptions = new OptionCategory("nametagOptions");
	public final OptionCategory rendering = new OptionCategory("rendering");
	public final OptionCategory outlines = new OptionCategory("blockOutlines");
	public final OptionCategory timeChanger = new OptionCategory("timeChanger");
	public final OptionCategory searchFilters = new OptionCategory("searchFilters");
	public final List<io.github.axolotlclient.AxolotlClientConfig.common.options.OptionCategory> config = new ArrayList<>();
	private final List<Option<?>> options = new ArrayList<>();
	private final List<io.github.axolotlclient.AxolotlClientConfig.common.options.OptionCategory> categories = new ArrayList<>();

	public void add(Option<?> option) {
		options.add(option);
	}

	public void addCategory(OptionCategory cat) {
		categories.add(cat);
	}

	public List<io.github.axolotlclient.AxolotlClientConfig.common.options.OptionCategory> getCategories() {
		return categories;
	}

	public List<Option<?>> getOptions() {
		return options;
	}

	public void init() {
		categories.add(general);
		categories.add(nametagOptions);
		categories.add(rendering);

		categories.forEach(io.github.axolotlclient.AxolotlClientConfig.common.options.OptionCategory::clearOptions);

		nametagOptions.add(showOwnNametag);
		nametagOptions.add(useShadows);
		nametagOptions.add(nametagBackground);

		nametagOptions.add(showBadges);
		nametagOptions.add(customBadge);
		nametagOptions.add(badgeText);

		general.add(loadingScreenColor);
		general.add(nightMode);
		general.add(AxolotlClientConfigConfig.showQuickToggles);
		general.add(AxolotlClientConfigConfig.showOptionTooltips);
		general.add(AxolotlClientConfigConfig.showCategoryTooltips);
		general.add(customWindowTitle);
		general.add(rawMouseInput);
		general.add(openCredits);
		general.add(debugLogOutput);

		searchFilters.add(AxolotlClientConfigConfig.searchIgnoreCase, AxolotlClientConfigConfig.searchForOptions,
			AxolotlClientConfigConfig.searchSort, AxolotlClientConfigConfig.searchSortOrder);
		general.add(searchFilters);

		rendering.add(customSky,
			cloudHeight,
			AxolotlClientConfigConfig.chromaSpeed,
			dynamicFOV,
			fullBright,
			removeVignette,
			lowFire,
			hitColor,
			minimalViewBob,
			flatItems,
			showSaturation,
			noHurtCam);

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
