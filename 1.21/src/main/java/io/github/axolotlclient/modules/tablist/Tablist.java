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

package io.github.axolotlclient.modules.tablist;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.network.PlayerListEntry;

public class Tablist extends AbstractModule {

	@Getter
	private static final Tablist Instance = new Tablist();
	public final BooleanOption showPlayerHeads = new BooleanOption("showPlayerHeads", true);
	public final BooleanOption showHeader = new BooleanOption("showHeader", true);
	public final BooleanOption showFooter = new BooleanOption("showFooter", true);
	public final BooleanOption alwaysShowHeadLayer = new BooleanOption("alwaysShowHeadLayer", false);
	private final BooleanOption numericalPing = new BooleanOption("numericalPing", false);
	private final ColorOption pingColor0 = new ColorOption("pingColor0", Color.parse("#FF00FFFF"));
	private final ColorOption pingColor1 = new ColorOption("pingColor1", Color.parse("#FF00FF00"));
	private final ColorOption pingColor2 = new ColorOption("pingColor2", Color.parse("#FF008800"));
	private final ColorOption pingColor3 = new ColorOption("pingColor3", Color.parse("#FFFFFF00"));
	private final ColorOption pingColor4 = new ColorOption("pingColor4", Color.parse("#FFFF8800"));
	private final ColorOption pingColor5 = new ColorOption("pingColor5", Color.parse("#FFFF0000"));
	private final BooleanOption shadow = new BooleanOption("shadow", true);
	private final OptionCategory tablist = OptionCategory.create("tablist");

	@Override
	public void init() {
		tablist.add(numericalPing, showPlayerHeads, shadow, showHeader, showFooter, alwaysShowHeadLayer);
		tablist.add(pingColor0, pingColor1, pingColor2, pingColor3, pingColor4, pingColor5);

		AxolotlClient.CONFIG.rendering.add(tablist);
	}

	public boolean renderNumericPing(GuiGraphics graphics, int width, int x, int y, PlayerListEntry entry) {
		if (numericalPing.get()) {
			Color current;
			if (entry.getLatency() < 0) {
				current = pingColor0.get();
			} else if (entry.getLatency() < 150) {
				current = pingColor1.get();
			} else if (entry.getLatency() < 300) {
				current = pingColor2.get();
			} else if (entry.getLatency() < 600) {
				current = pingColor3.get();
			} else if (entry.getLatency() < 1000) {
				current = pingColor4.get();
			} else {
				current = pingColor5.get();
			}

			DrawUtil.drawString(graphics,
				String.valueOf(entry.getLatency()),
				x + width - 1 - MinecraftClient.getInstance().textRenderer.getWidth(String.valueOf(entry.getLatency())),
				y, current, shadow.get());
			return true;
		}
		return false;
	}
}
