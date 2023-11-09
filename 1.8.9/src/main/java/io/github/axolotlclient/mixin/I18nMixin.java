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

import io.github.axolotlclient.util.translation.Translations;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(I18n.class)
public abstract class I18nMixin {

	private static final String KEY_PREFIX = "axolotlclient.";

	@Inject(method = "translate", at = @At("HEAD"), cancellable = true)
	private static void axolotlclient$translate(String key, Object[] args, CallbackInfoReturnable<String> callback) {
		if (key.startsWith(KEY_PREFIX)) {
			callback.setReturnValue(Translations
				.format(Translations.getInstance().translate(key.substring(KEY_PREFIX.length())), args));
		} else if (Translations.hasTranslation(key)) {
			callback.setReturnValue(Translations.getInstance().translate(key, args));
		}
	}
}
