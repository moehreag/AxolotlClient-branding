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

import com.mojang.blaze3d.shader.GlUniform;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
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

	public boolean renderScreen(GuiGraphics graphics) {
		if (enabled.get() && !(MinecraftClient.getInstance().currentScreen instanceof ChatScreen) && shader != null) {
			graphics.fill(0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(),
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
				((ShaderEffectAccessor) shader).axolotlclient$getPasses().forEach((shader) -> {
					GlUniform radius = shader.getProgram().getUniformByName("Radius");
					GlUniform progress = shader.getProgram().getUniformByName("Progress");

					if (radius != null) {
						radius.setFloat(strength.get());
					}

					if (progress != null) {
						if (fadeTime.get() > 0) {
							progress.setFloat(getProgress());
						} else {
							progress.setFloat(1);
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
	}

	public void onScreenOpen() {
		openTime = System.currentTimeMillis();
	}

	private static class MenuBlurShader extends Resource {

		public MenuBlurShader() {
			super(MinecraftClient.getInstance().getDefaultResourcePack(), () -> IOUtils.toInputStream("""
				{
				    "targets": [
				        "swap"
				    ],
				    "passes": [
				        {
				            "name": "menu_blur",
				            "intarget": "minecraft:main",
				            "outtarget": "swap",
				            "uniforms": [
				                {
				                    "name": "BlurDir",
				                    "values": [ 1.0, 0.0 ]
				                },
				                {
				                    "name": "Radius",
				                    "values": [ 0.0 ]
				                }
				            ]
				        },
				        {
				            "name": "menu_blur",
				            "intarget": "swap",
				            "outtarget": "minecraft:main",
				            "uniforms": [
				                {
				                    "name": "BlurDir",
				                    "values": [ 0.0, 1.0 ]
				                },
				                {
				                    "name": "Radius",
				                    "values": [ 0.0 ]
				                }
				            ]
				        },
				        {
				            "name": "menu_blur",
				            "intarget": "minecraft:main",
				            "outtarget": "swap",
				            "uniforms": [
				                {
				                    "name": "BlurDir",
				                    "values": [ 1.0, 0.0 ]
				                },
				                {
				                    "name": "Radius",
				                    "values": [ 0.0 ]
				                }
				            ]
				        },
				        {
				            "name": "menu_blur",
				            "intarget": "swap",
				            "outtarget": "minecraft:main",
				            "uniforms": [
				                {
				                    "name": "BlurDir",
				                    "values": [ 0.0, 1.0 ]
				                },
				                {
				                    "name": "Radius",
				                    "values": [ 0.0 ]
				                }
				            ]
				        }
				    ]
				}""", "utf-8"));
		}
	}
}
