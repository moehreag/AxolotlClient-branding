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

import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import io.github.axolotlclient.modules.zoom.Zoom;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.MouseInputEvent;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

	@Inject(method = "onPress", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"))
	private void axolotlclient$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (action == 1) {
			Events.MOUSE_INPUT.invoker().invoke(new MouseInputEvent(window, button, action, mods));
		}
	}

	@Inject(method = "onScroll",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"), cancellable = true)
	private void axolotlclient$scrollTooltips(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci) {
		if (ScrollableTooltips.getInstance().enabled.get() && Math.signum(scrollDeltaY) != 0) {
			if (ScrollableTooltips.getInstance().onScroll(Math.signum(scrollDeltaY) > 0)) {
				ci.cancel();
			}
		}
	}

	@ModifyArg(method = "onScroll",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
	private double axolotlclient$scrollZoom(double scrollAmount) {
		if (scrollAmount != 0 && Zoom.scroll(scrollAmount)) {
			return 0;
		}

		return scrollAmount;
	}

	@Inject(method = "setIgnoreFirstMove", at = @At(value = "HEAD"))
	private void axolotlclient$onResolutionChanged(CallbackInfo ci) {
		// Resize and rebuild!
		HudManager.getInstance().refreshAllBounds();
	}
}
