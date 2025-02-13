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
import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class IPHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "iphud");
	private final BooleanOption showIcon = new BooleanOption("iphud.show_icon", false);
	private FaviconTexture icon;
	private final IntegerOption height = new IntegerOption("iphud.height", 13, 9, 64);
	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_LEFT);

	public IPHud() {
		super(115, 13, true);
		ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, minecraft) -> {
			if (icon != null) {
				icon.close();
				icon = null;
			}
		});
		ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
			if (showIcon.get()) {
				if (!minecraft.isLocalServer() && minecraft.getCurrentServer() != null) {
					icon = FaviconTexture.forServer(minecraft.getTextureManager(), minecraft.getCurrentServer().ip);
					try {
						icon.upload(NativeImage.read(minecraft.getCurrentServer().getIconBytes()));
					} catch (IOException e) {
						icon.close();
						icon = null;
					}
				}
			}
		});
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public String getValue() {
		if (client.isLocalServer() || client.getCurrentServer() == null) {
			return "Singleplayer";
		}
		return client.getCurrentServer().ip;
	}

	private void updateSize() {
		int w = getWidth();
		int h = getHeight();
		int hNew = height.get();
		boolean updated = false;
		if (h != hNew) {
			setHeight(hNew);
			updated = true;
		}
		int req = client.font.width(getValue()) + 4;
		if (showIcon.get()) {
			req += getHeight() + 1;
		}
		if (w != req) {
			setWidth(req);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(showIcon);
		options.add(height);
		options.add(anchor);
		return options;
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		updateSize();
		DrawPosition pos = getPos();
		int textX = pos.x() + getWidth() / 2;
		if (showIcon.get() && icon != null) {
			int imageSize = getHeight() - 2 + 1;
			textX += imageSize / 2;
			graphics.blit(RenderType::guiTextured, icon.textureLocation(), pos.x() + 1, pos.y() + 1, 0, 0, imageSize, imageSize, imageSize, imageSize, -1);
		}

		graphics.drawCenteredString(client.font, getValue(), textX, pos.y() + getHeight() / 2 - client.font.lineHeight / 2, textColor.get().toInt());
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		renderComponent(graphics, delta);
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
