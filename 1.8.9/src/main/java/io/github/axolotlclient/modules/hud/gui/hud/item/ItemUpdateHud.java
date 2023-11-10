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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;

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
			if (stack.stack == null) {
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
			if (stack.stack == null) {
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
	public void renderComponent(float delta) {
		DrawPosition pos = getPos();
		int lastY = 1;
		int i = 0;
		for (ItemUtil.ItemStorage item : this.added) {
			if (i > 5) {
				GlStateManager.popMatrix();
				return;
			}
			String message = "+ " + Formatting.DARK_GRAY + "[" + Formatting.WHITE + item.times + Formatting.DARK_GRAY
				+ "] " + Formatting.RESET + item.stack.getHoverName();
			if (shadow.get()) {
				client.textRenderer.drawWithShadow(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_GREEN.toInt());
			} else {
				client.textRenderer.draw(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_GREEN.toInt());
			}
			lastY = lastY + client.textRenderer.fontHeight + 2;
			i++;
		}
		for (ItemUtil.ItemStorage item : this.removed) {
			if (i > 5) {
				GlStateManager.popMatrix();
				return;
			}
			String message = "- " + Formatting.DARK_GRAY + "[" + Formatting.WHITE + item.times + Formatting.DARK_GRAY
				+ "] " + Formatting.RESET + item.stack.getHoverName();
			if (shadow.get()) {
				client.textRenderer.drawWithShadow(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_RED.toInt());
			} else {
				client.textRenderer.draw(message, pos.x, pos.y + lastY, ClientColors.SELECTOR_RED.toInt());
			}
			lastY = lastY + client.textRenderer.fontHeight + 2;
			i++;
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		String addM = "+ " + Formatting.DARK_GRAY + "[" + Formatting.WHITE + 2 + Formatting.DARK_GRAY + "] "
			+ Formatting.RESET + new ItemStack(Blocks.DIRT).getHoverName();
		if (shadow.get()) {
			client.textRenderer.drawWithShadow(addM, pos.x + 1, pos.y + 1, ClientColors.SELECTOR_GREEN.toInt());
		} else {
			client.textRenderer.draw(addM, pos.x + 1, pos.y + 1 + client.textRenderer.fontHeight + 2,
				ClientColors.SELECTOR_GREEN.toInt());
		}
		String removeM = "- " + Formatting.DARK_GRAY + "[" + Formatting.WHITE + 4 + Formatting.DARK_GRAY + "] "
			+ Formatting.RESET + new ItemStack(Blocks.GRASS).getHoverName();
		if (shadow.get()) {
			client.textRenderer.drawWithShadow(removeM, pos.x + 1, pos.y + 1 + client.textRenderer.fontHeight + 2,
				ClientColors.SELECTOR_RED.toInt());
		} else {
			client.textRenderer.draw(removeM, pos.x + 1, pos.y + 1 + client.textRenderer.fontHeight + 3,
				ClientColors.SELECTOR_RED.toInt());
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
