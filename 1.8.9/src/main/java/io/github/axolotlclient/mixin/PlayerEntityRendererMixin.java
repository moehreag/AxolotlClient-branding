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

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<ClientPlayerEntity> {

	public PlayerEntityRendererMixin(EntityRenderDispatcher dispatcher, Model model, float shadowSize) {
		super(dispatcher, model, shadowSize);
	}

	@ModifyArgs(method = "renderNameTag(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;DDDLjava/lang/String;FD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;FD)V"))
	public void axolotlclient$modifiyName(Args args) {
		if (AxolotlClient.CONFIG != null) {
			ClientPlayerEntity player = args.get(0);
			if (player.getUuid() == Minecraft.getInstance().player.getUuid()
				&& NickHider.getInstance().hideOwnName.get()) {
				args.set(4, NickHider.getInstance().hiddenNameSelf.get());
			} else if (player.getUuid() != Minecraft.getInstance().player.getUuid()
					   && NickHider.getInstance().hideOtherNames.get()) {
				args.set(4, NickHider.getInstance().hiddenNameOthers.get());
			}
		}
	}
}
