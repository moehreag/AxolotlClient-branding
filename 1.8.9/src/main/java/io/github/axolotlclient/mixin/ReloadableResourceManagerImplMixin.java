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

import java.util.List;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.PackDisplayHud;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.util.translation.Translations;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {

	@Inject(method = "reload", at = @At("TAIL"))
	public void axolotlclient$onReload(List<ResourcePack> resourcePacks, CallbackInfo ci) {
		HypixelAbstractionLayer.clearPlayerData();

		PackDisplayHud hud = (PackDisplayHud) HudManager.getInstance().get(PackDisplayHud.ID);
		if (hud != null) {
			hud.setPacks(resourcePacks);
		}

		Translations.load();
	}

	@Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getResource(Identifier id, CallbackInfoReturnable<Resource> cir) {
		if (AxolotlClient.runtimeResources.get(id) != null) {
			cir.setReturnValue(AxolotlClient.runtimeResources.get(id));
		}
	}
}
