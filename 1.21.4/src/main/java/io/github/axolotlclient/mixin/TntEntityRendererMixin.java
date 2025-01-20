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
import io.github.axolotlclient.modules.tnttime.TntTime;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntRenderer.class)
public abstract class TntEntityRendererMixin extends EntityRenderer<PrimedTnt, TntRenderState> {

	protected TntEntityRendererMixin(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Inject(
		method = "render(Lnet/minecraft/client/renderer/entity/state/TntRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(value = "TAIL"))
	private void axolotlclient$render(TntRenderState tntRenderState, PoseStack matrices, MultiBufferSource vertexConsumers, int i, CallbackInfo ci) {
		if (TntTime.getInstance().enabled.get()) {
			matrices.pushPose();
			if (tntRenderState.nameTag != null) {
				matrices.translate(0, 0.25, 0);
			}
			Vec3 prevAttachment = tntRenderState.nameTagAttachment;
			if (prevAttachment == null) {
				tntRenderState.nameTagAttachment = new Vec3(0, 2, 0);
			}
			renderNameTag(tntRenderState, TntTime.getInstance().getFuseTime(tntRenderState.fuseRemainingInTicks),
								matrices, vertexConsumers, i);
			tntRenderState.nameTagAttachment = prevAttachment;
			matrices.popPose();
		}
	}
}
