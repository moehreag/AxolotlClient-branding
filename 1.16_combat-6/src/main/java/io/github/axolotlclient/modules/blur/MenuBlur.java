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
import java.nio.charset.StandardCharsets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

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
	private final ColorOption bgColor = new ColorOption("bgcolor", 0x64000000);
	private final OptionCategory category = new OptionCategory("menublur");

	private final Color black = new Color(0);

	private long openTime;

	private ShaderEffect shader;

	private int lastWidth;
	private int lastHeight;

	@Override
	public void init() {
		category.add(enabled, strength, fadeTime, bgColor);

		AxolotlClient.CONFIG.rendering.add(category);

		AxolotlClient.runtimeResources.put(shaderLocation, new MenuBlurShader());
	}

	public boolean renderScreen(MatrixStack matrices) {
		if (enabled.get() && !(MinecraftClient.getInstance().currentScreen instanceof ChatScreen) && shader != null) {
			DrawableHelper.fill(matrices, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(),
				Color.blend(black, bgColor.get(), getProgress()).getAsInt());
			return true;
		}
		return false;
	}

	private float getProgress() {
		return Math.min((System.currentTimeMillis() - openTime) / (fadeTime.get() * 1000F), 1);
	}

	public void updateBlur() {
		if (enabled.get() && MinecraftClient.getInstance().currentScreen != null && !(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)) {
			if ((shader == null || MinecraftClient.getInstance().getWindow().getWidth() != lastWidth
				|| MinecraftClient.getInstance().getWindow().getHeight() != lastHeight)
				&& MinecraftClient.getInstance().getWindow().getWidth() > 0
				&& MinecraftClient.getInstance().getWindow().getHeight() > 0) {
				try {
					shader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(),
						client.getFramebuffer(), shaderLocation);
					shader.setupDimensions(client.getWindow().getWidth(), client.getWindow().getHeight());
				} catch (IOException e) {
					AxolotlClient.LOGGER.error("Failed to load Menu Blur: ", e);
					return;
				}
			}

			if (shader != null) {
				((ShaderEffectAccessor) shader).getPasses().forEach((shader) -> {
					Uniform radius = shader.getProgram().getUniformByName("Radius");
					Uniform progress = shader.getProgram().getUniformByName("Progress");

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

			lastWidth = client.getWindow().getWidth();
			lastHeight = client.getWindow().getHeight();
			renderBlur();
		}
	}

	public void renderBlur() {
		shader.render(MinecraftClient.getInstance().getTickDelta());
		RenderSystem.enableTexture();
	}

	public void onScreenOpen() {
		openTime = System.currentTimeMillis();
	}

	private static class MenuBlurShader implements Resource {

		@Override
		public Identifier getId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
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
				+ "        }\n" + "    ]\n" + "}", StandardCharsets.UTF_8);
		}

		@Nullable
		@Override
		public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
			return null;
		}

		@Override
		public String getResourcePackName() {
			return null;
		}

		@Override
		public void close() throws IOException {

		}
	}

}
