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

package io.github.axolotlclient.modules.hypixel.bedwars.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMode;
import io.github.axolotlclient.util.ClientColors;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Identifier;

/**
 * @author DarkKronicle
 */

public class TrapUpgrade extends TeamUpgrade {

	private final static Pattern[] REGEX = {
		Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased (.+) Trap.?\\s*$"),
		Pattern.compile("Trap was set (off)!"),
		Pattern.compile("Removed (.+) Trap from the (queue)!\\s*$")
	};

	private final List<TrapType> traps = new ArrayList<>(3);

	public TrapUpgrade() {
		super("trap", REGEX);
	}

	@Override
	protected void onMatch(TeamUpgrade upgrade, Matcher matcher) {
		if (matcher.group(1).equals("off")) {
			// Trap went off
			traps.remove(0);
			return;
		}
		TrapType type = TrapType.getFuzzy(matcher.group(1));
		if (matcher.groupCount() >= 2 && matcher.group(2).equals("queue")) {
			traps.remove(type);
			return;
		}
		traps.add(type);
	}

	public boolean canPurchase() {
		return traps.size() < 3;
	}

	@Override
	public int getPrice(BedwarsMode mode) {
		return switch (traps.size()) {
			case 0 -> 1;
			case 1 -> 2;
			case 2 -> 4;
			default -> 0;
		};
	}

	@Override
	public boolean isPurchased() {
		return !traps.isEmpty();
	}

	@Override
	public void draw(int x, int y, int width, int height) {
		if (traps.isEmpty()) {
			Color color = ClientColors.DARK_GRAY;
			GlStateManager.color4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
			Minecraft.getInstance().getTextureManager().bind(new Identifier("textures/items/barrier.png"));
			GuiElement.drawTexture(x, y, 0, 0, 16, 16, 16, 16);
		} else {
			for (TrapType type : traps) {
				GlStateManager.color4f(1, 1, 1, 1);
				type.draw(x, y, width, height);
				x += width + 1;
			}
		}
	}

	public int getTrapCount() {
		return traps.size();
	}

	@Override
	public boolean isMultiUpgrade() {
		return true;
	}

	@AllArgsConstructor
	public enum TrapType {

		ITS_A_TRAP((x, y, width, height, unused) -> {
			Minecraft.getInstance().getTextureManager().bind(new Identifier("textures/gui/container/inventory.png"));
			GuiElement.drawTexture(x, y, 5 * 18, 198 + 18, 18, 18, 16, 16, 256, 256);
		}),
		COUNTER_OFFENSIVE((x, y, width, height, unused) -> {
			Minecraft.getInstance().getTextureManager().bind(new Identifier("textures/gui/container/inventory.png"));
			GuiElement.drawTexture(x, y, 0, 198, 18, 18, 16, 16, 256, 256);
		}),
		ALARM((x, y, width, height, unused) ->
			ItemUtil.renderGuiItemModel(new ItemStack(Items.ENDER_EYE), x, y)),
		MINER_FATIGUE((x, y, width, height, unused) -> {
			Minecraft.getInstance().getTextureManager().bind(new Identifier("textures/gui/container/inventory.png"));
			GuiElement.drawTexture(x, y, 3 * 18, 198, 18, 18, 16, 16, 256, 256);
		});

		private final TeamUpgradeRenderer renderer;

		public static TrapType getFuzzy(String s) {
			s = s.toLowerCase(Locale.ROOT);
			if (s.contains("miner")) {
				return MINER_FATIGUE;
			}
			if (s.contains("alarm")) {
				return ALARM;
			}
			if (s.contains("counter")) {
				return COUNTER_OFFENSIVE;
			}
			return ITS_A_TRAP;
		}

		public void draw(int x, int y, int width, int height) {
			renderer.render(x, y, width, height, 0);
		}
	}
}
