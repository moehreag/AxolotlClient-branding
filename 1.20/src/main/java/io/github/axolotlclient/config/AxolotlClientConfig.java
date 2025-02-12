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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.ConfigUI;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.*;
import io.github.axolotlclient.CommonOptions;
import io.github.axolotlclient.config.screen.CreditsScreen;
import io.github.axolotlclient.mixin.OverlayTextureAccessor;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;

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
	public final BooleanOption dynamicFOV = new BooleanOption("dynamicFov", true);
	public final ForceableBooleanOption fullBright = new ForceableBooleanOption("fullBright", false);
	public final BooleanOption removeVignette = new BooleanOption("removeVignette", false);
	public final ForceableBooleanOption lowFire = new ForceableBooleanOption("lowFire", false);
	public final BooleanOption lowShield = new BooleanOption("lowShield", false);
	public final ColorOption hitColor = new ColorOption("hitColor", new Color(255, 0, 0, 77),
		value -> {
			try { // needed because apparently someone created a bug that makes this be called when the config is loaded. Will be fixed with the next release.
				NativeImageBackedTexture texture = ((OverlayTextureAccessor) MinecraftClient.getInstance().gameRenderer.getOverlayTexture()).axolotlclient$getTexture();
				NativeImage nativeImage = texture.getImage();
				if (nativeImage != null) {
					int color = 255 - value.getAlpha();
					color = (color << 8) + value.getBlue();
					color = (color << 8) + value.getGreen();
					color = (color << 8) + value.getRed();

					for (int i = 0; i < 8; ++i) {
						for (int j = 0; j < 8; ++j) {
							nativeImage.setPixelColor(j, i, color);
						}
					}

					RenderSystem.activeTexture(33985);
					texture.bindTexture();
					nativeImage.upload(0, 0, 0, 0, 0,
						nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
					RenderSystem.activeTexture(33984);
				}
			} catch (Exception ignored) {
			}
		});
	public final BooleanOption minimalViewBob = new BooleanOption("minimalViewBob", false);
	public final BooleanOption noHurtCam = new BooleanOption("noHurtCam", false);
	public final BooleanOption flatItems = new BooleanOption("flatItems", false);

	public final ColorOption loadingScreenColor = new ColorOption("loadingBgColor", new Color(239, 50, 61, 255));
	public final BooleanOption nightMode = new BooleanOption("nightMode", false);
	public final BooleanOption customWindowTitle = new BooleanOption("customWindowTitle", true);

	public final BooleanOption enableCustomOutlines = new BooleanOption("enabled", false);
	public final ColorOption outlineColor = new ColorOption("color", Color.parse("#DD000000"));

	public final BooleanOption noRain = new BooleanOption("noRain", false);

	public final GenericOption openCredits = new GenericOption("Credits", "Open Credits", () ->
		MinecraftClient.getInstance().setScreen(new CreditsScreen(MinecraftClient.getInstance().currentScreen))
	);
	public final BooleanOption debugLogOutput = new BooleanOption("debugLogOutput", false);
	public final BooleanOption creditsBGM = new BooleanOption("creditsBGM", true);

	public final OptionCategory general = OptionCategory.create("general");
	public final OptionCategory nametagOptions = OptionCategory.create("nametagOptions");
	public final OptionCategory rendering = OptionCategory.create("rendering");
	public final OptionCategory outlines = OptionCategory.create("blockOutlines");
	public final OptionCategory timeChanger = OptionCategory.create("timeChanger");

	@Getter
	private final List<Option<?>> options = new ArrayList<>();

	@Getter
	private final OptionCategory config = OptionCategory.create("config");

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

		rendering.add(outlines);

		nametagOptions.add(showOwnNametag);
		nametagOptions.add(useShadows);
		nametagOptions.add(nametagBackground);

		nametagOptions.add(showBadges);
		nametagOptions.add(customBadge);
		nametagOptions.add(badgeText);

		general.add(loadingScreenColor);
		general.add(nightMode);
		general.add(customWindowTitle);
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
					MinecraftClient.getInstance().setScreen(null);
				}));
				AxolotlClient.configManager.load();
				ConfigUI.getInstance().setStyle(configStyle.get().split("\\.")[1]);
			}
		});

		rendering.add(customSky,
			dynamicFOV,
			fullBright,
			removeVignette,
			lowFire,
			lowShield,
			hitColor,
			minimalViewBob,
			flatItems,
			noHurtCam);

		timeChanger.add(timeChangerEnabled);
		timeChanger.add(customTime);
		rendering.add(timeChanger);

		outlines.add(enableCustomOutlines);
		outlines.add(outlineColor);

		rendering.add(noRain);

		AxolotlClient.config.add(creditsBGM);

	}

}
