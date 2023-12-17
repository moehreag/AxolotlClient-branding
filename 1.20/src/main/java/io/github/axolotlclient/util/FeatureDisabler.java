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

package io.github.axolotlclient.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.freelook.Freelook;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.simple.ToggleSprintHud;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class FeatureDisabler {

	private static final HashMap<ForceableBooleanOption, String[]> disabledServers = new HashMap<>();
	private static final HashMap<ForceableBooleanOption, Supplier<Boolean>> conditions = new HashMap<>();

	private static final Supplier<Boolean> NONE = () -> true;
	private static final Identifier channelName = new Identifier("axolotlclient", "block_mods");
	// Features that can be disabled on the server's behalf
	// If something should be added here, feel free to ping us via your favorite way.
	private static final HashMap<String, ForceableBooleanOption> features = Util.make(() -> {
		HashMap<String, ForceableBooleanOption> features = new HashMap<>();
		features.put("freelook", Freelook.getInstance().enabled);
		features.put("timechanger", AxolotlClient.CONFIG.timeChangerEnabled);
		features.put("lowfire", AxolotlClient.CONFIG.lowFire);
		features.put("fullbright", AxolotlClient.CONFIG.fullBright);
		return features;
	});
	private static String currentAddress = "";

	@SuppressWarnings("deprecation") // no idea how to not make it use the non-deprecated method
	public static void init() {
		setServers(AxolotlClient.CONFIG.fullBright, NONE, "gommehd");
		setServers(AxolotlClient.CONFIG.lowFire, NONE, "gommehd");
		setServers(Freelook.getInstance().enabled, () -> Freelook.getInstance().needsDisabling(), "hypixel", "mineplex", "gommehd", "nucleoid", "mccisland");
		setServers(((ToggleSprintHud) HudManager.getInstance().get(ToggleSprintHud.ID)).toggleSneak, NONE, "hypixel");

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (handler.getServerInfo() != null) {
				onServerJoin(Objects.requireNonNull(handler.getServerInfo()).address);
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());

		ClientPlayConnectionEvents.INIT.register((handler0, client0) ->
			ClientPlayNetworking.registerGlobalReceiver(channelName, (client, handler, buf, responseSender) -> {
				JsonArray array = JsonParser.parseString(buf.readString()).getAsJsonArray();
				for (JsonElement element : array) {
					try {
						features.get(element.getAsString()).setForceOff(true, "ban_reason");
					} catch (Exception e) {
						AxolotlClient.LOGGER.error("Failed to disable " + element.getAsString() + "!");
					}
				}
			})
		);
	}

	private static void setServers(ForceableBooleanOption option, Supplier<Boolean> condition, String... servers) {
		disabledServers.put(option, servers);
		conditions.put(option, condition);
	}

	public static void onServerJoin(String address) {
		currentAddress = address;
		update();
	}

	public static void clear() {
		disabledServers.keySet().forEach(option -> option.setForceOff(false, ""));
		features.values().forEach(option -> option.setForceOff(false, ""));
	}

	public static void update() {
		disabledServers.forEach((option, strings) -> disableOption(option, strings, currentAddress));
	}

	private static void disableOption(ForceableBooleanOption option, String[] servers, String currentServer) {
		boolean ban = false;
		for (String s : servers) {
			if (currentServer.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
				ban = conditions.get(option).get();
				break;
			}
		}

		if (option.isForceOff() != ban) {
			option.setForceOff(ban, "ban_reason");
		}
	}
}
