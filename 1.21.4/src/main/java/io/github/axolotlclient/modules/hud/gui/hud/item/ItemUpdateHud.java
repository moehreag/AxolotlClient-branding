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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ItemUpdateHud extends TextHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "itemupdatehud");
	private final IntegerOption timeout = new IntegerOption("timeout", 6, 1, 60);
	private List<ItemUtil.ItemStorage> oldItems = new ArrayList<>();
	private ArrayList<ItemUtil.TimedItemStorage> removed;
	private ArrayList<ItemUtil.TimedItemStorage> added;

	public ItemUpdateHud() {
		super(200, 11 * 6 - 2, true);
		removed = new ArrayList<>();
		added = new ArrayList<>();
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (client.level != null) {
			update();
		}
	}

	public void update() {
		this.removed = ItemUtil.removeOld(removed, timeout.get() * 1000);
		this.added = ItemUtil.removeOld(added, timeout.get() * 1000);
		updateAdded();
		updateRemoved();
		oldItems = ItemUtil.storageFromItem(ItemUtil.getItems(client));
	}

	private void updateAdded() {
		List<ItemUtil.ItemStorage> added =
			ItemUtil.compare(ItemUtil.storageFromItem(ItemUtil.getItems(client)), oldItems);
		ArrayList<ItemUtil.TimedItemStorage> timedAdded = new ArrayList<>();
		for (ItemUtil.ItemStorage stack : added) {
			timedAdded.add(stack.timed());
		}
		for (ItemUtil.TimedItemStorage stack : timedAdded) {
			if (stack.stack.isEmpty()) {
				continue;
			}
			Optional<ItemUtil.TimedItemStorage> item = ItemUtil.getTimedItemFromItem(stack.stack, this.added);
			if (item.isPresent()) {
				item.get().incrementTimes(stack.times);
			} else {
				this.added.add(stack);
			}
		}
		this.added.sort((o1, o2) -> Float.compare(o1.getPassedTime(), o2.getPassedTime()));
	}

	private void updateRemoved() {
		List<ItemUtil.ItemStorage> removed =
			ItemUtil.compare(oldItems, ItemUtil.storageFromItem(ItemUtil.getItems(client)));
		List<ItemUtil.TimedItemStorage> timed = ItemUtil.untimedToTimed(removed);
		for (ItemUtil.TimedItemStorage stack : timed) {
			if (stack.stack.isEmpty()) {
				continue;
			}
			Optional<ItemUtil.TimedItemStorage> item = ItemUtil.getTimedItemFromItem(stack.stack, this.removed);
			if (item.isPresent()) {
				item.get().incrementTimes(stack.times);
			} else {
				this.removed.add(stack);
			}
		}
		this.removed.sort((o1, o2) -> Float.compare(o1.getPassedTime(), o2.getPassedTime()));
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		int lastY = 1;
		int i = 0;
		for (ItemUtil.ItemStorage item : this.added) {
			if (i > 5) {
				return;
			}
			ComponentCollector message = new ComponentCollector();
			message.append(Component.literal("+ "));
			message.append(Component.literal("[")
							   .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.append(Component.literal(item.times + "").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
			message.append(Component.literal("] ")
							   .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.append(item.stack.getItemName());
			FormattedCharSequence text = Language.getInstance().getVisualOrder(message.getResult());
			graphics.drawString(client.font, text, pos.x(), pos.y() + lastY, ClientColors.SELECTOR_GREEN.toInt(),
								shadow.get()
							   );

			lastY = lastY + client.font.lineHeight + 2;
			i++;
		}
		for (ItemUtil.ItemStorage item : this.removed) {
			if (i > 5) {
				return;
			}
			ComponentCollector message = new ComponentCollector();
			message.append(Component.literal("- "));
			message.append(Component.literal("[")
							   .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.append(Component.literal(item.times + "").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
			message.append(Component.literal("] ")
							   .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.append(item.stack.getItemName());
			FormattedCharSequence text = Language.getInstance().getVisualOrder(message.getResult());
			graphics.drawString(client.font, text, pos.x(), pos.y() + lastY, ChatFormatting.RED.getColor(),
								shadow.get()
							   );
			lastY = lastY + client.font.lineHeight + 2;
			i++;
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		ComponentCollector addM = new ComponentCollector();
		addM.append(Component.literal("+ "));
		addM.append(
			Component.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		addM.append(Component.literal("2").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
		addM.append(
			Component.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		addM.append(new ItemStack(Items.DIRT).getItemName());
		FormattedCharSequence addText = Language.getInstance().getVisualOrder(addM.getResult());
		graphics.drawString(client.font, addText, pos.x(), pos.y(), ChatFormatting.RED.getColor(), shadow.get());
		ComponentCollector removeM = new ComponentCollector();
		removeM.append(Component.literal("- "));
		removeM.append(
			Component.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		removeM.append(Component.literal("4").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
		removeM.append(
			Component.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		removeM.append(new ItemStack(Items.SHORT_GRASS).getItemName());
		FormattedCharSequence removeText = Language.getInstance().getVisualOrder(removeM.getResult());
		graphics.drawString(client.font, removeText, pos.x(), pos.y() + client.font.lineHeight + 2,
							ChatFormatting.RED.getColor(), shadow.get()
						   );
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(shadow);
		options.add(timeout);
		return options;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}
}
