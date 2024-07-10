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

package io.github.axolotlclient.modules.blur;

import java.io.IOException;
import java.io.InputStream;

import com.mojang.blaze3d.shaders.Uniform;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.PostChain;
import net.minecraft.client.resource.Resource;
import net.minecraft.client.resource.metadata.ResourceMetadataSection;
import net.minecraft.resource.Identifier;
import org.apache.commons.io.IOUtils;

/**
 * Totally not stolen from Sol.
 * License: GPL-3.0
 *
 * @author TheKodeToad
 * @author tterag1098
 */

public class MenuBlur extends AbstractModule {

	@Getter
	private static final MenuBlur Instance = new MenuBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	private final Identifier shaderLocation = new Identifier("minecraft:shaders/post/menu_blur.json");
	private final IntegerOption strength = new IntegerOption("strength", 8, 0, 100);
	private final IntegerOption fadeTime = new IntegerOption("fadeTime", 1, 0, 10);
	private final ColorOption bgColor = new ColorOption("bgcolor", new Color(0x64000000));
	private final OptionCategory category = OptionCategory.create("menublur");

	private final Color black = new Color(0);

	private long openTime;

	private PostChain shader;

	private int lastWidth;
	private int lastHeight;

	@Override
	public void init() {
		category.add(enabled, strength, fadeTime, bgColor);

		AxolotlClient.CONFIG.rendering.add(category);

		AxolotlClient.runtimeResources.put(shaderLocation, new MenuBlurShader());
	}

	public boolean renderScreen() {
		if (enabled.get() && !(Minecraft.getInstance().screen instanceof ChatScreen) && shader != null) {
			GuiElement.fill(0, 0, Util.getWindow().getWidth(), Util.getWindow().getHeight(),
				ClientColors.blend(black, bgColor.get(), getProgress()).toInt());
			return true;
		}
		return false;
	}

	private float getProgress() {
		return Math.min((System.currentTimeMillis() - openTime) / (fadeTime.get() * 1000F), 1);
	}

	public void updateBlur() {
		if (enabled.get() && Minecraft.getInstance().screen != null && !(Minecraft.getInstance().screen instanceof ChatScreen)) {
			if ((shader == null || client.width != lastWidth || client.height != lastHeight) && client.height != 0
				&& client.width != 0) {
				try {
					shader = new PostChain(client.getTextureManager(), client.getResourceManager(),
						client.getRenderTarget(), shaderLocation);
					shader.resize(client.width, client.height);
				} catch (IOException e) {
					AxolotlClient.LOGGER.error("Failed to load Menu Blur: ", e);
					return;
				}
			}

			if (shader != null) {
				((ShaderEffectAccessor) shader).getPasses().forEach((shader) -> {
					Uniform radius = shader.getEffect().getUniform("Radius");
					Uniform progress = shader.getEffect().getUniform("Progress");

					if (radius != null) {
						radius.set(strength.get());
					}

					if (progress != null) {
						if (fadeTime.get() > 0) {
							progress.set(getProgress());
						} else {
							progress.set(1);
						}
					}
				});
			}

			lastWidth = client.width;
			lastHeight = client.height;
			renderBlur();
		}
	}

	public void renderBlur() {
		shader.process(((MinecraftClientAccessor) Minecraft.getInstance()).getTicker().tickDelta);
	}

	public void onScreenOpen() {
		openTime = System.currentTimeMillis();
	}

	private static class MenuBlurShader implements Resource {

		@Override
		public Identifier getLocation() {
			return null;
		}

		@Override
		public InputStream asStream() {
			return IOUtils.toInputStream("{\n" + "    \"targets\": [\n" + "        \"swap\"\n" + "    ],\n"
				+ "    \"passes\": [\n" + "        {\n" + "            \"name\": \"menu_blur\",\n"
				+ "            \"intarget\": \"minecraft:main\",\n" + "            \"outtarget\": \"swap\",\n"
				+ "            \"uniforms\": [\n" + "                {\n"
				+ "                    \"name\": \"BlurDir\",\n" + "                    \"values\": [ 1.0, 0.0 ]\n"
				+ "                },\n" + "                {\n" + "                    \"name\": \"Radius\",\n"
				+ "                    \"values\": [ 0.0 ]\n" + "                }\n" + "            ]\n"
				+ "        },\n" + "        {\n" + "            \"name\": \"menu_blur\",\n"
				+ "            \"intarget\": \"swap\",\n" + "            \"outtarget\": \"minecraft:main\",\n"
				+ "            \"uniforms\": [\n" + "                {\n"
				+ "                    \"name\": \"BlurDir\",\n" + "                    \"values\": [ 0.0, 1.0 ]\n"
				+ "                },\n" + "                {\n" + "                    \"name\": \"Radius\",\n"
				+ "                    \"values\": [ 0.0 ]\n" + "                }\n" + "            ]\n"
				+ "        },\n" + "        {\n" + "            \"name\": \"menu_blur\",\n"
				+ "            \"intarget\": \"minecraft:main\",\n" + "            \"outtarget\": \"swap\",\n"
				+ "            \"uniforms\": [\n" + "                {\n"
				+ "                    \"name\": \"BlurDir\",\n" + "                    \"values\": [ 1.0, 0.0 ]\n"
				+ "                },\n" + "                {\n" + "                    \"name\": \"Radius\",\n"
				+ "                    \"values\": [ 0.0 ]\n" + "                }\n" + "            ]\n"
				+ "        },\n" + "        {\n" + "            \"name\": \"menu_blur\",\n"
				+ "            \"intarget\": \"swap\",\n" + "            \"outtarget\": \"minecraft:main\",\n"
				+ "            \"uniforms\": [\n" + "                {\n"
				+ "                    \"name\": \"BlurDir\",\n" + "                    \"values\": [ 0.0, 1.0 ]\n"
				+ "                },\n" + "                {\n" + "                    \"name\": \"Radius\",\n"
				+ "                    \"values\": [ 0.0 ]\n" + "                }\n" + "            ]\n"
				+ "        }\n" + "    ]\n" + "}");
		}

		@Override
		public boolean hasMetadata() {
			return false;
		}

		@Override
		public <T extends ResourceMetadataSection> T getMetadata(String key) {
			return null;
		}

		@Override
		public String getSourceName() {
			return null;
		}
	}
}
