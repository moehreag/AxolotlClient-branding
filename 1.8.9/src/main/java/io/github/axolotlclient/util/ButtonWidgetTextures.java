/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.resource.loader.api.ResourceLoaderEvents;

/**
 * This utility allows us to separate the texture atlas parts in order to be able to make use of nine-slicing
 */
public class ButtonWidgetTextures {
	private static Identifier disabledTexture, activeTexture, hoveredTexture;
	static {
		ResourceLoaderEvents.END_RESOURCE_RELOAD.register(() -> {
			Minecraft.getInstance().getTextureManager().close(disabledTexture);
			Minecraft.getInstance().getTextureManager().close(activeTexture);
			Minecraft.getInstance().getTextureManager().close(hoveredTexture);
			disabledTexture = activeTexture = hoveredTexture = null;
		});
	}

	private static void load(){
		if (hoveredTexture != null) {
			return;
		}
		var mc = Minecraft.getInstance();
		var resMan = mc.getResourceManager();
		try {

			BufferedImage img = TextureUtil.readImage(resMan.getResource(new Identifier("textures/gui/widgets.png")).asStream());
			disabledTexture = register(img, "disabled", 46);
			activeTexture = register(img, "active", 46 + 20);
			hoveredTexture = register(img, "hovered", 46 + 40);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Identifier register(BufferedImage atlas, String name, int imageY) {
		var id = new Identifier("axolotlclient", "minecraft/buttonwidget/" + name);
		int scale = atlas.getHeight()/256;
		var texture = new DynamicTexture(atlas.getSubimage(0, imageY * scale, 200*scale, 20*scale));
		Minecraft.getInstance().getTextureManager().register(id, texture);
		return id;
	}

	public static Identifier get(int state) {
		load();
		return switch (state) {
			case 2 -> hoveredTexture;
			case 1 -> activeTexture;
			default -> disabledTexture;
		};
	}
}
