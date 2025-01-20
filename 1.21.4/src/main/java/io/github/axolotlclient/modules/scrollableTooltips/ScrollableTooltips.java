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

package io.github.axolotlclient.modules.scrollableTooltips;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.mixin.AbstractContainerScreenAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public class ScrollableTooltips extends AbstractModule {

	@Getter
	private static final ScrollableTooltips Instance = new ScrollableTooltips();
	public final BooleanOption enabled = new BooleanOption("enabled", false);
	public final BooleanOption enableShiftHorizontalScroll = new BooleanOption("shiftHorizontalScroll", true);
	protected final IntegerOption scrollAmount = new IntegerOption("scrollAmount", 5, 1, 20);
	protected final BooleanOption inverse = new BooleanOption("inverse", false);
	private final OptionCategory category = OptionCategory.create("scrollableTooltips");
	public int tooltipOffsetX;
	public int tooltipOffsetY;

	@Override
	public void init() {
		category.add(enabled);
		category.add(enableShiftHorizontalScroll);
		category.add(scrollAmount);
		category.add(inverse);

		AxolotlClient.CONFIG.rendering.add(category);
	}

	public boolean onScroll(boolean reverse) {
		if (client.screen instanceof AbstractContainerScreen<?> screen) {
			if ((Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen)
				&& ((CreativeModeInventoryScreen) Minecraft.getInstance().screen)
				.getSelectedItemGroup() != BuiltInRegistries.CREATIVE_MODE_TAB.getValue(CreativeModeTabs.INVENTORY)) {
				return false;
			}
			Slot hovered = ((AbstractContainerScreenAccessor)screen).getHoveredSlot();
			if (hovered == null || hovered.hasItem() && hovered.getItem().is(Items.BUNDLE) && !Screen.hasControlDown()) {
				return false;
			}
			if (Screen.hasShiftDown()) {
				if (applyInverse(reverse)) {
					tooltipOffsetX -= scrollAmount.get();
				} else {
					tooltipOffsetX += scrollAmount.get();
				}
			} else {
				if (applyInverse(reverse)) {
					tooltipOffsetY -= scrollAmount.get();
				} else {
					tooltipOffsetY += scrollAmount.get();
				}
			}
			return true;
		}
		return false;
	}

	protected boolean applyInverse(boolean value) {
		return inverse.get() != value;
	}

	public void resetScroll() {
		tooltipOffsetY = tooltipOffsetX = 0;
	}
}
