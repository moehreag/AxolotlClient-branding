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
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ArrowHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "arrowhud");
	private final BooleanOption dynamic = new BooleanOption("dynamic", false);
	private final ItemStack currentArrow = new ItemStack(Items.ARROW);
	private int arrows = 0;

	public ArrowHud() {
		super(20, 30, true);
	}

	@Override
	public void render(float delta) {
		if (dynamic.get()) {
			LocalClientPlayerEntity player = client.player;
			if (player == null || player.getMainHandStack() == null
				|| !(player.getMainHandStack().getItem() instanceof BowItem)) {
				return;
			}
		}
		super.render(delta);
	}

	@Override
	public void renderComponent(float delta) {
		DrawPosition pos = getPos();
		drawCenteredString(client.textRenderer, String.valueOf(arrows), pos.x() + getWidth() / 2,
			pos.y() + getHeight() - 10, textColor.get(), shadow.get());
		ItemUtil.renderGuiItemModel(currentArrow, pos.x() + 2, pos.y() + 2);
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		drawCenteredString(client.textRenderer, "64", pos.x() + getWidth() / 2, pos.y() + getHeight() - 10,
			textColor.get(), shadow.get());
		ItemUtil.renderGuiItemModel(new ItemStack(Items.ARROW), pos.x() + 2, pos.y() + 2);
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
		arrows = ItemUtil.getTotal(client, currentArrow);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(dynamic);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}
}
