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

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {

	@Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
	private void injectResources(ResourceLocation resourceLocation, CallbackInfoReturnable<Optional<Resource>> cir) {
		if (AxolotlClient.runtimeResources.containsKey(resourceLocation)) {
			cir.setReturnValue(Optional.of(AxolotlClient.runtimeResources.get(resourceLocation)));
		}
	}

	@Inject(method = "listResources", at = @At(value = "TAIL"))
	private void injectResourcesList(String startingPath, Predicate<ResourceLocation> pathFilter, CallbackInfoReturnable<Map<ResourceLocation, Resource>> cir, @Local Map<ResourceLocation, Resource> map) {
		AxolotlClient.runtimeResources.forEach((resourceLocation, resource) -> {
			if (resourceLocation.getPath().startsWith(startingPath) && pathFilter.test(resourceLocation)) {
				map.put(resourceLocation, resource);
			}
		});
	}
}
