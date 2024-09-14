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

package io.github.axolotlclient.modules.rpc;

import java.time.Instant;
import java.util.Optional;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventHandler;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.modules.rpc.gameSdk.GameSdkDownloader;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

/**
 * This DiscordRPC module is derived from <a href="https://github.com/DeDiamondPro/HyCord">HyCord</a>.
 * @license GPL-3.0
 * @author DeDiamondPro
 */

public class DiscordRPC extends AbstractModule {

    private static DiscordRPC Instance;

    public OptionCategory category = new OptionCategory("rpc");

    public BooleanOption enabled = new BooleanOption("enabled", value -> {
        if (value) {
            initRPC();
        } else {
            shutdown();
        }
    }, false);
    public BooleanOption showActivity = new BooleanOption("showActivity", true);
    public EnumOption showServerNameMode = new EnumOption("showServerNameMode",
            new String[] { "showIp", "showName", "off" }, "off");
    public BooleanOption showTime = new BooleanOption("showTime", true);

    public static Activity currentActivity;
    public static Core discordRPC;
    Instant time = Instant.now();
    private static String modVersion;

    private static boolean running;

    public static DiscordRPC getInstance() {
        if (Instance == null)
            Instance = new DiscordRPC();
        return Instance;
    }

    @Override
    public void init() {
        category.add(enabled, showTime, showActivity, showServerNameMode);

        AxolotlClient.CONFIG.addCategory(category);

        if (OSUtil.getOS() == OSUtil.OperatingSystem.OTHER) {
            enabled.setForceOff(true, I18n.translate("crash"));
        }

        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("axolotlclient");
        container.ifPresent(modContainer -> modVersion = modContainer.getMetadata().getVersion().getFriendlyString());
    }

    @SuppressWarnings("BusyWait")
    public void initRPC() {
        if (enabled.get()) {
            GameSdkDownloader.downloadSdk();
        }

        if (enabled.get()) {
            CreateParams params = new CreateParams();

            params.setClientID(875835666729152573L);
            params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);

            DiscordEventHandler handler = new DiscordEventHandler();
            params.registerEventHandler(handler);

            try {
                discordRPC = new Core(params);

                running = true;
                Thread callBacks = new Thread(() -> {
                    while (enabled.get() && running) {
                        discordRPC.runCallbacks();

                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    discordRPC.close();
                    Thread.currentThread().interrupt();
                });
                callBacks.setName("Discord RPC Thread");
                callBacks.setDaemon(true);
                callBacks.start();
                Logger.info("Started RPC Core");
            } catch (Exception e) {
                if (!e.getMessage().contains("INTERNAL_ERROR")) {
                    Logger.error("An error occurred: ");
                    e.printStackTrace();
                } else {
                    Logger.debug("Error starting RPC: ", e);
                    enabled.set(false);
                }
            }
        }
    }

    public void updateActivity() {
        Activity activity = new Activity();

        String state;
        switch (showServerNameMode.get()) {
            case "showIp":
                state = MinecraftClient.getInstance().world == null ? "In the menu"
                        : (Util.getCurrentServerAddress() == null ? "Singleplayer" : Util.getCurrentServerAddress());
                break;
            case "showName":
                state = MinecraftClient.getInstance().world == null ? "In the menu"
                        : (MinecraftClient.getInstance().getCurrentServerEntry() == null
                                ? (Util.getCurrentServerAddress() == null ? "Singleplayer"
                                        : Util.getCurrentServerAddress())
                                : MinecraftClient.getInstance().getCurrentServerEntry().name);
                break;
            case "off":
            default:
                state = "";
                break;
        }

        if (showActivity.get() && Util.getCurrentServerAddress() != null) {
            activity.setDetails(Util.getGame());
        } else if (showActivity.get() && currentActivity != null) {
            activity.setDetails(currentActivity.getDetails());
        }

        activity.setState(state);
        activity.setType(ActivityType.PLAYING);

        if (showTime.get()) {
            activity.timestamps().setStart(Instant.ofEpochMilli(time.toEpochMilli()));
        }

        if (currentActivity != null) {
            currentActivity.close();
        }

        activity.assets().setLargeText("AnupClient " + modVersion);
        activity.assets().setLargeImage("icon");
        discordRPC.activityManager().updateActivity(activity);
        currentActivity = activity;
    }

    public static void setWorld(String world) {
        if (running) {
            if (currentActivity == null) {
                DiscordRPC.getInstance().updateRPC();
            }

            currentActivity.setDetails("World: " + world);
            if (discordRPC.isOpen()) {
                discordRPC.activityManager().updateActivity(currentActivity);
            }
        }
    }

    public void updateRPC() {
        if (discordRPC != null && discordRPC.isOpen()) {
            updateActivity();
        }
    }

    @Override
    public void tick() {
        if (!running && enabled.get()) {
            initRPC();
        }

        if (running) {
            updateRPC();
        }
    }

    public static void shutdown() {
        running = false;
    }
}
