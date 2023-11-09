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

import com.mojang.authlib.GameProfile;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

	@Shadow
	@Final
	private GameProfile profile;

	@Inject(method = "getSkinTexture", at = @At("RETURN"), cancellable = true)
	private void axolotlclient$hideSkins(CallbackInfoReturnable<Identifier> cir) {
		if (profile.equals(MinecraftClient.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOwnSkin.get()) {
			cir.setReturnValue(DefaultSkinHelper.getTexture(profile.getId()));
		} else if (!profile.equals(MinecraftClient.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOtherSkins.get()) {
			cir.setReturnValue(DefaultSkinHelper.getTexture(profile.getId()));
		}
	}
}
