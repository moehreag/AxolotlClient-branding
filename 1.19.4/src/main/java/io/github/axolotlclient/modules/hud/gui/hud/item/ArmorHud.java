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

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArmorHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "armorhud");

	protected final BooleanOption showProtLvl = new BooleanOption("showProtectionLevel", false);
	private final ItemStack[] placeholderStacks = new ItemStack[]{new ItemStack(Items.IRON_BOOTS),
		new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_HELMET),
		new ItemStack(Items.IRON_SWORD)};

	public ArmorHud() {
		super(20, 100, true);
	}

	@Override
	public void renderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderMainItem(matrices, client.player.getInventory().getMainHandStack(), pos.x() + 2, pos.y() + lastY);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			ItemStack stack = client.player.getInventory().getArmorStack(i).copy();
			if (showProtLvl.get() && stack.hasEnchantments()) {
				NbtList nbtList = stack.getEnchantments();
				if (nbtList != null) {
					for (int k = 0; k < nbtList.size(); ++k) {
						int enchantId = nbtList.getCompound(k).getShort("id");
						int level = nbtList.getCompound(k).getShort("lvl");
						if (enchantId == 0 && Enchantment.byRawId(enchantId) != null) {
							stack.setCount(level);
						}
					}
				}
			}
			renderItem(matrices, stack, pos.x() + 2, lastY + pos.y());
			lastY = lastY - 20;
		}
	}

	public void renderMainItem(MatrixStack matrices, ItemStack stack, int x, int y) {
		client.getItemRenderer().renderItemInGui(matrices, stack, x, y);
		String total = String.valueOf(ItemUtil.getTotal(client, stack));
		if (total.equals("1")) {
			total = null;
		}
		client.getItemRenderer().renderGuiItemDecorations(matrices, client.textRenderer, stack, x, y, total);
	}

	public void renderItem(MatrixStack matrices, ItemStack stack, int x, int y) {
		client.getItemRenderer().renderItemInGui(matrices, stack, x, y);
		client.getItemRenderer().renderGuiItemDecorations(matrices, client.textRenderer, stack, x, y);
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderItem(matrices, placeholderStacks[4], pos.x() + 2, pos.y() + lastY);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			ItemStack item = placeholderStacks[i];
			renderItem(matrices, item, pos.x() + 2, lastY + pos.y());
			lastY = lastY - 20;
		}
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(showProtLvl);
		return options;
	}
}
