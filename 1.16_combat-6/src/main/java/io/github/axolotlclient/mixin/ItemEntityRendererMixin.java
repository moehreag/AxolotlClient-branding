/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.Random;

import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {

	@Shadow
	@Final
	private Random random;

	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	protected ItemEntityRendererMixin(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void minimalItemPhysics(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci, ItemStack itemStack, int j, BakedModel bakedModel, boolean bl, int k, float h, float l, float m) {
		if (AxolotlClient.CONFIG.flatItems.get()) {
			matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(itemEntity.getPitch(0)));
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
			if (!itemEntity.isOnGround()) {
				itemEntity.pitch = (itemEntity.getPitch(0) - 5);
				matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(itemEntity.getPitch(0)));
				matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(itemEntity.getPitch(0)));
				matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(itemEntity.getPitch(0)));
			}
			float o = bakedModel.getTransformation().ground.scale.getX();
			float p = bakedModel.getTransformation().ground.scale.getY();
			float q = bakedModel.getTransformation().ground.scale.getZ();
			for (int u = 0; u < k; ++u) {
				matrixStack.push();
				if (u > 0) {
					if (bl) {
						float s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						float t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						float v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
						matrixStack.translate(s, t, v);
					} else {
						float s = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
						float t = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
						matrixStack.translate(s, t, 0.0F);
					}
				}

				this.itemRenderer
					.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
				matrixStack.pop();
				if (!bl) {
					matrixStack.translate(0.0F * o, 0.0F * p, 0.09375F * q);
				}
			}

			matrixStack.pop();
			super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
			ci.cancel();
		}
	}
}
