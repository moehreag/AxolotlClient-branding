/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.Locale;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.mixin.WorldRendererAccessor;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL14;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

// TODO fix rotation & blending issues with shooting stars, implement more missing features like worlds, weather, biomes...

public abstract class SkyboxInstance {

	protected final Identifier MOON_PHASES = new Identifier("textures/environment/moon_phases.png");
	protected final Identifier SUN = new Identifier("textures/environment/sun.png");
	protected int blendMode = 1;
	// ! These are the options variables.  Do not mess with these.
	protected boolean alwaysOn;
	protected float maxAlpha = 1f;
	protected boolean manualBlend = false;
	protected int blendSrcFactor = 1;
	protected int blendDstFactor = 1;
	protected int blendEquation;
	protected boolean rotate = false;
	protected float rotationSpeed = 1F;
	protected float[] rotationStatic = new float[]{0, 0, 0};
	protected float[] rotationAxis = new float[]{0, 0, 0};
	protected boolean showSun = true;
	protected boolean showMoon = true;
	protected boolean showStars = true;
	JsonObject object;
	float alpha = 1F;
	Identifier[] textures = new Identifier[6];
	int[] fade = new int[4];

	public SkyboxInstance(JsonObject json) {
		this.object = json;
	}

	public float getAlpha() {
		if (alwaysOn) {
			return 1F;
		}

		int currentTime = (int) Objects.requireNonNull(MinecraftClient.getInstance().world).getTimeOfDay() % 24000; // modulo so that it's bound to 24000
		int durationIn = Util.getTicksBetween(fade[0], fade[1]);
		int durationOut = Util.getTicksBetween(fade[2], fade[3]);

		int startFadeIn = fade[0] % 24000;
		int endFadeIn = fade[1] % 24000;

		if (endFadeIn < startFadeIn) {
			endFadeIn += 24000;
		}

		int startFadeOut = fade[2] % 24000;
		int endFadeOut = fade[3] % 24000;

		if (startFadeOut < endFadeIn) {
			startFadeOut += 24000;
		}

		if (endFadeOut < startFadeOut) {
			endFadeOut += 24000;
		}

		int tempInTime = currentTime;

		if (tempInTime < startFadeIn) {
			tempInTime += 24000;
		}

		int tempFullTime = currentTime;

		if (tempFullTime < endFadeIn) {
			tempFullTime += 24000;
		}

		int tempOutTime = currentTime;

		if (tempOutTime < startFadeOut) {
			tempOutTime += 24000;
		}

		float maxPossibleAlpha;

		if (startFadeIn < tempInTime && endFadeIn >= tempInTime) {
			maxPossibleAlpha = 1f - (((float) (endFadeIn - tempInTime)) / durationIn); // fading in
		} else if (endFadeIn < tempFullTime && startFadeOut >= tempFullTime) {
			maxPossibleAlpha = 1f; // fully faded in
		} else if (startFadeOut < tempOutTime && endFadeOut >= tempOutTime) {
			maxPossibleAlpha = (float) (endFadeOut - tempOutTime) / durationOut; // fading out
		} else {
			maxPossibleAlpha = 0f; // default not showing
		}

		return alpha = MathHelper.clamp(maxPossibleAlpha * maxAlpha, 0, 1);
	}

	protected int parseBlend(String str) {
		if (str == null) {
			return 1;
		} else {
			switch (str.toLowerCase(Locale.ENGLISH).trim()) {
				case "alpha":
					return 0;
				case "add":
					return 1;
				case "subtract":
					return 2;
				case "multiply":
					return 3;
				case "dodge":
					return 4;
				case "burn":
					return 5;
				case "screen":
					return 6;
				case "overlay":
					return 7;
				case "replace":
					return 8;
				default:
					AxolotlClient.LOGGER.warn("Unknown blend: " + str);
					return 1;
			}
		}
	}

	public void render(MatrixStack matrices, float tickDelta) {
		float brightness = MinecraftClient.getInstance().world.getRainGradient(tickDelta);
		matrices.push();
		setupBlend(brightness);
		setupRotate(matrices, tickDelta, brightness);
		renderSkybox(matrices);
		clearBlend(brightness);
		clearRotate(matrices);
		matrices.pop();
		renderDecorations(matrices, tickDelta);
		RenderSystem.enableTexture();
	}

