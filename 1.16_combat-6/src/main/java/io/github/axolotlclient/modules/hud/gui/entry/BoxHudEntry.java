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

package io.github.axolotlclient.modules.hud.gui.entry;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.util.math.MatrixStack;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public abstract class BoxHudEntry extends AbstractHudEntry {

	private final boolean backgroundAllowed;

	protected BooleanOption background = new BooleanOption("background", true);
	protected ColorOption backgroundColor = new ColorOption("bgcolor", new Color(0x64000000));

	protected BooleanOption outline = new BooleanOption("outline", false);
	protected ColorOption outlineColor = new ColorOption("outlinecolor", ClientColors.WHITE);

	public BoxHudEntry(int width, int height, boolean backgroundAllowed) {
		super(width, height);
		this.backgroundAllowed = backgroundAllowed;
		if (!backgroundAllowed) {
			background = null;
			backgroundColor = null;
			outline = null;
			outlineColor = null;
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		if (backgroundAllowed) {
			options.add(background);
			options.add(backgroundColor);
			options.add(outline);
			options.add(outlineColor);
		}
		return options;
	}

	@Override
	public void render(MatrixStack matrices, float delta) {
		matrices.push();
		scale(matrices);
		if (backgroundAllowed) {
			if (background.get() && backgroundColor.get().getAlpha() > 0) {
				fillRect(matrices, getBounds(), backgroundColor.get());
			}
			if (outline.get() && outlineColor.get().getAlpha() > 0) {
				outlineRect(matrices, getBounds(), outlineColor.get());
			}
		}
		renderComponent(matrices, delta);
		matrices.pop();
	}

	public abstract void renderComponent(MatrixStack matrices, float delta);

	@Override
	public void renderPlaceholder(MatrixStack matrices, float delta) {
		matrices.push();
		renderPlaceholderBackground(matrices);
		scale(matrices);
		renderPlaceholderComponent(matrices, delta);
		matrices.pop();
		hovered = false;
	}

	public abstract void renderPlaceholderComponent(MatrixStack matrices, float delta);
}
