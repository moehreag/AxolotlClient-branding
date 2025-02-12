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
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArmorHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "armorhud");

	protected final BooleanOption showProtLvl = new BooleanOption("showProtectionLevel", false);
	private final ItemStack[] placeholderStacks =
		new ItemStack[]{new ItemStack(Items.IRON_BOOTS), new ItemStack(Items.IRON_LEGGINGS),
			new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_SWORD)};
	private final BooleanOption showDurabilityNumber = new BooleanOption("show_durability_num", false);
	private final BooleanOption showMaxDurabilityNumber = new BooleanOption("show_max_durability_num", false);

	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_RIGHT);

	public ArmorHud() {
		super(20, 100, true);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		int width = 20;
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		int labelWidth = showDurability || showMaxDurability ? Stream.concat(Stream.of(client.player.getInventory().getSelected()), client.player.getInventory().armor.stream())
			.map(stack -> showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamageValue())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamageValue() : stack.getMaxDamage())))
			.mapToInt(text -> client.font.width(text)+2).max().orElse(0) : 0;
		width += labelWidth;
		if (width != getWidth()) {
			setWidth(width);
			onBoundsUpdate();
		}
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderMainItem(graphics, client.player.getInventory().getSelected(), pos.x() + 2, pos.y() + lastY, labelWidth);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			ItemStack stack = client.player.getInventory().getArmor(i).copy();
			if (showProtLvl.get() && stack.isEnchanted()) {
				client.level.registryAccess().lookup(Registries.ENCHANTMENT).orElseThrow().get(Enchantments.PROTECTION)
					.ifPresent(enchantmentReference -> stack.setCount(
						EnchantmentHelper.getItemEnchantmentLevel(enchantmentReference, stack)));
			}
			renderItem(graphics, stack, pos.x() + 2, lastY + pos.y(), labelWidth);
			lastY = lastY - 20;
		}
	}

	public void renderMainItem(GuiGraphics graphics, ItemStack stack, int x, int y, int offset) {
		renderDurabilityNumber(graphics, stack, x, y);
		x += offset;
		String total = String.valueOf(ItemUtil.getTotal(client, stack));
		if (total.equals("1")) {
			total = null;
		}
		graphics.renderItem(stack, x, y);
		graphics.renderItemDecorations(client.font, stack, x, y, total);
	}

	public void renderItem(GuiGraphics graphics, ItemStack stack, int x, int y, int offset) {
		renderDurabilityNumber(graphics, stack, x, y);
		x += offset;
		graphics.renderItem(stack, x, y);
		graphics.renderItemDecorations(client.font, stack, x, y);
	}

	private void renderDurabilityNumber(GuiGraphics graphics, ItemStack stack, int x, int y) {
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		if (!(showMaxDurability || showDurability) || stack.isEmpty() || stack.getMaxDamage() == 0) {
			return;
		}
		String text = showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamageValue())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamageValue() : stack.getMaxDamage()));;
		int textY = y + 10 - client.font.lineHeight/2;
		graphics.drawString(client.font, text, x, textY, stack.getBarColor());
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		int width = 20;
		boolean showDurability = showDurabilityNumber.get();
		boolean showMaxDurability = showMaxDurabilityNumber.get();
		int labelWidth = showDurability || showMaxDurability ? Arrays.stream(placeholderStacks)
			.map(stack -> showDurability && showMaxDurability ? (stack.getMaxDamage() - stack.getDamageValue())+"/"+stack.getMaxDamage() : String.valueOf((showDurability ? stack.getMaxDamage() - stack.getDamageValue() : stack.getMaxDamage())))
			.mapToInt(text -> client.font.width(text)+2).max().orElse(0) : 0;
		width += labelWidth;
		if (width != getWidth()) {
			setWidth(width);
			onBoundsUpdate();
		}
		DrawPosition pos = getPos();
		int lastY = 2 + (4 * 20);
		renderItem(graphics, placeholderStacks[4], pos.x() + 2, pos.y() + lastY, labelWidth);
		lastY = lastY - 20;
		for (int i = 0; i <= 3; i++) {
			ItemStack item = placeholderStacks[i];
			renderItem(graphics, item, pos.x() + 2, lastY + pos.y(), labelWidth);
			lastY = lastY - 20;
		}
	}

	@Override
	public ResourceLocation getId() {
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