	protected void setupBlend(float brightness) {
		if (manualBlend) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(blendSrcFactor, blendDstFactor);
			GL14.glBlendEquation(blendEquation);
			RenderSystem.enableTexture();
			return;
		}

		switch (blendMode) {
			case 0:

				GlStateManager.enableBlend();
				GlStateManager.blendFunc(770, 771);
				break;

			case 1:

				GlStateManager.enableBlend();
				GlStateManager.blendFunc(770, 1);
				break;

			case 2:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(775, 0);
				break;

			case 3:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(774, 771);
				break;

			case 4:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(1, 1);
				break;

			case 5:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(0, 769);
				break;

			case 6:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(1, 769);
				break;

			case 7:
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(774, 768);
				break;

			case 8:
				GlStateManager.disableBlend();
				break;
		}
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, brightness);

		RenderSystem.enableTexture();
	}

	protected void setupRotate(MatrixStack matrices, float delta, float brightness) {
		matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(rotationStatic[0]));
		matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotationStatic[1]));
		matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotationStatic[2]));
		if (rotate) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, brightness);
			matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(rotationAxis[0]));
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotationAxis[1]));
			matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotationAxis[2]));
			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
			matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(
				MinecraftClient.getInstance().world.getSkyAngle(delta) * 360F * rotationSpeed));
			matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(rotationAxis[0]));
			matrices.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(rotationAxis[1]));
			matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(rotationAxis[2]));
		}
	}

	public abstract void renderSkybox(MatrixStack matrices);

	protected void clearBlend(float brightness) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(770, 1);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, brightness);
	}

	protected void clearRotate(MatrixStack matrices) {
		matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(rotationStatic[0]));
		matrices.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(rotationStatic[1]));
		matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(rotationStatic[2]));
	}

	protected void renderDecorations(MatrixStack matrices, float delta) {
		WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) MinecraftClient
			.getInstance().worldRenderer;
		RenderSystem.enableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE,
			GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

		matrices.push();
		float i = 1.0F - MinecraftClient.getInstance().world.getRainGradient(delta);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, i);
		matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
		matrices.multiply(
			Vector3f.POSITIVE_X.getDegreesQuaternion(MinecraftClient.getInstance().world.getSkyAngle(delta) * 360.0F));
		Matrix4f matrix4f2 = matrices.peek().getModel();
		float k = 30.0F;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

		if (showSun) {
			MinecraftClient.getInstance().getTextureManager().bindTexture(SUN);
			bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f2, -k, 100.0F, -k).texture(0.0F, 0.0F).next();
			bufferBuilder.vertex(matrix4f2, k, 100.0F, -k).texture(1.0F, 0.0F).next();
			bufferBuilder.vertex(matrix4f2, k, 100.0F, k).texture(1.0F, 1.0F).next();
			bufferBuilder.vertex(matrix4f2, -k, 100.0F, k).texture(0.0F, 1.0F).next();
			Tessellator.getInstance().draw();
		}
		if (showMoon) {
			k = 20.0F;
			MinecraftClient.getInstance().getTextureManager().bindTexture(MOON_PHASES);
			int r = MinecraftClient.getInstance().world.getMoonPhase();
			int s = r % 4;
			int m = r / 4 % 2;
			float t = (float) (s) / 4.0F;
			float o = (float) (m) / 2.0F;
			float p = (float) (s + 1) / 4.0F;
			float q = (float) (m + 1) / 2.0F;
			bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f2, -k, -100.0F, k).texture(p, q).next();
			bufferBuilder.vertex(matrix4f2, k, -100.0F, k).texture(t, q).next();
			bufferBuilder.vertex(matrix4f2, k, -100.0F, -k).texture(t, o).next();
			bufferBuilder.vertex(matrix4f2, -k, -100.0F, -k).texture(p, o).next();
			Tessellator.getInstance().draw();
		}
		if (showStars) {
			RenderSystem.disableTexture();
			float u = MinecraftClient.getInstance().world.getSkyAngleRadians(delta) * i;
			if (u > 0.0F) {
				RenderSystem.color4f(u, u, u, u);
				BackgroundRenderer.method_23792();
				worldRendererAccessor.getStarsBuffer().bind();
				worldRendererAccessor.getStarsBuffer().draw(matrices.peek().getModel(), 7);
				VertexBuffer.unbind();
			}

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			matrices.pop();
		}

		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
	}
}
