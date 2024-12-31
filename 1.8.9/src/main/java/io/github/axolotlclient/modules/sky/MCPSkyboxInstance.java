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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resource.Identifier;

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
	public void renderSkybox() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();

		Minecraft.getInstance().getTextureManager().bind(textures[0]);
		for (int i = 0; i < 6; ++i) {
			GlStateManager.pushMatrix();

			double u;
			double v;

			if (i == 0) {
				u = 0;
				v = 0;
			} else if (i == 1) {
				GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				u = 1 / 3D;
				v = 0.5D;
			} else if (i == 2) {
				GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(180, 0, 1, 0);
				u = 2 / 3D;
				v = 0F;
			} else if (i == 3) {
				GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
				u = 1 / 3D;
				v = 0F;
			} else if (i == 4) {
				GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotatef(-90, 0, 1, 0);
				u = 2 / 3D;
				v = 0.5D;
			} else {
				GlStateManager.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotatef(90, 0, 1, 0);
				v = 0.5D;
				u = 0;
			}

			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(-100, -100, -100).texture(u, v).color(1F, 1F, 1F, alpha).nextVertex();
			bufferBuilder.vertex(-100, -100, 100).texture(u, v + 0.5).color(1F, 1F, 1F, alpha).nextVertex();
			bufferBuilder.vertex(100, -100, 100).texture(u + 1 / 3F, v + 0.5).color(1F, 1F, 1F, alpha).nextVertex();
			bufferBuilder.vertex(100, -100, -100).texture(u + 1 / 3F, v).color(1F, 1F, 1F, alpha).nextVertex();

			tessellator.end();

			GlStateManager.popMatrix();
		}
	}
}
