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
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHead;
import io.github.axolotlclient.util.BadgeRenderer;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

	@Shadow
	@Final
	protected EntityRenderDispatcher dispatcher;

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotate(FFFF)V", ordinal = 1))
	private void axolotlclient$correctNameplateRotation(Entity entity, String string, double d, double e, double f, int i, CallbackInfo ci) {
		if (MinecraftClient.getInstance().options.perspective == Perspective.THIRD_PERSON_FRONT.ordinal()) {
			GlStateManager.rotate(-this.dispatcher.pitch * 2, 1.0F, 0.0F, 0.0F);
		}
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;III)I"))
	public void axolotlclient$addBadges(T entity, String string, double d, double e, double f, int i, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity && string.contains(entity.getName().asFormattedString()))
			BadgeRenderer.renderNametagBadge(entity);
	}

	@Redirect(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;III)I"))
	public int axolotlclient$forceShadows(TextRenderer instance, String text, int x, int y, int color) {
		instance.draw(text, x, y, color, AxolotlClient.CONFIG.useShadows.get());
		return 0;
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;III)I", ordinal = 1))
	public void axolotlclient$addLevel(T entity, String string, double d, double e, double f, int i, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity && string.contains(entity.getName().asFormattedString())) {
			if (Util.currentServerAddressContains("hypixel.net")) {
				if (BedwarsMod.getInstance().isEnabled() &&
					BedwarsMod.getInstance().inGame() &&
					BedwarsMod.getInstance().bedwarsLevelHead.get()) {
					String levelhead = BedwarsMod.getInstance().getGame().get().getLevelHead((AbstractClientPlayerEntity) entity);
					if (levelhead != null) {
						axolotlclient$drawLevelHead(levelhead);
					}
				} else if (LevelHead.getInstance().enabled.get()) {
					String text = "Level: " + HypixelAbstractionLayer.getPlayerLevel(String.valueOf(entity.getUuid()), LevelHead.getInstance().mode.get());

					if(LevelHead.getInstance().mode.get().equals(LevelHeadMode.BEDWARS.toString())){
						text += "☆";
					}

					axolotlclient$drawLevelHead(text);
				}
			}
		}
	}

	@Unique
	private void axolotlclient$drawLevelHead(String text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		float x = textRenderer.getStringWidth(text) / 2F;
		int y = text.contains("deadmau5") ? -20 : -10;

		if (LevelHead.getInstance().background.get()) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuffer();
			GlStateManager.disableTexture();
			bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
			bufferBuilder.vertex(-x - 1, -1 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
			bufferBuilder.vertex(-x - 1, 8 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
			bufferBuilder.vertex(x + 1, 8 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
			bufferBuilder.vertex(x + 1, -1 + y, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).next();
			tessellator.draw();
			GlStateManager.enableTexture();
		}

		textRenderer.draw(text, -x, y, LevelHead.getInstance().textColor.get().getAsInt(),
			AxolotlClient.CONFIG.useShadows.get());
	}

	@Redirect(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;vertex(DDD)Lnet/minecraft/client/render/BufferBuilder;"))
	public BufferBuilder axolotlclient$noBg(BufferBuilder instance, double d, double e, double f) {
		if (AxolotlClient.CONFIG.nametagBackground.get()) {
			instance.vertex(d, e, f);
		}
		return instance;
	}
}
