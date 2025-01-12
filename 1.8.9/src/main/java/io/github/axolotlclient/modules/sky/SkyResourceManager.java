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

package io.github.axolotlclient.modules.sky;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.moehreag.searchInResources.SearchableResourceManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.Resource;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

public class SkyResourceManager extends AbstractModule {

	@Getter
	private static final SkyResourceManager instance = new SkyResourceManager();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public void reload(ResourceManager resourceManager) {
		SkyboxManager.getInstance().clearSkyboxes();
		for (Map.Entry<Identifier, Resource> entry : resourceManager
			.findResources("fabricskyboxes", "sky", identifier -> identifier.getPath().endsWith(".json"))
			.entrySet()) {
			if (entry.getKey().getNamespace().equals("celestial")) { // Skip Celestial Packs, we cannot load them.
				continue;
			}
			AxolotlClient.LOGGER.debug("Loaded sky: " + entry.getKey());
			SkyboxManager.getInstance().addSkybox(new FSBSkyboxInstance(gson.fromJson(
				new BufferedReader(new InputStreamReader(entry.getValue().asStream(), StandardCharsets.UTF_8))
					.lines().collect(Collectors.joining("\n")),
				JsonObject.class)));
		}

		for (Map.Entry<Identifier, Resource> entry : ((SearchableResourceManager) resourceManager)
			.findResources("minecraft", "optifine/sky", identifier -> isMCPSky(identifier.getPath()))
			.entrySet()) {
			AxolotlClient.LOGGER.debug("Loaded sky: " + entry.getKey());
			loadMCPSky("optifine", entry.getKey(), entry.getValue());
		}

		for (Map.Entry<Identifier, Resource> entry : ((SearchableResourceManager) resourceManager)
			.findResources("minecraft", "mcpatcher/sky", identifier -> isMCPSky(identifier.getPath()))
			.entrySet()) {
			AxolotlClient.LOGGER.debug("Loaded sky: " + entry.getKey());
			loadMCPSky("mcpatcher", entry.getKey(), entry.getValue());
		}
	}

	private boolean isMCPSky(String path) {
		return path.endsWith(".properties") && path.startsWith("sky");
	}

	private void loadMCPSky(String loader, Identifier id, Resource resource) {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(resource.asStream(), StandardCharsets.UTF_8));

		JsonObject object = new JsonObject();
		String string;
		String[] option;
		try {
			while ((string = reader.readLine()) != null) {
				try {
					if (!string.startsWith("#")) {
						option = string.split("=");
						if (option[0].equals("source")) {
							if (option[1].contains(":")) {
								option[1] = option[1].split(":")[1];
							} else {
								if (option[1].startsWith("assets")) {
									option[1] = option[1].replace("./", "").replace("assets/minecraft/", "");
								}
								if (id.getPath().contains("world")) {
									option[1] = loader + "/sky/world" + id.getPath().split("world")[1].split("/")[0]
												+ "/" + option[1].replace("./", "");
								}
							}
						}
						if (option[0].equals("startFadeIn") || option[0].equals("endFadeIn")
							|| option[0].equals("startFadeOut") || option[0].equals("endFadeOut")) {
							option[1] = option[1].replace(":", "").replace("\\", "");
						}

						object.addProperty(option[0], option[1]);
					}
				} catch (Exception ignored) {
				}
			}

			SkyboxManager.getInstance().addSkybox(new MCPSkyboxInstance(object));
		} catch (Exception ignored) {
		}
	}

	@Override
	public void init() {
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(() -> reload(Minecraft.getInstance().getResourceManager()));
	}
}
