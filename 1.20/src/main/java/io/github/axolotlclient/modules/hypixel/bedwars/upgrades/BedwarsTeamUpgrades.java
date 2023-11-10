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

package io.github.axolotlclient.modules.hypixel.bedwars.upgrades;


import java.util.regex.Pattern;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * @author DarkKronicle
 */

public class BedwarsTeamUpgrades {

	public final TrapUpgrade trap = new TrapUpgrade();

	public final TeamUpgrade sharpness = new BinaryUpgrade(
		"sharp", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased Sharpened Swords"),
		8, 4, (graphics, x, y, width, height, upgradeLevel) -> {
		if (upgradeLevel == 0) {
			graphics.drawItem(new ItemStack(Items.STONE_SWORD), x, y);
		} else {
			graphics.drawItem(new ItemStack(Items.DIAMOND_SWORD), x, y);
		}
	}
	);

	public final TeamUpgrade dragonBuff = new BinaryUpgrade(
		"dragonbuff", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased Dragon Buff\\s*$"),
		5, 5, (graphics, x, y, width, height, purchased) -> {
		if (purchased > 0) {
			graphics.drawItem(new ItemStack(Items.END_CRYSTAL), x, y);
		}
	});

	public final TeamUpgrade healPool = new BinaryUpgrade(
		"healpool", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased Heal Pool\\s*$"),
		3, 1, (graphics, x, y, width, height, upgradeLevel) -> {
		if (upgradeLevel == 0) {
			Color color = ClientColors.DARK_GRAY;
			RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		}
		graphics.drawSprite(x, y, 0, width, height, MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(StatusEffects.HEALTH_BOOST));
	}
	);

	public final TeamUpgrade protection = new TieredUpgrade(
		"prot", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased Reinforced Armor .{1,3}\\s*$"),
		new int[]{5, 10, 20, 30}, new int[]{2, 4, 8, 16}, (graphics, x, y, width, height, upgradeLevel) -> {
		switch (upgradeLevel) {
			case 1 -> {
				graphics.drawItem(new ItemStack(Items.IRON_CHESTPLATE), x, y);
				graphics.enableScissor(x, y + height / 2, x + width / 2, y + height);
				graphics.drawItem(new ItemStack(Items.DIAMOND_CHESTPLATE), x, y);
				graphics.disableScissor();
			}
			case 2 -> {
				graphics.drawItem(new ItemStack(Items.IRON_CHESTPLATE), x, y);
				graphics.enableScissor(x, y, x + width / 2, y + height);
				graphics.drawItem(new ItemStack(Items.DIAMOND_CHESTPLATE), x, y);
				graphics.disableScissor();
			}
			case 3 -> {
				graphics.drawItem(new ItemStack(Items.DIAMOND_CHESTPLATE), x, y);
				graphics.enableScissor(x + width / 2, y + height / 2, x + width, y + height);
				graphics.drawItem(new ItemStack(Items.IRON_CHESTPLATE), x, y);
				graphics.disableScissor();
			}
			case 4 -> graphics.drawItem(new ItemStack(Items.DIAMOND_CHESTPLATE), x, y);
			default -> graphics.drawItem(new ItemStack(Items.IRON_CHESTPLATE), x, y);
		}
	}
	);

	public final TeamUpgrade maniacMiner = new TieredUpgrade(
		"haste", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased Maniac Miner .{1,3}\\s*$"),
		new int[]{2, 4}, new int[]{4, 6}, (graphics, x, y, width, height, upgradeLevel) -> {
		if (upgradeLevel == 1) {
			Color color = ClientColors.GRAY;
			RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		} else if (upgradeLevel == 0) {
			Color color = ClientColors.DARK_GRAY;
			RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
		}
		graphics.drawSprite(x, y, 0, width, height, MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(StatusEffects.HASTE));
	}
	);

	public final TeamUpgrade forge = new TieredUpgrade(
		"forge", Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased (?:Iron|Golden|Emerald|Molten) Forge\\s*$"),
		new int[]{2, 4}, new int[]{4, 6}, (graphics, x, y, width, height, upgradeLevel) -> {
		if (upgradeLevel == 0) {
			graphics.drawTexture(new Identifier("textures/block/furnace_front.png"), x, y, 0, 0, width, height, width, height);
		} else {
			if (upgradeLevel == 2) {
				Color color = Color.parse("#FFFF00");
				RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			} else if (upgradeLevel == 3) {
				Color color = Color.parse("#00FF00");
				RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			} else if (upgradeLevel == 4) {
				Color color = Color.parse("#FF0000");
				RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			}
			graphics.drawTexture(new Identifier("textures/block/furnace_front_on.png"), x, y, 0, 0, width, height, width, height);
			graphics.drawShadowedText(MinecraftClient.getInstance().textRenderer, String.valueOf(upgradeLevel), x + width - 4, y + height - 6, -1);
		}
	}
	);

	public final TeamUpgrade[] upgrades = {trap, sharpness, dragonBuff, healPool, protection, maniacMiner, forge};

	public BedwarsTeamUpgrades() {

	}

	public void onMessage(String rawMessage) {
		for (TeamUpgrade upgrade : upgrades) {
			if (upgrade.match(rawMessage)) {
				return;
			}
		}
	}

}
