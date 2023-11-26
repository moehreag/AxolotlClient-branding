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

import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

	@ModifyVariable(method = "renderTooltipFromComponents",
		at = @At("STORE"), index = 8)
	private int axolotlclient$scrollableTooltipsX(int x) {
		if (ScrollableTooltips.getInstance().enabled.get()) {
			if ((MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen)
				&& !((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).m_zqfbkfzl()) {
				return x;
			}

			return x + ScrollableTooltips.getInstance().tooltipOffsetX;
		}
		return x;
	}

	@ModifyVariable(method = "renderTooltipFromComponents",
		at = @At("STORE"), index = 9)
	private int axolotlclient$scrollableTooltipsY(int y) {
		if (ScrollableTooltips.getInstance().enabled.get()) {
			if ((MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen)
				&& !((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).m_zqfbkfzl()) {
				return y;
			}
			return y + ScrollableTooltips.getInstance().tooltipOffsetY;
		}
		return y;
	}

	@Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/ClickEvent;getAction()Lnet/minecraft/text/ClickEvent$Action;", ordinal = 0), cancellable = true)
	public void axolotlclient$customClickEvents(Style style, CallbackInfoReturnable<Boolean> cir) {
		ClickEvent event = style.getClickEvent();
		if (event instanceof ScreenshotUtils.CustomClickEvent) {
			((ScreenshotUtils.CustomClickEvent) event).doAction();
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;fillGradient(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), cancellable = true)
	private void axolotlclient$menuBlur(MatrixStack matrices, int vOffset, CallbackInfo ci) {
		if (MenuBlur.getInstance().renderScreen(matrices)) {
			ci.cancel();
		}
	}
}
