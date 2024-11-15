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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

public class PackDisplayHud extends TextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("axolotlclient", "packdisplayhud");
	public final List<PackWidget> widgets = new ArrayList<>();
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private PackWidget placeholder;

	public PackDisplayHud() {
		super(200, 50, true);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float f) {
		DrawPosition pos = getPos();

		if (widgets.isEmpty()) init();

		if (background.get()) {
			fillRect(graphics, getBounds(), backgroundColor.get());
		}

		if (outline.get()) outlineRect(graphics, getBounds(), outlineColor.get());

		int y = pos.y() + 1;
		for (int i = widgets.size() - 1;
			 i >= 0; i--) { // Badly reverse the order (I'm sure there are better ways to do this)
			widgets.get(i).render(graphics, pos.x() + 1, y);
			y += 18;
		}
		if (y - pos.y() + 1 != getHeight()) {
			setHeight(y - pos.y() - 1);
			onBoundsUpdate();
		}
	}

	@Override
	public void init() {
		int listSize = client.getResourcePackRepository().getSelectedPacks().size();
		client.getResourcePackRepository().getSelectedPacks().forEach(profile -> {
			try (PackResources pack = profile.open()) {
				if (pack.location().title().getContents() instanceof TranslatableContents tr && tr.getKey().matches("pack\\.name\\.fabricMods?")) {
					return;
				}

				if (listSize == 1) {
					widgets.add(createWidget(profile.getTitle(), pack));
				} else if (!pack.packId().equalsIgnoreCase("vanilla")) {
					widgets.add(createWidget(profile.getTitle(), pack));
				}

			} catch (Exception ignored) {
			}
		});

		AtomicInteger w = new AtomicInteger(20);
		widgets.forEach(packWidget -> {
			int textW = client.font.width(packWidget.getName()) + 20;
			if (textW > w.get()) w.set(textW);
		});
		setWidth(w.get());

		setHeight(widgets.size() * 18);
		onBoundsUpdate();
	}

	private PackWidget createWidget(Component displayName, PackResources pack) throws IOException, AssertionError {
		IoSupplier<InputStream> supplier = pack.getRootResource("pack.png");
		assert supplier != null;
		InputStream stream = supplier.get();
		if (stream != null) {
			ResourceLocation id =
				client.getTextureManager().register(ID.getPath(), new DynamicTexture(NativeImage.read(stream)));
			stream.close();
			return new PackWidget(displayName, id);
		}
		return null;
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float f) {
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
			try (PackResources defaultPack = client.getVanillaPackResources()) {
				placeholder = createWidget(Component.literal(defaultPack.packId()), defaultPack);
			} catch (Exception ignored) {
			}
		} else {
			placeholder.render(graphics, getPos().x() + 1, getPos().y() + 1);
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(iconsOnly);
		return options;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public void update() {
		widgets.clear();
		init();
	}

	class PackWidget {

		@Getter public final String name;
		private final ResourceLocation texture;

		public PackWidget(Component name, ResourceLocation id) {
			this.name = name.getString();
			texture = id;
		}

		public void render(GuiGraphics graphics, int x, int y) {
			if (!iconsOnly.get()) {
				RenderSystem.setShaderColor(1, 1, 1, 1F);
				graphics.blit(RenderType::guiTextured, texture, x, y, 0, 0, 16, 16, 16, 16);
			}
			drawString(graphics, name, x + 18, y + 6, textColor.get().toInt(), shadow.get());
		}
	}
}
