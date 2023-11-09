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

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.shader.GlUniform;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.FloatOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;

public class MotionBlur extends AbstractModule {

	private static final MotionBlur Instance = new MotionBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final FloatOption strength = new FloatOption("strength", 50F, 1F, 99F);
	public final BooleanOption inGuis = new BooleanOption("inGuis", false);
	public final OptionCategory category = new OptionCategory("motionBlur");
	private final Identifier shaderLocation = new Identifier("minecraft:shaders/post/motion_blur.json");
	private final MinecraftClient client = MinecraftClient.getInstance();
	public ShaderEffect shader;
	private float currentBlur;

	private int lastWidth;
	private int lastHeight;

	@Override
	public void init() {
		category.add(enabled, strength, inGuis);

		AxolotlClient.CONFIG.rendering.addSubCategory(category);
		AxolotlClient.runtimeResources.put(shaderLocation, new MotionBlurShader());
	}

	public void onUpdate() {
		if ((shader == null || MinecraftClient.getInstance().getWindow().getWidth() != lastWidth
			|| MinecraftClient.getInstance().getWindow().getHeight() != lastHeight)
			&& MinecraftClient.getInstance().getWindow().getWidth() > 0
			&& MinecraftClient.getInstance().getWindow().getHeight() > 0) {
			currentBlur = getBlur();
			try {
				shader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(),
					client.getFramebuffer(), shaderLocation);
				shader.setupDimensions(MinecraftClient.getInstance().getWindow().getWidth(),
					MinecraftClient.getInstance().getWindow().getHeight());
			} catch (JsonSyntaxException | IOException e) {
				AxolotlClient.LOGGER.error("Could not load motion blur: ", e);
			}
		}
		if (currentBlur != getBlur() && shader != null) {
			((ShaderEffectAccessor) shader).axolotlclient$getPasses().forEach(shader -> {
				GlUniform blendFactor = shader.getProgram().getUniformByName("BlendFactor");
				if (blendFactor != null) {
					blendFactor.setFloat(getBlur());
				}
			});
			currentBlur = getBlur();
		}

		lastWidth = MinecraftClient.getInstance().getWindow().getWidth();
		lastHeight = MinecraftClient.getInstance().getWindow().getHeight();
	}

	private static float getBlur() {
		return MotionBlur.getInstance().strength.get() / 100F;
	}

	public static MotionBlur getInstance() {
		return Instance;
	}

	private static class MotionBlurShader extends Resource {

		public MotionBlurShader() {
			super(MinecraftClient.getInstance().getDefaultResourcePack(), () -> IOUtils.toInputStream(String.format("{" + "    \"targets\": [" + "        \"swap\","
					+ "        \"previous\"" + "    ]," + "    \"passes\": [" + "        {"
					+ "            \"name\": \"motion_blur\"," + "            \"intarget\": \"minecraft:main\","
					+ "            \"outtarget\": \"swap\"," + "            \"auxtargets\": [" + "                {"
					+ "                    \"name\": \"PrevSampler\"," + "                    \"id\": \"previous\""
					+ "                }" + "            ]," + "            \"uniforms\": [" + "                {"
					+ "                    \"name\": \"BlendFactor\"," + "                    \"values\": [ %s ]"
					+ "                }" + "            ]" + "        }," + "        {"
					+ "            \"name\": \"blit\"," + "            \"intarget\": \"swap\","
					+ "            \"outtarget\": \"previous\"" + "        }," + "        {"
					+ "            \"name\": \"blit\"," + "            \"intarget\": \"swap\","
					+ "            \"outtarget\": \"minecraft:main\"" + "        }" + "    ]" + "}", getBlur()),
				"utf-8"));
		}
	}
}
