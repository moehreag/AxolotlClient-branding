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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArmorHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "armorhud");

	protected final BooleanOption showProtLvl = new BooleanOption("showProtectionLevel", false);
	private final ItemStack[] placeholderStacks = new ItemStack[]{new ItemStack(Items.IRON_BOOTS),
		new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_HELMET),
		new ItemStack(Items.IRON_SWORD)};
	private final BooleanOption showDurabilityNumber = new BooleanOption("show_durability_num", false);
	private final BooleanOption showMaxDurabilityNumber = new BooleanOption("show_max_durability_num", false);

	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_RIGHT);

	public ArmorHud() {
		super(20, 100, true);
	}

	@Override
	public void renderComponent(float delta) {
		int width = 20;
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		int labelWidth = showDurability || showMaxDurability ? Stream.concat(Stream.of(client.player.inventory.getMainHandStack()), Arrays.stream(client.player.inventory.armorSlots))
			.filter(Objects::nonNull)
			.map(stack -> showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamage())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamage() : stack.getMaxDamage())))
			.mapToInt(text -> client.textRenderer.getWidth(text)+2).max().orElse(0) : 0;
		width += labelWidth;
		if (width != getWidth()) {
			setWidth(width);
			onBoundsUpdate();
		}
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderMainItem(client.player.inventory.getMainHandStack(), pos.x() + 2, pos.y() + lastY, labelWidth);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			if (client.player.inventory.armorSlots[i] != null) {
				ItemStack stack = client.player.inventory.armorSlots[i].copy();
				if (showProtLvl.get() && stack.hasEnchantments()) {
					NbtList nbtList = stack.getEnchantments();
					if (nbtList != null) {
						for (int k = 0; k < nbtList.size(); ++k) {
							int enchantId = nbtList.getCompound(k).getShort("id");
							int level = nbtList.getCompound(k).getShort("lvl");
							if (enchantId == 0 && Enchantment.byId(enchantId) != null) {
								stack.size = level;
							}
						}
					}
				}
				renderItem(stack, pos.x() + 2, lastY + pos.y(), labelWidth);
			}

			lastY = lastY - 20;
		}
	}

	public void renderMainItem(ItemStack stack, int x, int y, int offset) {
		renderDurabilityNumber(stack, x, y);
		x += offset;
		ItemUtil.renderGuiItemModel(stack, x, y);
		String total = String.valueOf(ItemUtil.getTotal(client, stack));
		if (total.equals("1")) {
			total = null;
		}
		ItemUtil.renderGuiItemOverlay(client.textRenderer, stack, x, y, total, textColor.get().toInt(),
			shadow.get());
	}

	public void renderItem(ItemStack stack, int x, int y, int offset) {
		renderDurabilityNumber(stack, x, y);
		x += offset;
		ItemUtil.renderGuiItemModel(stack, x, y);
		ItemUtil.renderGuiItemOverlay(client.textRenderer, stack, x, y, null, textColor.get().toInt(), shadow.get());
	}

	private void renderDurabilityNumber(ItemStack stack, int x, int y) {
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		if (stack == null || !(showMaxDurability || showDurability) || stack.getMaxDamage() == 0) {
			return;
		}
		String text = showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamage())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamage() : stack.getMaxDamage()));
		int textY = y + 10 - client.textRenderer.fontHeight/2;
		float f = (float) stack.getDamage();
		float g = (float) stack.getMaxDamage();
		float h = Math.max(0.0F, (g - f) / g);
		int j = java.awt.Color.HSBtoRGB(h / 3.0F, 1.0F, 1.0F);
		drawString(client.textRenderer, text, x, textY, (((255 << 8) + (j >> 16 & 255) << 8) + (j >> 8 & 255) << 8) + (j & 255));
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		int width = 20;
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		int labelWidth = showDurability || showMaxDurability ? Arrays.stream(placeholderStacks)
			.map(stack -> showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamage())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamage() : stack.getMaxDamage())))
			.mapToInt(text -> client.textRenderer.getWidth(text)+2).max().orElse(0) : 0;
		width += labelWidth;
		if (width != getWidth()) {
			setWidth(width);
			onBoundsUpdate();
		}
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderItem(placeholderStacks[4], pos.x() + 2, pos.y() + lastY, labelWidth);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			ItemStack item = placeholderStacks[i];
			renderItem(item, pos.x() + 2, lastY + pos.y(), labelWidth);
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
		options.add(showDurabilityNumber);
		options.add(showMaxDurabilityNumber);
		options.add(anchor);
		return options;
	}

	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
