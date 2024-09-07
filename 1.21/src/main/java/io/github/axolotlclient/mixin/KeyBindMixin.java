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

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.KeyBindChangeEvent;
import io.github.axolotlclient.util.events.impl.KeyPressEvent;
import net.minecraft.client.option.KeyBind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBind.class)
public abstract class KeyBindMixin {

	@Inject(method = "setBoundKey", at = @At("RETURN"))
	public void axolotlclient$boundKeySet(InputUtil.Key key, CallbackInfo ci) {
		Events.KEYBIND_CHANGE.invoker().invoke(new KeyBindChangeEvent(key));
	}

	@Inject(method = "setPressed", at = @At("RETURN"))
	public void axolotlclient$onPress(boolean pressed, CallbackInfo ci) {
		if (pressed) {
			Events.KEY_PRESS.invoker().invoke(new KeyPressEvent((KeyBind) ((Object) this)));
		}
	}
}
