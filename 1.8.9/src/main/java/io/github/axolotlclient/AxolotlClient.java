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

package io.github.axolotlclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.AxolotlClientConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.DefaultConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.common.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.StatusUpdateProviderImpl;
import io.github.axolotlclient.config.AxolotlClientConfig;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.modules.ModuleLoader;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.blur.MotionBlur;
import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import io.github.axolotlclient.modules.particles.Particles;
import io.github.axolotlclient.modules.renderOptions.BeaconBeam;
import io.github.axolotlclient.modules.rpc.DiscordRPC;
import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import io.github.axolotlclient.modules.sky.SkyResourceManager;
import io.github.axolotlclient.modules.tablist.Tablist;
import io.github.axolotlclient.modules.tnttime.TntTime;
import io.github.axolotlclient.modules.unfocusedFpsLimiter.UnfocusedFpsLimiter;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.FeatureDisabler;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.LoggerImpl;
import io.github.axolotlclient.util.notifications.Notifications;
import io.github.axolotlclient.util.translation.Translations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class AxolotlClient implements ClientModInitializer {

	public static final String MODID = "axolotlclient";
	public static String VERSION;
	public static final HashMap<Identifier, Resource> runtimeResources = new HashMap<>();
	public static final Identifier badgeIcon = new Identifier("axolotlclient", "textures/badge.png");
	public static final OptionCategory config = new OptionCategory("storedOptions");
	public static final BooleanOption someNiceBackground = new BooleanOption("defNoSecret", false);
	public static final List<Module> modules = new ArrayList<>();
	public static final Logger LOGGER = new LoggerImpl();
	public static AxolotlClientConfig CONFIG;
	public static ConfigManager configManager;

	@Override
	public void onInitializeClient() {

		VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow(IllegalStateException::new)
			.getMetadata().getVersion().getFriendlyString();

		CONFIG = new AxolotlClientConfig();
		config.add(someNiceBackground);

		getModules();
		addExternalModules();
		CONFIG.init();

		new API(LOGGER, Notifications.getInstance(), Translations.getInstance(), new StatusUpdateProviderImpl(), APIOptions.getInstance());

		modules.forEach(Module::init);

		CONFIG.config.addAll(CONFIG.getCategories());
		CONFIG.config.add(config);

		AxolotlClientConfigManager.getInstance().registerConfig(MODID, CONFIG, configManager = new DefaultConfigManager(MODID,
			FabricLoader.getInstance().getConfigDir().resolve("AxolotlClient.json"), CONFIG.config));
		AxolotlClientConfigManager.getInstance().addIgnoredName(MODID, "x");
		AxolotlClientConfigManager.getInstance().addIgnoredName(MODID, "y");

		modules.forEach(Module::lateInit);

        /*FabricLoader.getInstance().getModContainer("axolotlclient").ifPresent(modContainer -> {
            Optional<Path> optional = modContainer.findPath("resourcepacks/AxolotlClientUI.zip");
            optional.ifPresent(path -> MinecraftClient.getInstance().getResourcePackLoader().method_10366(path.toFile()));
        });*/

		ClientTickEvents.END_CLIENT_TICK.register(client -> tickClient());

		FeatureDisabler.init();

		LOGGER.debug("Debug Output enabled, Logs will be quite verbose!");

		LOGGER.info("AxolotlClient Initialized");
	}

	private static void getModules() {
		modules.add(SkyResourceManager.getInstance());
		modules.add(Zoom.getInstance());
		modules.add(HudManager.getInstance());
		modules.add(HypixelMods.getInstance());
		modules.add(MotionBlur.getInstance());
		modules.add(MenuBlur.getInstance());
		modules.add(ScrollableTooltips.getInstance());
		modules.add(DiscordRPC.getInstance());
		modules.add(Freelook.getInstance());
		modules.add(TntTime.getInstance());
		modules.add(Particles.getInstance());
		modules.add(ScreenshotUtils.getInstance());
		modules.add(BeaconBeam.getInstance());
		modules.add(UnfocusedFpsLimiter.getInstance());
		modules.add(Tablist.getInstance());
		modules.add(Auth.getInstance());
		modules.add(APIOptions.getInstance());
	}

	private static void addExternalModules() {
		modules.addAll(ModuleLoader.loadExternalModules());
	}

	public static void tickClient() {
		modules.forEach(Module::tick);
	}
}
