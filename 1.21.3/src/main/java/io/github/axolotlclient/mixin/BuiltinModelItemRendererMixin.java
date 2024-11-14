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

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class BuiltinModelItemRendererMixin {

	@Inject(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ShieldModel;handle()Lnet/minecraft/client/model/geom/ModelPart;"))
	private void axolotlclient$lowShield(net.minecraft.world.item.ItemStack stack, ItemDisplayContext transformationMode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.lowShield.get()
			&& Minecraft.getInstance().options.getCameraType().isFirstPerson()
			&& (transformationMode.equals(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
				|| transformationMode.equals(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND))) {
			matrices.translate(0, 0.2F, 0);
		}
	}
}
