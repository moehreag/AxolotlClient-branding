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

package io.github.axolotlclient.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Unique
    private int recursionDepth;

    @WrapMethod(method = "renderTooltipInternal")
    private void axolotlclient$scrollableTooltipsX(Font font, List<ClientTooltipComponent> list, int x, int y, ClientTooltipPositioner clientTooltipPositioner, @Nullable ResourceLocation resourceLocation, Operation<Void> original) {
        if (ScrollableTooltips.getInstance().enabled.get()) {
            Minecraft mc = Minecraft.getInstance();
            if ((!(mc.screen instanceof CreativeModeInventoryScreen c)) || c.isInventoryOpen()) {
                if (recursionDepth == 0) {
                    x += ScrollableTooltips.getInstance().tooltipOffsetX;
                    y += ScrollableTooltips.getInstance().tooltipOffsetY;
                }
            }
        }
        recursionDepth++;
        original.call(font, list, x, y, clientTooltipPositioner, resourceLocation);
        recursionDepth--;
    }
}
