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

package io.github.axolotlclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.VersionedJsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.StatusUpdateProviderImpl;
import io.github.axolotlclient.config.AxolotlClientConfig;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.modules.ModuleLoader;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.blur.MotionBlur;
import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import io.github.axolotlclient.modules.mcci.MccIslandMods;
import io.github.axolotlclient.modules.particles.Particles;
import io.github.axolotlclient.modules.renderOptions.BeaconBeam;
import io.github.axolotlclient.modules.rpc.DiscordRPC;
import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import io.github.axolotlclient.modules.tablist.Tablist;
import io.github.axolotlclient.modules.tnttime.TntTime;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.FeatureDisabler;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.LoggerImpl;
import io.github.axolotlclient.util.notifications.Notifications;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class AxolotlClient implements ClientModInitializer {

	public static final String MODID = "axolotlclient";
	public static final HashMap<Identifier, Resource> runtimeResources = new HashMap<>();
	public static final Identifier badgeIcon = Identifier.of("axolotlclient", "textures/badge.png");
	public static final OptionCategory config = OptionCategory.create("storedOptions");
	public static final BooleanOption someNiceBackground = new BooleanOption("defNoSecret", false);
	public static final List<Module> modules = new ArrayList<>();
	public static final Logger LOGGER = new LoggerImpl();
	public static String VERSION;
	public static AxolotlClientConfig CONFIG;
	public static ConfigManager configManager;

	public static void getModules() {
		modules.add(Zoom.getInstance());
		modules.add(HudManager.getInstance());
		modules.add(HypixelMods.getInstance());
		modules.add(MotionBlur.getInstance());
		modules.add(ScrollableTooltips.getInstance());
		modules.add(DiscordRPC.getInstance());
		modules.add(Freelook.getInstance());
		modules.add(TntTime.getInstance());
		modules.add(Particles.getInstance());
		modules.add(ScreenshotUtils.getInstance());
		modules.add(BeaconBeam.getInstance());
		modules.add(Tablist.getInstance());
		modules.add(Auth.getInstance());
		modules.add(APIOptions.getInstance());
		modules.add(MccIslandMods.getInstance());
	}

	private static void addExternalModules() {
		modules.addAll(ModuleLoader.loadExternalModules());
	}

	public static void tickClient() {
		modules.forEach(Module::tick);
	}

	@Override
	public void onInitializeClient() {

		VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();

		CONFIG = new AxolotlClientConfig();
		config.add(someNiceBackground);

		getModules();
		addExternalModules();

		CONFIG.init();

		new AxolotlClientCommon(LOGGER, Notifications.getInstance(), () -> configManager);
		new API(LOGGER, I18n::translate, new StatusUpdateProviderImpl(), APIOptions.getInstance());
		ClientLifecycleEvents.CLIENT_STOPPING.register(c -> API.getInstance().shutdown());

		modules.forEach(Module::init);

		CONFIG.getConfig().add(config);

		io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig.getInstance()
			.register(configManager = new VersionedJsonConfigManager(FabricLoader.getInstance().getConfigDir().resolve("AxolotlClient.json"),
				CONFIG.getConfig(), 1, (oldVersion, newVersion, config, json) -> {
				// convert changed Options between versions here
				return json;
			}));
		configManager.load();
		configManager.suppressName("x");
		configManager.suppressName("y");
		configManager.suppressName(config.getName());

		modules.forEach(Module::lateInit);

		ClientTickEvents.END_CLIENT_TICK.register(client -> tickClient());

		FeatureDisabler.init();

		LOGGER.debug("Debug Output activated, Logs will be more verbose!");

		LOGGER.info("AxolotlClient Initialized");
	}
}
