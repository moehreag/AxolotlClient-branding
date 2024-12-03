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

package io.github.axolotlclient.modules.hud.gui.hud.item;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArrowHud extends TextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "arrowhud");
	private final BooleanOption dynamic = new BooleanOption("dynamic", false);
	private final BooleanOption allArrowTypes = new BooleanOption("allArrowTypes", false);
	private final ItemStack[] arrowTypes = new ItemStack[]{new ItemStack(Items.ARROW),
		new ItemStack(Items.TIPPED_ARROW), new ItemStack(Items.SPECTRAL_ARROW)};
	private int arrows = 0;
	private ItemStack currentArrow = arrowTypes[0];

	public ArrowHud() {
		super(20, 30, true);
	}

	@Override
	public void render(GuiGraphics graphics, float delta) {
		if (dynamic.get()) {
			LocalPlayer player = client.player;
			if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ProjectileWeaponItem
				  || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ProjectileWeaponItem)) {
				return;
			}
		}
		super.render(graphics, delta);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		graphics.renderItem(currentArrow, pos.x() + 2, pos.y() + 2);
		graphics.renderItemDecorations(client.font, currentArrow, pos.x() + 2, pos.y() + 2, String.valueOf(arrows));
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		graphics.renderItem(arrowTypes[0], pos.x() + 2, pos.y() + 2);
		graphics.renderItemDecorations(client.font, arrowTypes[0], pos.x() + 2, pos.y() + 2, "64");
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (allArrowTypes.get()) {
			arrows = ItemUtil.getTotal(client, arrowTypes[0]) + ItemUtil.getTotal(client, arrowTypes[1])
					 + ItemUtil.getTotal(client, arrowTypes[2]);
		} else {
			arrows = ItemUtil.getTotal(client, currentArrow);
		}
		if (client.player == null) {
			return;
		}
		if (!allArrowTypes.get() && !client.player.getProjectile(Items.BOW.getDefaultInstance()).isEmpty()) {
			currentArrow = client.player.getProjectile(Items.BOW.getDefaultInstance());
		} else {
			currentArrow = arrowTypes[0];
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(dynamic);
		options.add(allArrowTypes);
		return options;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}
}
