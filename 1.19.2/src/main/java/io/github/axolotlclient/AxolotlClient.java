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
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.FeatureDisabler;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.LoggerImpl;
import io.github.axolotlclient.util.UnsupportedMod;
import io.github.axolotlclient.util.notifications.Notifications;
import io.github.axolotlclient.util.translation.Translations;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

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
	public static UnsupportedMod badmod;
	public static boolean titleDisclaimer = false;
	public static boolean showWarning = true;

	@Override
	public void onInitializeClient(ModContainer container) {

		VERSION = QuiltLoader.getModContainer(MODID).orElseThrow().metadata().version().raw();

		if (QuiltLoader.isModLoaded("ares")) {
			badmod = new UnsupportedMod("Ares Client", UnsupportedMod.UnsupportedReason.BAN_REASON);
		} else if (QuiltLoader.isModLoaded("inertia")) {
			badmod = new UnsupportedMod("Inertia Client", UnsupportedMod.UnsupportedReason.BAN_REASON);
		} else if (QuiltLoader.isModLoaded("meteor-client")) {
			badmod = new UnsupportedMod("Meteor Client", UnsupportedMod.UnsupportedReason.BAN_REASON);
		} else if (QuiltLoader.isModLoaded("wurst")) {
			badmod = new UnsupportedMod("Wurst Client", UnsupportedMod.UnsupportedReason.BAN_REASON);
		} else if (QuiltLoader.isModLoaded("baritone")) {
			badmod = new UnsupportedMod("Baritone", UnsupportedMod.UnsupportedReason.BAN_REASON);
		} else if (QuiltLoader.isModLoaded("essential-container")) {
			badmod = new UnsupportedMod("Essential", UnsupportedMod.UnsupportedReason.MIGHT_CRASH,
				UnsupportedMod.UnsupportedReason.UNKNOWN_CONSEQUENSES);
		} else if (QuiltLoader.isModLoaded("optifabric")) {
			badmod = new UnsupportedMod("OptiFine", UnsupportedMod.UnsupportedReason.MIGHT_CRASH,
				UnsupportedMod.UnsupportedReason.UNKNOWN_CONSEQUENSES);
		} else {
			showWarning = false;
		}

		CONFIG = new AxolotlClientConfig();
		config.add(someNiceBackground);

		getModules();
		addExternalModules();

		CONFIG.init();

		new API(LOGGER, Notifications.getInstance(), Translations.getInstance(), new StatusUpdateProviderImpl(), APIOptions.getInstance());

		modules.forEach(Module::init);

		CONFIG.getConfig().addAll(CONFIG.getCategories());
		CONFIG.getConfig().add(config);

		AxolotlClientConfigManager.getInstance().registerConfig(MODID, CONFIG, configManager = new DefaultConfigManager(MODID,
			QuiltLoader.getConfigDir().resolve("AxolotlClient.json"), CONFIG.getConfig()));
		AxolotlClientConfigManager.getInstance().addIgnoredName(MODID, "x");
		AxolotlClientConfigManager.getInstance().addIgnoredName(MODID, "y");

		modules.forEach(Module::lateInit);

		ResourceLoader.registerBuiltinResourcePack(new Identifier("axolotlclient", "axolotlclient-ui"), container,
			ResourcePackActivationType.NORMAL);
		ClientTickEvents.END.register(client -> tickClient());

		FeatureDisabler.init();

		LOGGER.debug("Debug Output activated, Logs will be more verbose!");

		LOGGER.info("AxolotlClient Initialized");
	}

	public static void getModules() {
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
