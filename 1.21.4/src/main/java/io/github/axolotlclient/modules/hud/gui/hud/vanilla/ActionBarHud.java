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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ActionBarHud extends TextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "actionbarhud");

	public final IntegerOption timeShown = new IntegerOption("timeshown", 60, 40, 300);
	public final BooleanOption customTextColor = new BooleanOption("customtextcolor", false);
	private final String placeholder = "Action Bar";

	public ActionBarHud() {
		super(115, 13, false);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
	}

	public void render(GuiGraphics graphics, Component actionBar, int color) {

		graphics.drawString(client.font, actionBar,
			(int) ((float) getPos().x() + Math.round((float) getWidth() / 2) -
				(float) client.font.width(actionBar) / 2), (int) ((float) getPos().y() + 3),
			customTextColor.get() ? (textColor.get().getAlpha() == 255 ? new Color(
				textColor.get().getRed(), textColor.get().getGreen(), textColor.get().getBlue(),
				ARGB.alpha(color)
			).toInt() : textColor.get().toInt()) : color, shadow.get()
		);

	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		graphics.drawString(client.font, placeholder, (int) ((float) getPos().x() + Math.round((float) getWidth() / 2) -
				(float) client.font.width(placeholder) / 2),
			(int) ((float) getPos().y() + 3), -1, false
		);
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(timeShown);
		options.add(customTextColor);
		return options;
	}
}
