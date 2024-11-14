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

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.shaders.Uniform;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.FloatOption;
import io.github.axolotlclient.mixin.ShaderEffectAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class MotionBlur extends AbstractModule {

	private static final MotionBlur Instance = new MotionBlur();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final FloatOption strength = new FloatOption("strength", 50F, 1F, 99F);
	public final BooleanOption inGuis = new BooleanOption("inGuis", false);
	public final OptionCategory category = OptionCategory.create("motionBlur");
	private final ResourceLocation postChainId = ResourceLocation.withDefaultNamespace("motion_blur");
	private final Minecraft client = Minecraft.getInstance();
	public PostChain shader;
	private float currentBlur;

	private int lastWidth;
	private int lastHeight;

	private static float getBlur() {
		return getInstance().strength.get() / 100F;
	}

	public static MotionBlur getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled, strength, inGuis);

		AxolotlClient.CONFIG.rendering.add(category);
	}

	public void onUpdate() {
		if ((shader == null || client.getMainRenderTarget().viewWidth != lastWidth ||
			client.getMainRenderTarget().viewHeight != lastHeight) && client.getMainRenderTarget().viewWidth > 0 &&
			client.getMainRenderTarget().viewHeight > 0) {
			currentBlur = getBlur();
			try {
				shader = client.getShaderManager().getPostChain(postChainId, LevelTargetBundle.MAIN_TARGETS);
			} catch (JsonSyntaxException e) {
				AxolotlClient.LOGGER.error("Could not load motion blur: ", e);
			}
		}
		if (currentBlur != getBlur() && shader != null) {
			((ShaderEffectAccessor) shader).axolotlclient$getPasses().forEach(shader -> {
				Uniform blendFactor = shader.getShader().getUniform("BlendFactor");
				if (blendFactor != null) {
					blendFactor.set(getBlur());
				}
			});
			currentBlur = getBlur();
		}

		lastWidth = client.getMainRenderTarget().viewWidth;
		lastHeight = client.getMainRenderTarget().viewHeight;
	}
}
