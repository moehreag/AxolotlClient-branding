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

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class MCPSkyboxInstance extends SkyboxInstance {

	public MCPSkyboxInstance(JsonObject json) {
		super(json);
		this.textures[0] = new Identifier(json.get("source").getAsString());
		try {
			this.fade[0] = convertToTicks(json.get("startFadeIn").getAsInt());
			this.fade[1] = convertToTicks(json.get("endFadeIn").getAsInt());
			this.fade[3] = convertToTicks(json.get("endFadeOut").getAsInt());
		} catch (Exception e) {
			this.alwaysOn = true;
		}
		try {
			this.fade[2] = convertToTicks(json.get("startFadeOut").getAsInt());
		} catch (Exception ignored) {
			this.fade[2] = Util.getTicksBetween(Util.getTicksBetween(fade[0], fade[1]), fade[3]);
		}
		try {
			this.rotate = json.get("rotate").getAsBoolean();
			if (rotate) {
				this.rotationSpeed = json.get("speed").getAsFloat();
			}
		} catch (Exception e) {
			this.rotate = false;
		}
		try {
			String[] axis = json.get("axis").getAsString().split(" ");
			for (int i = 0; i < axis.length; i++) {
				this.rotationAxis[i] = Float.parseFloat(axis[i]);
			}
		} catch (Exception ignored) {
		}

		try {
			this.blendMode = parseBlend(json.get("blend").getAsString());
		} catch (Exception ignored) {
		}
		showMoon = true;
		showSun = true;
	}

	protected int convertToTicks(int hourFormat) {
		hourFormat *= 10;
		hourFormat -= 6000;
		if (hourFormat < 0) {
			hourFormat += 24000;
		}
		if (hourFormat >= 24000) {
			hourFormat -= 24000;
		}
		return hourFormat;
	}

	@Override
	public void renderSkybox(MatrixStack matrices) {
		this.alpha = getAlpha();

		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.enableTexture();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		MinecraftClient.getInstance().getTextureManager().bindTexture(textures[0]);

		for (int i = 0; i < 6; ++i) {
			if (textures[0] != null) {
				matrices.push();

				float u;
				float v;

				if (i == 0) {
					u = 0;
					v = 0;
				} else if (i == 1) {
					matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
					u = 1 / 3F;
					v = 0.5F;
				} else if (i == 2) {
					matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90));
					matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
					u = 2 / 3F;
					v = 0F;
				} else if (i == 3) {
					matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180));
					u = 1 / 3F;
					v = 0F;
				} else if (i == 4) {
					matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90));
					matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90));
					u = 2 / 3F;
					v = 0.5F;
				} else {
					matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-90));
					matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
					v = 0.5F;
					u = 0;
				}

				Matrix4f matrix4f = matrices.peek().getModel();
				bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
				bufferBuilder.vertex(matrix4f, -100, -100, -100).texture(u, v).color(1F, 1F, 1F, alpha).next();
				bufferBuilder.vertex(matrix4f, -100, -100, 100).texture(u, v + 0.5F).color(1F, 1F, 1F, alpha).next();
				bufferBuilder.vertex(matrix4f, 100, -100, 100).texture(u + 1 / 3F, v + 0.5F).color(1F, 1F, 1F, alpha).next();
				bufferBuilder.vertex(matrix4f, 100, -100, -100).texture(u + 1 / 3F, v).color(1F, 1F, 1F, alpha).next();
				tessellator.draw();

				matrices.pop();
			}
		}
	}
}
