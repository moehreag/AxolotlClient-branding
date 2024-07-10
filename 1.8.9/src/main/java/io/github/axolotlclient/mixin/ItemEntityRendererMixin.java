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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

	@Inject(method = "applyItemBobbing", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;translatef(FFF)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
	private void axolotlclient$transformItems(ItemEntity itemEntity, double d, double e, double f, float g, BakedModel bakedModel, CallbackInfoReturnable<Integer> cir, ItemStack stack, Item item, boolean bl, int i) {
		if (AxolotlClient.CONFIG.flatItems.get()) {
			GlStateManager.translated(d, e + 0.05, f);
			GlStateManager.rotatef(itemEntity.pitch, 0, 0, 1);
			GlStateManager.rotatef(90, 1, 0, 0);
			if (!itemEntity.onGround) {
				itemEntity.pitch -= 5;
				GlStateManager.rotatef(itemEntity.pitch, 1, 1, 1);
			}
			cir.setReturnValue(i);
		}
	}
}
