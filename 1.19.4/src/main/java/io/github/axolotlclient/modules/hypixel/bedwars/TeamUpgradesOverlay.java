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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.BedwarsTeamUpgrades;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.TeamUpgrade;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.TrapUpgrade;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * @author DarkKronicle
 */

public class TeamUpgradesOverlay extends BoxHudEntry {

	public final static Identifier ID = new Identifier("axolotlclient", "bedwars_teamupgrades");

	private final BooleanOption renderWhenRelevant = new BooleanOption(ID.getPath() + ".renderWhenRelevant", true);

	private BedwarsTeamUpgrades upgrades = null;
	private final BedwarsMod mod;
	private final static TrapUpgrade.TrapType[] trapEdit = {TrapUpgrade.TrapType.MINER_FATIGUE, TrapUpgrade.TrapType.ITS_A_TRAP};

	public TeamUpgradesOverlay(BedwarsMod mod) {
		super(60, 40, true);
		this.mod = mod;
	}

	public void onStart(BedwarsTeamUpgrades newUpgrades) {
		upgrades = newUpgrades;
	}

	public void onEnd() {
		upgrades = null;
	}

	@Override
	public void render(MatrixStack matrices, float delta) {
		if (!renderWhenRelevant.get() || mod.inGame()) {
			super.render(matrices, delta);
		}
	}

	public void drawOverlay(MatrixStack stack, DrawPosition position, boolean editMode) {
		if (upgrades == null && !editMode) {
			return;
		}

		int x = position.x() + 1;
		int y = position.y() + 2;
		int width = getWidth();
		int height = getHeight();
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
		boolean normalUpgrades = false;
		if (upgrades != null) {
			for (TeamUpgrade u : upgrades.upgrades) {
				if (!u.isPurchased()) {
					continue;
				}
				if (u instanceof TrapUpgrade) {
					continue;
				}
				RenderSystem.setShaderColor(1, 1, 1, 1);
				u.draw(stack, x, y, 16, 16);
				x += 17;
				normalUpgrades = true;
			}
			setWidth(Math.max((x - position.x()) + 1, 18));
		}
		x = position.x() + 1;
		if (normalUpgrades) {
			y += 17;
		}
		if (editMode) {
			for (TrapUpgrade.TrapType type : trapEdit) {
				RenderSystem.setShaderColor(1, 1, 1, 1);
				type.draw(stack, x, y, 16, 16);
				x += 17;
			}
			setWidth(Math.max((x - position.x()) + 1, 18));
		} else {
			upgrades.trap.draw(stack, x, y, 16, 16);
			setWidth(Math.max(((x + (upgrades.trap.getTrapCount() * 16)) - position.x()) + 1, getWidth()));
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		setHeight((y - position.y()) + 19);
		if (getHeight() != height || getWidth() != width) {
			onBoundsUpdate();
		}
	}

	@Override
	public void renderComponent(MatrixStack stack, float delta) {
		drawOverlay(stack, getPos(), false);
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack stack, float delta) {
		drawOverlay(stack, getPos(), true);
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
