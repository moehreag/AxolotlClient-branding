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

package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

	@ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V",
		at = @At("STORE"), index = 11)
	private int axolotlclient$scrollableTooltipsX(int x) {
		if (ScrollableTooltips.getInstance().enabled.get()) {
			if ((MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen)
				&& !((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).isInventoryOpen()) {
				return x;
			}

			return x + ScrollableTooltips.getInstance().tooltipOffsetX;
		}
		return x;
	}

	@ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;)V",
		at = @At("STORE"), index = 12)
	private int axolotlclient$scrollableTooltipsY(int y) {
		if (ScrollableTooltips.getInstance().enabled.get()) {
			if ((MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen)
				&& !((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).isInventoryOpen()) {
				return y;
			}
			return y + ScrollableTooltips.getInstance().tooltipOffsetY;
		}
		return y;
	}

}
