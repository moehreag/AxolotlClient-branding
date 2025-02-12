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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * This utility allows us to separate the texture atlas parts in order to be able to make use of nine-slicing
 */
public class ButtonWidgetTextures {
	private static Identifier disabledTexture, activeTexture, hoveredTexture;

	static {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("axolotlclient", "buttonwidget/nineslicing");
			}

			@Override
			public void apply(ResourceManager manager) {
				MinecraftClient.getInstance().getTextureManager().destroyTexture(disabledTexture);
				MinecraftClient.getInstance().getTextureManager().destroyTexture(activeTexture);
				MinecraftClient.getInstance().getTextureManager().destroyTexture(hoveredTexture);
				disabledTexture = activeTexture = hoveredTexture = null;
			}
		});
	}

	private static void load() {
		if (hoveredTexture != null) {
			return;
		}
		var mc = MinecraftClient.getInstance();
		var resMan = mc.getResourceManager();
		try (var in = resMan.getResource(new Identifier("textures/gui/widgets.png")).getInputStream()) {
			BufferedImage img = ImageIO.read(in);
			disabledTexture = register(img, "disabled", 46);
			activeTexture = register(img, "active", 46 + 20);
			hoveredTexture = register(img, "hovered", 46 + 40);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Identifier register(BufferedImage atlas, String name, int imageY) throws IOException {
		var id = new Identifier("axolotlclient", "minecraft/buttonwidget/" + name);
		NativeImage img;
		try (var out = new ByteArrayOutputStream()) {
			int scale = atlas.getHeight()/256;
			ImageIO.write(atlas.getSubimage(0, imageY*scale, 200*scale, 20*scale), "png", out);
			var in = new ByteArrayInputStream(out.toByteArray());
			img = NativeImage.read(in);
			in.close();
		}
		var texture = new NativeImageBackedTexture(img);
		MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
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
