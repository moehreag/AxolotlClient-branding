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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Axis;
import net.minecraft.util.random.RandomGenerator;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {

	@Shadow
	@Final
	private RandomGenerator random;

	@Shadow
	@Final
	private ItemRenderer itemRenderer;

	protected ItemEntityRendererMixin(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), cancellable = true)
	private void minimalItemPhysics(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci, @Local ItemStack itemStack, @Local BakedModel bakedModel, @Local boolean bl, @Local(argsOnly = true) int k) {
		if (AxolotlClient.CONFIG.flatItems.get()) {
			Matrix4f matrix = new Matrix4f();
			matrixStack.multiply(Axis.Z_POSITIVE.rotationDegrees(itemEntity.getPitch()).get(matrix));
			matrixStack.multiply(Axis.X_POSITIVE.rotationDegrees(90).get(matrix));
			if (!itemEntity.isOnGround()) {
				itemEntity.setPitch(itemEntity.getPitch() - 5);
				matrixStack.multiply(Axis.X_POSITIVE.rotationDegrees(itemEntity.getPitch()).get(matrix));
				matrixStack.multiply(Axis.Y_POSITIVE.rotationDegrees(itemEntity.getPitch()).get(matrix));
				matrixStack.multiply(Axis.Z_POSITIVE.rotationDegrees(itemEntity.getPitch()).get(matrix));
			}
		}
	}

	@WrapOperation(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void removeTranslationIfPhysicsOn(MatrixStack instance, float x, float y, float z, Operation<Void> original) {
		if (!AxolotlClient.CONFIG.flatItems.get()) {
			original.call(instance, x, y, z);
		}
	}

	@WrapOperation(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;rotate(Lorg/joml/Quaternionf;)V"))
	private void removeRotationIfPhysicsOn(MatrixStack instance, Quaternionf quaternionf, Operation<Void> original) {
		if (!AxolotlClient.CONFIG.flatItems.get()) {
			original.call(instance, quaternionf);
		}
	}
}
