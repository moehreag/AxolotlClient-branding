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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.inventory.slot.InventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenuScreen.class)
public abstract class HandledScreenMixin {

	@Shadow
	private InventorySlot hoveredSlot;
	private InventorySlot cachedSlot;

	@Shadow
	protected abstract boolean moveHoveredSlotToHotbar(int i);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;popMatrix()V"))
	public void axolotlclient$resetScrollOnSlotChange(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (ScrollableTooltips.getInstance().enabled.get() && cachedSlot != hoveredSlot) {
			cachedSlot = hoveredSlot;
			ScrollableTooltips.getInstance().resetScroll();
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$mouseClickedHead(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		if (mouseButton - 100 == Minecraft.getInstance().options.inventoryKey.getKeyCode()) {
			Minecraft.getInstance().closeScreen();
			ci.cancel();
		}
	}

	@Inject(method = "mouseClicked", at = @At("RETURN"))
	private void axolotlclient$mouseClickedTail(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		moveHoveredSlotToHotbar(mouseButton - 100);
	}
}
