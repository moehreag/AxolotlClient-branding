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

package io.github.axolotlclient.modules.blur;

import java.io.IOException;
import java.io.InputStream;

import com.google.gson.JsonSyntaxException;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.Uniform;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

public class MotionBlur extends AbstractModule {

	private static final MotionBlur Instance = new MotionBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final FloatOption strength = new FloatOption("strength", 50F, 1F, 99F);
	public final BooleanOption inGuis = new BooleanOption("inGuis", false);
	public final OptionCategory category = OptionCategory.create("motionBlur");
	private final Identifier shaderLocation = new Identifier("minecraft:shaders/post/motion_blur.json");
	private final MinecraftClient client = MinecraftClient.getInstance();
	public ShaderEffect shader;
	private float currentBlur;

	private int lastWidth;
	private int lastHeight;

	private static float getBlur() {
		return MotionBlur.getInstance().strength.get() / 100F;
	}

	public static MotionBlur getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled, strength, inGuis);

		AxolotlClient.CONFIG.rendering.add(category);
		AxolotlClient.runtimeResources.put(shaderLocation, new MotionBlurShader());
	}

	public void onUpdate() {
		if ((shader == null || MinecraftClient.getInstance().getFramebuffer().textureWidth != lastWidth
			|| MinecraftClient.getInstance().getWindow().getHeight() != lastHeight)
			&& MinecraftClient.getInstance().getFramebuffer().textureWidth > 0
			&& MinecraftClient.getInstance().getWindow().getHeight() > 0) {
			currentBlur = getBlur();
			try {
				shader = new ShaderEffect(client.getTextureManager(), client.getResourceManager(),
					client.getFramebuffer(), shaderLocation);
				shader.setupDimensions(MinecraftClient.getInstance().getFramebuffer().textureWidth,
					MinecraftClient.getInstance().getWindow().getHeight());
			} catch (JsonSyntaxException | IOException e) {
				AxolotlClient.LOGGER.error("Could not load motion blur: ", e);
			}
		}
		if (currentBlur != getBlur() && shader != null) {
			((ShaderEffectAccessor) shader).getPasses().forEach(shader -> {
				Uniform blendFactor = shader.getProgram().getUniformByName("BlendFactor");
				if (blendFactor != null) {
					blendFactor.set(getBlur());
				}
			});
			currentBlur = getBlur();
		}

		lastWidth = MinecraftClient.getInstance().getFramebuffer().textureWidth;
		lastHeight = MinecraftClient.getInstance().getWindow().getHeight();
	}

	private static class MotionBlurShader implements Resource {

		@Override
		public Identifier getId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			try {
				return IOUtils.toInputStream(String.format("{" + "    \"targets\": [" + "        \"swap\","
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
					+ "            \"outtarget\": \"minecraft:main\"" + "        }" + "    ]" + "}", getBlur()), "utf-8");
			} catch (IOException e) {
				return null;
			}
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
