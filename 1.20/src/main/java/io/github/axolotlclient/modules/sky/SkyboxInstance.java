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

package io.github.axolotlclient.modules.sky;

import java.util.Locale;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.mixin.WorldRendererAccessor;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
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
				case "alpha" -> {
					return 0;
				}
				case "add" -> {
					return 1;
				}
				case "subtract" -> {
					return 2;
				}
				case "multiply" -> {
					return 3;
				}
				case "dodge" -> {
					return 4;
				}
				case "burn" -> {
					return 5;
				}
				case "screen" -> {
					return 6;
				}
				case "overlay" -> {
					return 7;
				}
				case "replace" -> {
					return 8;
				}
				default -> {
					AxolotlClient.LOGGER.warn("Unknown blend: " + str);
					return 1;
				}
			}
		}
	}

	public void render(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Runnable runnable) {
		float brightness = MinecraftClient.getInstance().world.getRainGradient(tickDelta);
		matrices.push();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		setupBlend(brightness);
		setupRotate(matrices, tickDelta, brightness);
		renderSkybox(matrices);
		clearBlend(brightness);
		clearRotate(matrices);
		matrices.pop();
		renderDecorations(matrices, projectionMatrix, tickDelta, runnable);
	}

	protected void setupBlend(float brightness) {
		if (manualBlend) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(blendSrcFactor, blendDstFactor);
			GL14.glBlendEquation(blendEquation);
			return;
		}

		switch (blendMode) {
			case 0 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(770, 771);
			}
			case 1 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(770, 1);
			}
			case 2 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(775, 0);
			}
			case 3 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(774, 771);
			}
			case 4 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(1, 1);
			}
			case 5 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(0, 769);
			}
			case 6 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(1, 769);
			}
			case 7 -> {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(774, 768);
			}
			case 8 -> RenderSystem.disableBlend();
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, brightness);
	}

	protected void setupRotate(MatrixStack matrices, float delta, float brightness) {
		matrices.multiply(Axis.X_POSITIVE.rotationDegrees(rotationStatic[0]));
		matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(rotationStatic[1]));
		matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(rotationStatic[2]));
		if (rotate) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, brightness);
			matrices.multiply(Axis.X_POSITIVE.rotationDegrees(rotationAxis[0]));
			matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(rotationAxis[1]));
			matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(rotationAxis[2]));
			matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(-90.0F));
			matrices.multiply(Axis.X_NEGATIVE.rotationDegrees(
				MinecraftClient.getInstance().world.getSkyAngle(delta) * 360F * rotationSpeed));
			matrices.multiply(Axis.Z_NEGATIVE.rotationDegrees(rotationAxis[0]));
			matrices.multiply(Axis.Y_NEGATIVE.rotationDegrees(rotationAxis[1]));
			matrices.multiply(Axis.X_NEGATIVE.rotationDegrees(rotationAxis[2]));
		}
	}

	public abstract void renderSkybox(MatrixStack matrices);

	protected void clearBlend(float brightness) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(770, 1);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, brightness);
	}

	protected void clearRotate(MatrixStack matrices) {
		matrices.multiply(Axis.X_NEGATIVE.rotationDegrees(rotationStatic[0]));
		matrices.multiply(Axis.Y_NEGATIVE.rotationDegrees(rotationStatic[1]));
		matrices.multiply(Axis.Z_NEGATIVE.rotationDegrees(rotationStatic[2]));
	}

	protected void renderDecorations(MatrixStack matrices, Matrix4f projectionMatrix, float delta, Runnable runnable) {
		WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) MinecraftClient
			.getInstance().worldRenderer;
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
			GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		matrices.push();
		float i = 1.0F - MinecraftClient.getInstance().world.getRainGradient(delta);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, i);
		matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(-90.0F));
		matrices.multiply(
			Axis.X_POSITIVE.rotationDegrees(MinecraftClient.getInstance().world.getSkyAngle(delta) * 360.0F));
		Matrix4f matrix4f2 = matrices.peek().getModel();
		float k = 30.0F;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBufferBuilder();

		if (showSun) {
			RenderSystem.setShaderTexture(0, SUN);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f2, -k, 100.0F, -k).uv(0.0F, 0.0F).next();
			bufferBuilder.vertex(matrix4f2, k, 100.0F, -k).uv(1.0F, 0.0F).next();
			bufferBuilder.vertex(matrix4f2, k, 100.0F, k).uv(1.0F, 1.0F).next();
			bufferBuilder.vertex(matrix4f2, -k, 100.0F, k).uv(0.0F, 1.0F).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
		}
		if (showMoon) {
			k = 20.0F;
			RenderSystem.setShaderTexture(0, MOON_PHASES);
			int r = MinecraftClient.getInstance().world.getMoonPhase();
			int s = r % 4;
			int m = r / 4 % 2;
			float t = (float) (s) / 4.0F;
			float o = (float) (m) / 2.0F;
			float p = (float) (s + 1) / 4.0F;
			float q = (float) (m + 1) / 2.0F;
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bufferBuilder.vertex(matrix4f2, -k, -100.0F, k).uv(p, q).next();
			bufferBuilder.vertex(matrix4f2, k, -100.0F, k).uv(t, q).next();
			bufferBuilder.vertex(matrix4f2, k, -100.0F, -k).uv(t, o).next();
			bufferBuilder.vertex(matrix4f2, -k, -100.0F, -k).uv(p, o).next();
			BufferRenderer.drawWithShader(bufferBuilder.end());
		}
		if (showStars) {
			float u = MinecraftClient.getInstance().world.getStarBrightness(delta) * i;
			if (u > 0.0F) {
				RenderSystem.setShaderColor(u, u, u, u);
				BackgroundRenderer.clearFog();
				worldRendererAccessor.axolotlclient$getStarsBuffer().bind();
				worldRendererAccessor.axolotlclient$getStarsBuffer().draw(matrices.peek().getModel(), projectionMatrix,
					GameRenderer.getPositionShader());
				VertexBuffer.unbind();
				runnable.run();
			}

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			matrices.pop();
		}

		RenderSystem.depthMask(true);
	}
}
