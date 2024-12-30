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

import java.io.File;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenshotUtils.class)
public abstract class ScreenshotUtilsMixin {

	@Inject(method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;IILcom/mojang/blaze3d/pipeline/RenderTarget;)Lnet/minecraft/text/Text;", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
	private static void axolotlclient$onScreenshotSaveSuccess(File parent, String name, int textureWidth, int textureHeight,
															  RenderTarget buffer, CallbackInfoReturnable<Text> cir, @Local(ordinal = 2) File target) {
		cir.setReturnValue(io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils.getInstance()
			.onScreenshotTaken(cir.getReturnValue(), target));
	}
}
