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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public class PackDisplayHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("axolotlclient", "packdisplayhud");

	private final List<PackWidget> widgets = new ArrayList<>();
	private final List<ResourcePack> packs = new ArrayList<>();
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private PackWidget placeholder;

	public PackDisplayHud() {
		super(200, 50, true);
	}

	public void setPacks(List<ResourcePack> packs) {
		widgets.clear();
		this.packs.clear();
		this.packs.addAll(packs);
	}

	@Override
	public void renderComponent(float f) {
		DrawPosition pos = getPos();

		if (widgets.isEmpty())
			init();

		int y = pos.y + 1;
		for (int i = widgets.size() - 1; i >= 0; i--) { // Badly reverse the order (I'm sure there are better ways to do this)
			widgets.get(i).render(pos.x + 1, y);
			y += 18;
		}
		if (y - pos.y + 1 != getHeight()) {
			setHeight(y - pos.y - 1);
			onBoundsUpdate();
		}
	}

	@Override
	public void init() {
		packs.forEach(pack -> {
			try {
				if (pack.getIcon() != null) {
					if (packs.size() == 1) {
						widgets.add(new PackWidget(pack));
					} else if (!pack.getName().equalsIgnoreCase("Default")) {
						widgets.add(new PackWidget(pack));
					}
				}
			} catch (Exception ignored) {
			}
		});

		AtomicInteger w = new AtomicInteger(20);
		widgets.forEach(packWidget -> {
			int textW = MinecraftClient.getInstance().textRenderer.getStringWidth(packWidget.getName()) + 20;
			if (textW > w.get())
				w.set(textW);
		});
		setWidth(w.get());

		setHeight(widgets.size() * 18);
		onBoundsUpdate();
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
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
			placeholder = new PackWidget(MinecraftClient.getInstance().getResourcePackLoader().defaultResourcePack);
		}
		placeholder.render(getPos().x + 1, getPos().y + 1);
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

	private class PackWidget {

		@Getter
		private final String name;
		private int texture;

		public PackWidget(ResourcePack pack) {
			this.name = pack.getName();
			try {
				this.texture = new NativeImageBackedTexture(pack.getIcon()).getGlId();
			} catch (Exception e) {
				AxolotlClient.LOGGER.warn("Pack " + pack.getName()
					+ " somehow threw an error! Please investigate... Does it have an icon?");
			}
		}

		public void render(int x, int y) {
			if (!iconsOnly.get()) {
				GlStateManager.color(1, 1, 1, 1F);
				GlStateManager.bindTexture(texture);
				DrawableHelper.drawTexture(x, y, 0, 0, 16, 16, 16, 16);
			}
			drawString(name, x + 18, y + 6, textColor.get().getAsInt(), shadow.get());
		}
	}
}
