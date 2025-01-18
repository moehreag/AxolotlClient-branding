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
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

public class SkyResourceManager extends AbstractModule implements SimpleSynchronousResourceReloadListener {

	private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Getter
	private final static SkyResourceManager Instance = new SkyResourceManager();

	@Override
	public void init() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
	}

	@Override
	public void apply(ResourceManager manager) {
		try {
			AxolotlClient.LOGGER.debug("Loading Custom Skies!");
			SkyboxManager.getInstance().clearSkyboxes();

			for (Identifier entry : manager
				.findResources("sky", identifier -> identifier.endsWith(".json"))) {
				if (entry.getNamespace().equals("celestial")) { // Skip Celestial Packs, we cannot load them.
					continue;
				}
				AxolotlClient.LOGGER.debug("Loading FSB sky from " + entry);
				JsonObject json = gson.fromJson(
					new BufferedReader(new InputStreamReader(manager.getResource(entry).getInputStream(), StandardCharsets.UTF_8))
						.lines().collect(Collectors.joining("\n")),
					JsonObject.class);
				if (!json.has("type") || !json.get("type").getAsString().equals("square-textured")) {
					AxolotlClient.LOGGER.debug("Skipping "+entry+" as we currently cannot load it!");
					continue;
				}
				SkyboxManager.getInstance().addSkybox(new FSBSkyboxInstance(json));
				AxolotlClient.LOGGER.debug("Loaded FSB sky from " + entry);
			}

			for (Identifier entry : manager
				.findResources("mcpatcher/sky", this::isMCPSky)) {
				AxolotlClient.LOGGER.debug("Loading MCP sky from " + entry);
				loadMCPSky("mcpatcher", entry, manager.getResource(entry));
				AxolotlClient.LOGGER.debug("Loaded MCP sky from " + entry);
			}

			for (Identifier entry : manager
				.findResources("optifine/sky", this::isMCPSky)) {
				AxolotlClient.LOGGER.debug("Loading OF sky from " + entry);
				loadMCPSky("optifine", entry, manager.getResource(entry));
				AxolotlClient.LOGGER.debug("Loaded OF sky from " + entry);
			}

			AxolotlClient.LOGGER.debug("Finished Loading Custom Skies!");
		} catch (Exception e) {
			AxolotlClient.LOGGER.warn("Failed to load skies!", e);
		}
	}

	private boolean isMCPSky(String path) {
		return path.endsWith(".properties") && path.startsWith("sky");
	}

	private void loadMCPSky(String loader, Identifier id, Resource resource) {
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

		JsonObject object = new JsonObject();
		String string;
		String[] option;
		try {
			while ((string = reader.readLine()) != null) {
				try {
					if (!string.startsWith("#")) {
						option = string.split("=");
						if (option[0].equals("source")) {
							if (!option[1].contains(":")) {
								if (option[1].startsWith("assets")) {
									option[1] = option[1].replace("./", "").replace("assets/minecraft/", "");
								}
								if (id.getPath().contains("world")) {
									option[1] = loader + "/sky/world" + id.getPath().split("world")[1].split("/")[0]
												+ "/" + option[1].replace("./", "");
								}
							}
							try {
								MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(option[1]));
							} catch (FileNotFoundException e) {
								AxolotlClient.LOGGER.warn("Sky "+id+" does not have a valid texture attached to it: ", option[1]);
								AxolotlClient.LOGGER.warn("Please fix your packs.");
								return;
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
	public Identifier getFabricId() {
		return new Identifier("axolotlclient", "custom_skies");
	}
}
