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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PackDisplayHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("axolotlclient", "packdisplayhud");
	public final List<PackWidget> widgets = new ArrayList<>();
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private PackWidget placeholder;

	public PackDisplayHud() {
		super(200, 50, true);
	}

	@Override
	public void renderComponent(MatrixStack matrices, float f) {
		DrawPosition pos = getPos();

		if (widgets.isEmpty())
			init();

		if (background.get()) {
			fillRect(matrices, getBounds(), backgroundColor.get());
		}

		if (outline.get())
			outlineRect(matrices, getBounds(), outlineColor.get());

		int y = pos.y + 1;
		for (int i = widgets.size() - 1; i >= 0; i--) { // Badly reverse the order (I'm sure there are better ways to do this)
			widgets.get(i).render(matrices, pos.x + 1, y);
			y += 18;
		}
		if (y - pos.y + 1 != getHeight()) {
			setHeight(y - pos.y - 1);
			onBoundsUpdate();
		}
	}

	@Override
	public void init() {
		int listSize = client.getResourcePackManager().getProfiles().size();
		MinecraftClient.getInstance().getResourcePackManager().getEnabledProfiles().forEach(profile -> {
			try (ResourcePack pack = profile.createResourcePack()) {

				if (listSize == 1) {
					widgets.add(createWidget(profile.getDisplayName(), pack));
				} else if (!pack.getName().equalsIgnoreCase("vanilla")) {
					widgets.add(createWidget(profile.getDisplayName(), pack));
				}

			} catch (Exception ignored) {
			}
		});

		AtomicInteger w = new AtomicInteger(20);
		widgets.forEach(packWidget -> {
			int textW = MinecraftClient.getInstance().textRenderer.getWidth(packWidget.getName()) + 20;
			if (textW > w.get())
				w.set(textW);
		});
		setWidth(w.get());

		setHeight(widgets.size() * 18);
		onBoundsUpdate();
	}

	private PackWidget createWidget(Text displayName, ResourcePack pack) throws IOException, AssertionError {
		ResourceIoSupplier<InputStream> supplier = pack.openRoot("pack.png");
		assert supplier != null;
		InputStream stream = supplier.get();
		if (stream != null) {
			int texture = new NativeImageBackedTexture(NativeImage.read(stream)).getGlId();
			stream.close();
			return new PackWidget(displayName, texture);
		}
		return null;
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float f) {
		boolean updateBounds = false;
		if (getHeight() < 18) {
			setHeight(18);
			updateBounds = true;
		}
		if (getWidth() < 56) {
			setWidth(56);
			updateBounds = true;
		}
		if (updateBounds) {
			onBoundsUpdate();
		}
		if (placeholder == null) {
			try (ResourcePack defaultPack = MinecraftClient.getInstance().m_iuvifafs()) {
				placeholder = createWidget(defaultPack.getDisplayName(), defaultPack);
			} catch (Exception ignored) {
			}
		} else {
			placeholder.render(matrices, getPos().x + 1, getPos().y + 1);
		}
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(iconsOnly);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public void update() {
		widgets.clear();
		init();
	}

	private class PackWidget {

		@Getter
		public final String name;
		private final int texture;

		public PackWidget(Text name, int textureId) {
			this.name = name.getString();
			texture = textureId;
		}

		public void render(MatrixStack matrices, int x, int y) {
			if (!iconsOnly.get()) {
				RenderSystem.setShaderColor(1, 1, 1, 1F);
				RenderSystem.setShaderTexture(0, texture);
				DrawableHelper.drawTexture(matrices, x, y, 0, 0, 16, 16, 16, 16);
			}
			drawString(matrices, name, x + 18, y + 6, textColor.get().getAsInt(), shadow.get());
		}
	}
}
