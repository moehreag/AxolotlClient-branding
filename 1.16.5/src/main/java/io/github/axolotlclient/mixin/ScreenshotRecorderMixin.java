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

import java.io.File;
import java.util.function.Consumer;

import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.util.ScreenshotUtils.class)
public abstract class ScreenshotRecorderMixin {

	private static File currentFile;

	@SuppressWarnings("unresolvable-target")
	@Inject(method = "method_1661", at = @At("HEAD"))
	private static void axolotlclient$getFile(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
		currentFile = file;
	}

	@SuppressWarnings({"unchecked", "unresolvable-target"})
	@ModifyArg(method = "method_1661", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
	private static <T> T axolotlclient$onScreenShotTaken(T t) {
		return (T) ScreenshotUtils.getInstance().onScreenshotTaken((MutableText) t, currentFile);
	}
}
