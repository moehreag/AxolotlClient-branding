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
import io.github.axolotlclient.modules.freelook.Perspective;
import io.github.axolotlclient.modules.hud.gui.hud.PlayerHud;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.util.BadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> extends EntityRenderer<T> {

	protected LivingEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Inject(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotatef(FFFF)V", ordinal = 1))
	private void axolotlclient$correctNameplateRotation(LivingEntity livingEntity, double d, double e, double f, CallbackInfo ci) {
		if (Minecraft.getInstance().options.perspective == Perspective.THIRD_PERSON_FRONT.ordinal()) {
			GlStateManager.rotatef(-this.dispatcher.cameraPitch * 2, 1.0F, 0.0F, 0.0F);
		}
	}

	@Inject(method = "shouldRenderNameTag(Lnet/minecraft/entity/living/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$showOwnNametag(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
		if (AxolotlClient.CONFIG.showOwnNametag.get()
			&& livingEntity.getNetworkId() == Minecraft.getInstance().player.getNetworkId()
			&& !PlayerHud.isCurrentlyRendering()) {
			cir.setReturnValue(true);
		}
	}

	@Redirect(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/LivingEntity;getDisplayName()Lnet/minecraft/text/Text;"))
	public Text axolotlclient$hideNameWhenSneaking(LivingEntity instance) {
		if (instance instanceof ClientPlayerEntity) {
			if (NickHider.getInstance().hideOwnName.get() && instance.equals(Minecraft.getInstance().player)) {
				return new LiteralText(NickHider.getInstance().hiddenNameSelf.get());
			} else if (NickHider.getInstance().hideOtherNames.get()
				&& !instance.equals(Minecraft.getInstance().player)) {
				return new LiteralText(NickHider.getInstance().hiddenNameOthers.get());
			}
		}
		return instance.getDisplayName();
	}

	@Inject(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;draw(Ljava/lang/String;III)I"))
	public void axolotlclient$addBadge(LivingEntity livingEntity, double d, double e, double f, CallbackInfo ci) {
		if (!NickHider.getInstance().hideOwnName.get() && livingEntity.equals(Minecraft.getInstance().player))
			BadgeRenderer.renderNametagBadge(livingEntity);
		else if (!NickHider.getInstance().hideOtherNames.get() && !livingEntity.equals(Minecraft.getInstance().player))
			BadgeRenderer.renderNametagBadge(livingEntity);
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 1.0f, ordinal = 0))
	private float axolotlclient$customHitColorRed(float constant) {
		return AxolotlClient.CONFIG.hitColor.get().getRed() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.0f, ordinal = 0))
	private float axolotlclient$customHitColorGreen(float constant) {
		return AxolotlClient.CONFIG.hitColor.get().getGreen() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.0f, ordinal = 1))
	private float axolotlclient$customHitColorBlue(float constant) {
		return AxolotlClient.CONFIG.hitColor.get().getBlue() / 255F;
	}

	@ModifyConstant(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z", constant = @Constant(floatValue = 0.3f, ordinal = 0))
	private float axolotlclient$customHitColorAlpha(float constant) {
		return AxolotlClient.CONFIG.hitColor.get().getAlpha() / 255F;
	}
}
