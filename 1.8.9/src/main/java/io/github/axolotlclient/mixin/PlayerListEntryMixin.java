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

import com.mojang.authlib.GameProfile;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.PlayerInfo;
import net.minecraft.client.resource.skin.DefaultSkinUtils;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerListEntryMixin {

	@Shadow
	@Final
	private GameProfile profile;

	@Inject(method = "getSkinTexture", at = @At("RETURN"), cancellable = true)
	public void axolotlclient$hideSkins(CallbackInfoReturnable<Identifier> cir) {
		if (profile.equals(Minecraft.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOwnSkin.get()) {
			cir.setReturnValue(DefaultSkinUtils.getDefaultSkin(profile.getId()));
		} else if (!profile.equals(Minecraft.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOtherSkins.get()) {
			cir.setReturnValue(DefaultSkinUtils.getDefaultSkin(profile.getId()));
		}
	}
}
