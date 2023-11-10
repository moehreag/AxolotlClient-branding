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

package io.github.axolotlclient.modules.hud.gui.hud.item;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArrowHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "arrowhud");
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
	public void render(MatrixStack matrices, float delta) {
		if (dynamic.get()) {
			ClientPlayerEntity player = client.player;
			if (!(player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof RangedWeaponItem
				|| player.getStackInHand(Hand.OFF_HAND).getItem() instanceof RangedWeaponItem)) {
				return;
			}
		}
		super.render(matrices, delta);
	}

	@Override
	public void renderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		drawCenteredString(matrices, client.textRenderer, String.valueOf(arrows), pos.x() + getWidth() / 2,
			pos.y() + getHeight() - 10, textColor.get(), shadow.get());
		ItemUtil.renderGuiItemModel(getScale(), currentArrow, pos.x() + 2, pos.y() + 2);
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		drawCenteredString(matrices, client.textRenderer, "64", pos.x() + getWidth() / 2, pos.y() + getHeight() - 10,
			textColor.get(), shadow.get());
		ItemUtil.renderGuiItemModel(getScale(), arrowTypes[0], pos.x() + 2, pos.y() + 2);
	}

	@Override
	public boolean movable() {
		return true;
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
		if (!allArrowTypes.get() && !client.player.getArrowType(Items.BOW.getDefaultStack()).isEmpty()) {
			currentArrow = client.player.getArrowType(Items.BOW.getDefaultStack());
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
	public Identifier getId() {
		return ID;
	}
}
