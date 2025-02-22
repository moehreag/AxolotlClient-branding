/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ResourceOverlay extends BoxHudEntry {

	public final static Identifier ID = new Identifier("axolotlclient", "bedwars_resources");
	private final BooleanOption renderWhenRelevant = new BooleanOption(ID.getPath() + ".renderWhenRelevant", true);
	private static final List<Item> RESOURCES = List.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.EMERALD);
	private static final Map<Item, Integer> PLACEHOLDER = Map.of(
		Items.IRON_INGOT, 3,
		Items.GOLD_INGOT, 43,
		Items.DIAMOND, 5,
		Items.EMERALD, 13
	);
	private final BedwarsMod mod;

	public ResourceOverlay(BedwarsMod mod) {
		super(4 * 18 + 1, 18 + 1, true);
		this.mod = mod;
	}

	@Override
	public void render(MatrixStack graphics, float delta) {
		if (!renderWhenRelevant.get() || mod.inGame()) {
			super.render(graphics, delta);
		}
	}

	@Override
	public void renderComponent(MatrixStack graphics, float delta) {
		draw(graphics, s -> ItemUtil.getTotal(client, s));
	}

	private void draw(MatrixStack graphics, Function<ItemStack, Integer> countFunction) {
		var pos = getPos();
		int x = pos.x() + 1;
		int y = pos.y() + 1;
		for (Item item : RESOURCES) {
			var stack = item.getDefaultStack();
			int amount = countFunction.apply(stack);
			if (amount > 0) {
				ItemUtil.renderGuiItemModel(getScale(), stack, x, y);
				ItemUtil.renderGuiItemOverlay(graphics, client.textRenderer, stack, x, y, String.valueOf(amount), -1, true);
				x += 18;
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack graphics, float delta) {
		draw(graphics, s -> PLACEHOLDER.get(s.getItem()));
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(renderWhenRelevant);
		return options;
	}
}
