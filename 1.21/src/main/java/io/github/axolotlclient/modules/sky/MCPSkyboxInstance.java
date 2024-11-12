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

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import org.joml.Matrix4f;

public class MCPSkyboxInstance extends SkyboxInstance {

	public MCPSkyboxInstance(JsonObject json) {
		super(json);
		this.textures[0] = Identifier.parse(json.get("source").getAsString());
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

		RenderSystem.setShaderColor(1, 1, 1, 1);

		Matrix4f dest = new Matrix4f();
		BufferBuilder bufferBuilder;
		RenderSystem.setShaderTexture(0, textures[0]);

		for (int i = 0; i < 6; ++i) {
			if (textures[0] != null) {
				matrices.push();

				float u;
				float v;

				if (i == 0) {
					u = 0;
					v = 0;
				} else if (i == 1) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(90).get(dest));
					u = 1 / 3F;
					v = 0.5F;
				} else if (i == 2) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(-90).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(180).get(dest));
					u = 2 / 3F;
					v = 0F;
				} else if (i == 3) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(180).get(dest));
					u = 1 / 3F;
					v = 0F;
				} else if (i == 4) {
					matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(90).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(-90).get(dest));
					u = 2 / 3F;
					v = 0.5F;
				} else {
					matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(-90).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(90).get(dest));
					v = 0.5F;
					u = 0;
				}

				Matrix4f matrix4f = matrices.peek().getModel();
				bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
				bufferBuilder.xyz(matrix4f, -100, -100, -100).uv0(u, v).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, -100, -100, 100).uv0(u, v + 0.5F).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, 100, -100, 100).uv0(u + 1 / 3F, v + 0.5F).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, 100, -100, -100).uv0(u + 1 / 3F, v).color(1F, 1F, 1F, alpha);
				BufferRenderer.draw(bufferBuilder.endOrThrow());

				matrices.pop();
			}
		}
	}
}
