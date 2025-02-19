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

import java.util.Base64;
import java.util.List;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.GraphicsImpl;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.Util;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class IPHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "iphud");
	private final BooleanOption showIcon = new BooleanOption("iphud.show_icon", false);
	private Identifier icon;
	private final IntegerOption height = new IntegerOption("iphud.height", 13, 9, 64);
	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_LEFT);

	@SuppressWarnings("UnstableApiUsage")
	public IPHud() {
		super(115, 13, true);
		ClientConnectionEvents.DISCONNECT.register((minecraft) -> {
			if (icon != null) {
				minecraft.getTextureManager().close(icon);
				icon = null;
			}
		});
		ClientConnectionEvents.LOGIN.register((minecraft) -> {
			if (showIcon.get()) {
				if (!minecraft.isInSingleplayer() && minecraft.getCurrentServerEntry() != null) {
					try {
						var graphics = new GraphicsImpl(0, 0);
						graphics.setPixelData(Base64.getDecoder().decode(minecraft.getCurrentServerEntry().getIcon()));
						icon = Util.getTexture(graphics, "servers/" + Hashing.sha1().hashUnencodedChars(minecraft.getCurrentServerEntry().address) + "/icon");
					} catch (Exception e) {
						if (icon != null) {
							minecraft.getTextureManager().close(icon);
							icon = null;
						}
					}
				}
			}
		});
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
		int req = client.textRenderer.getWidth(getValue()) + 4;
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
	public Identifier getId() {
		return ID;
	}

	public String getValue() {
		if (client.isInSingleplayer() || client.getCurrentServerEntry() == null) {
			return "Singleplayer";
		}
		return client.getCurrentServerEntry().address;
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
	public void renderComponent(float delta) {
		updateSize();
		DrawPosition pos = getPos();
		int textX = pos.x() + getWidth() / 2 + 1;
		if (showIcon.get() && icon != null) {
			int imageSize = getHeight() - 2;
			textX += imageSize / 2;
			client.getTextureManager().bind(icon);
			GlStateManager.color4f(1, 1, 1, 1);
			drawTexture(pos.x() + 1, pos.y() + 1, 0, 0, imageSize, imageSize, imageSize, imageSize);
		}

		drawCenteredString(client.textRenderer, getValue(), textX, pos.y() + getHeight() / 2 - client.textRenderer.fontHeight / 2, textColor.get().toInt(), shadow.get());
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		renderComponent(delta);
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
