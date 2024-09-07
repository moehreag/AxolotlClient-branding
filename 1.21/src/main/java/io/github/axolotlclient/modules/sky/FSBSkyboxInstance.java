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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import org.joml.Matrix4f;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

public class FSBSkyboxInstance extends SkyboxInstance {

	public FSBSkyboxInstance(JsonObject json) {
		super(json);
		JsonObject props = json.get("properties").getAsJsonObject();
		JsonObject textures = json.get("textures").getAsJsonObject();
		this.textures[0] = Identifier.parse(textures.get("bottom").getAsString());
		this.textures[1] = Identifier.parse(textures.get("north").getAsString());
		this.textures[2] = Identifier.parse(textures.get("south").getAsString());
		this.textures[3] = Identifier.parse(textures.get("top").getAsString());
		this.textures[4] = Identifier.parse(textures.get("east").getAsString());
		this.textures[5] = Identifier.parse(textures.get("west").getAsString());
		try {
			this.fade[0] = props.get("fade").getAsJsonObject().get("startFadeIn").getAsInt();
			this.fade[1] = props.get("fade").getAsJsonObject().get("endFadeIn").getAsInt();
			this.fade[2] = props.get("fade").getAsJsonObject().get("startFadeOut").getAsInt();
			this.fade[3] = props.get("fade").getAsJsonObject().get("endFadeOut").getAsInt();
		} catch (Exception e) {
			alwaysOn = true;
		}
		try {
			JsonObject rotation = props.get("rotation").getAsJsonObject();
			this.rotate = props.get("shouldRotate").getAsBoolean();
			this.rotationSpeed = rotation.get("rotationSpeed").getAsFloat();
			JsonArray axis = rotation.get("axis").getAsJsonArray();
			for (int i = 0; i < axis.size(); i++) {
				this.rotationAxis[i] = axis.get(i).getAsFloat();
			}
			JsonArray staticRotation = rotation.get("static").getAsJsonArray();
			for (int i = 0; i < staticRotation.size(); i++) {
				this.rotationStatic[i] = staticRotation.get(i).getAsFloat();
			}
		} catch (Exception ignored) {
		}

		try {
			JsonObject jsonBlend = json.get("blend").getAsJsonObject();
			this.blendMode = parseBlend(jsonBlend.get("type").getAsString());
		} catch (Exception ignored) {
			try {
				AxolotlClient.LOGGER.debug(textures + ": Using manual blend!");
				JsonObject blend = json.get("blend").getAsJsonObject();
				this.blendEquation = blend.get("equation").getAsInt();
				this.blendDstFactor = blend.get("dfactor").getAsInt();
				this.blendSrcFactor = blend.get("sfactor").getAsInt();
				this.manualBlend = true;
			} catch (Exception e) {
				AxolotlClient.LOGGER.debug(textures + ": Manual Blend failed, using fallback blend!");
				manualBlend = false;
				blendMode = 8;
			}
		}

		try {
			JsonObject decorations = json.get("decorations").getAsJsonObject();
			this.showMoon = decorations.get("showMoon").getAsBoolean();
			this.showSun = decorations.get("showSun").getAsBoolean();
			this.showStars = decorations.get("showStars").getAsBoolean();
		} catch (Exception ignored) {
		}
	}

	@Override
	public void renderSkybox(MatrixStack matrices) {
		this.alpha = getAlpha();

		RenderSystem.setShaderColor(1, 1, 1, 1);

		Matrix4f dest = new Matrix4f();
		BufferBuilder bufferBuilder;

		for (int i = 0; i < 6; ++i) {
			// 0 = bottom
			// 1 = north
			// 2 = south
			// 3 = top
			// 4 = east
			// 5 = west

			if (textures[i] != null) {
				RenderSystem.setShaderTexture(0, textures[i]);
				matrices.push();

				if (i == 1) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(90.0F).get(dest));
				} else if (i == 2) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(-90.0F).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(180.0F).get(dest));
				} else if (i == 3) {
					matrices.multiply(Axis.X_POSITIVE.rotationDegrees(180.0F).get(dest));
				} else if (i == 4) {
					matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(90.0F).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(-90.0F).get(dest));
				} else if (i == 5) {
					matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(-90.0F).get(dest));
					matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(90.0F).get(dest));
				}
				Matrix4f matrix4f = matrices.peek().getModel();
				bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
				bufferBuilder.xyz(matrix4f, -100, -100, -100).uv0(0F, 0F).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, -100, -100, 100).uv0(0F, 1F).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, 100, -100, 100).uv0(1F, 1F).color(1F, 1F, 1F, alpha);
				bufferBuilder.xyz(matrix4f, 100, -100, -100).uv0(1F, 0F).color(1F, 1F, 1F, alpha);
				BufferRenderer.drawWithShader(bufferBuilder.endOrThrow());

				matrices.pop();
			}
		}
	}
}
