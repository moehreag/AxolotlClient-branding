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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.util.TextCollector;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ItemUpdateHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "itemupdatehud");
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
		if (client.world != null) {
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
		List<ItemUtil.ItemStorage> added = ItemUtil.compare(ItemUtil.storageFromItem(ItemUtil.getItems(client)),
			oldItems);
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
		List<ItemUtil.ItemStorage> removed = ItemUtil.compare(oldItems,
			ItemUtil.storageFromItem(ItemUtil.getItems(client)));
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
	public void renderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		int lastY = 1;
		int i = 0;
		for (ItemUtil.ItemStorage item : this.added) {
			if (i > 5) {
				return;
			}
			TextCollector message = new TextCollector();
			message.add(Text.literal("+ "));
			message.add(
				Text.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.add(Text.literal(item.times + "").setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
			message.add(
				Text.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.add(item.stack.getName());
			OrderedText text = Language.getInstance().reorder(message.getCombined());
			if (shadow.get()) {
				client.textRenderer.drawWithShadow(matrices, text, pos.x(), pos.y() + lastY,
					ClientColors.SELECTOR_GREEN.toInt());
			} else {
				client.textRenderer.draw(matrices, text, pos.x(), pos.y() + lastY, ClientColors.SELECTOR_GREEN.toInt());
			}
			lastY = lastY + client.textRenderer.fontHeight + 2;
			i++;
		}
		for (ItemUtil.ItemStorage item : this.removed) {
			if (i > 5) {
				return;
			}
			TextCollector message = new TextCollector();
			message.add(Text.literal("- "));
			message.add(
				Text.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.add(Text.literal(item.times + "").setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
			message.add(
				Text.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
			message.add(item.stack.getName());
			OrderedText text = Language.getInstance().reorder(message.getCombined());
			if (shadow.get()) {
				client.textRenderer.drawWithShadow(matrices, text, pos.x(), pos.y() + lastY,
					Formatting.RED.getColorValue());
			} else {
				client.textRenderer.draw(matrices, text, pos.x(), pos.y() + lastY, Formatting.RED.getColorValue());
			}
			lastY = lastY + client.textRenderer.fontHeight + 2;
			i++;
		}
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();
		TextCollector addM = new TextCollector();
		addM.add(Text.literal("+ "));
		addM.add(Text.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		addM.add(Text.literal("2").setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
		addM.add(Text.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		addM.add(new ItemStack(Items.DIRT).getName());
		OrderedText addText = Language.getInstance().reorder(addM.getCombined());
		if (shadow.get()) {
			client.textRenderer.drawWithShadow(matrices, addText, pos.x(), pos.y(), Formatting.RED.getColorValue());
		} else {
			client.textRenderer.draw(matrices, addText, pos.x(), pos.y() + client.textRenderer.fontHeight + 2,
				Formatting.RED.getColorValue());
		}
		TextCollector removeM = new TextCollector();
		removeM.add(Text.literal("- "));
		removeM.add(Text.literal("[").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		removeM.add(Text.literal("4").setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
		removeM.add(Text.literal("] ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ClientColors.DARK_GRAY.toInt()))));
		removeM.add(new ItemStack(Items.GRASS).getName());
		OrderedText removeText = Language.getInstance().reorder(removeM.getCombined());
		if (shadow.get()) {
			client.textRenderer.drawWithShadow(matrices, removeText, pos.x(),
				pos.y() + client.textRenderer.fontHeight + 2, Formatting.RED.getColorValue());
		} else {
			client.textRenderer.draw(matrices, removeText, pos.x(), pos.y() + client.textRenderer.fontHeight + 3,
				Formatting.RED.getColorValue());
		}
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(shadow);
		options.add(timeout);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}
}
