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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {

	@Shadow
	@Final
	private RandomSource random;

	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	protected ItemEntityRendererMixin(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), cancellable = true)
	private void minimalItemPhysics(ItemEntityRenderState itemEntityRenderState, PoseStack stack, MultiBufferSource vertexConsumers, int i, CallbackInfo ci, @Local ItemStack itemStack, @Local BakedModel bakedModel, @Local boolean bl, @Local(argsOnly = true) int k) {
		// TODO
		/*if (AxolotlClient.CONFIG.flatItems.get()) {
			Matrix4f matrix = new Matrix4f();
			stack.mulPose(Axis.ZP.rotationDegrees(itemEntity.getPitch()).get(matrix));
			stack.mulPose(Axis.XP.rotationDegrees(90).get(matrix));
			if (!itemEntity.isOnGround()) {
				itemEntity.setPitch(itemEntity.getPitch() - 5);
				stack.mulPose(Axis.XP.rotationDegrees(itemEntity.getPitch()).get(matrix));
				stack.mulPose(Axis.YP.rotationDegrees(itemEntity.getPitch()).get(matrix));
				stack.mulPose(Axis.ZP.rotationDegrees(itemEntity.getPitch()).get(matrix));
			}
			BakedModel bakedModel = itemEntityRenderState.itemModel;
			if (bakedModel != null) {
				stack.pushPose();
				ItemStack itemStack = itemEntityRenderState.item;
				this.random.setSeed(ItemEntityRenderer.getSeedForItemStack(itemStack));
				boolean bl = bakedModel.isGui3d();
				float f = 0.25F;
				float g = Mth.sin(itemEntityRenderState.ageInTicks / 10.0F + itemEntityRenderState.bobOffset) * 0.1F + 0.1F;
				float h = bakedModel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
				stack.translate(0.0F, g + 0.25F * h, 0.0F);
				float j = ItemEntity.getSpin(itemEntityRenderState.ageInTicks, itemEntityRenderState.bobOffset);
				stack.mulPose(Axis.YP.rotation(j));
				ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, stack, vertexConsumers, i, itemStack, bakedModel, bl, this.random);
				stack.popPose();
				super.render(itemEntityRenderState, stack, vertexConsumers, i);
			}
			}

			stack.pop();
			super.render(itemEntity, f, g, stack, vertexConsumerProvider, i);
			ci.cancel();
		}*/
	}
}
