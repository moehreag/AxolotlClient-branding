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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMode;
import io.github.axolotlclient.util.ClientColors;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author DarkKronicle
 */

public class TrapUpgrade extends TeamUpgrade {

	private final static Pattern[] REGEX = {
		Pattern.compile("^\\b[A-Za-z0-9_§]{3,16}\\b purchased (.+) Trap.?\\s*$"),
		Pattern.compile("Trap was set (off)!"),
		Pattern.compile("Removed (.+) from the (queue)!\\s*$")
	};

	private final List<TrapType> traps = new ArrayList<>(3);

	public TrapUpgrade() {
		super("trap", REGEX);
	}

	@Override
	protected void onMatch(TeamUpgrade upgrade, Matcher matcher) {
		if (matcher.group(1).equals("off")) {
			// Trap went off
			traps.removeFirst();
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
		switch (traps.size()) {
			case 0 -> {
				return 1;
			}
			case 1 -> {
				return 2;
			}
			case 2 -> {
				return 4;
			}
		}
		return 0;
	}

	@Override
	public boolean isPurchased() {
		return !traps.isEmpty();
	}

	@Override
	public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
		if (traps.isEmpty()) {
			Color color = ClientColors.DARK_GRAY;
			RenderSystem.setShaderColor(color.getAlpha() / 255F, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			graphics.blit(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("textures/item/barrier.png"), x, y, 0, 0, 16, 16, 16, 16);
		} else {
			for (TrapType type : traps) {
				RenderSystem.setShaderColor(1, 1, 1, 1);
				type.draw(graphics, x, y, width, height);
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
		ITS_A_TRAP((graphics, x, y, width, height, unused) ->
			graphics.blitSprite(RenderType::guiTextured, Minecraft.getInstance().getMobEffectTextures().get(MobEffects.BLINDNESS), x, y, 0, width, height)),
		COUNTER_OFFENSIVE((graphics, x, y, width, height, unused) ->
			graphics.blitSprite(RenderType::guiTextured, Minecraft.getInstance().getMobEffectTextures().get(MobEffects.MOVEMENT_SPEED), x, y, 0, width, height)),
		ALARM((graphics, x, y, width, height, unused) ->
			graphics.renderItem(new ItemStack(Items.ENDER_EYE), x, y, 0)),
		MINER_FATIGUE((graphics, x, y, width, height, unused) ->
			graphics.blitSprite(RenderType::guiTextured, Minecraft.getInstance().getMobEffectTextures().get(MobEffects.DIG_SLOWDOWN), x, y, 0, width, height));

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

		public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
			renderer.render(graphics, x, y, width, height, 0);
		}
	}
}
