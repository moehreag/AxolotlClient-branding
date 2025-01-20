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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHead;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 0))
	public void axolotlclient$addBadges(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
										CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity && text.getString().contains(entity.getName().getString())) {
			if (!entity.isSneaky()) {
				if (AxolotlClient.CONFIG.showBadges.get() && UserRequest.getOnline(entity.getUuid().toString())) {
					RenderSystem.enableDepthTest();
					RenderSystem.setShaderTexture(0, AxolotlClient.badgeIcon);

					assert MinecraftClient.getInstance().player != null;
					int x = -(MinecraftClient.getInstance().textRenderer
								  .getWidth(
									  entity.getUuid() == MinecraftClient.getInstance().player.getUuid()
										  ? (NickHider.getInstance().hideOwnName.get()
										  ? NickHider.getInstance().hiddenNameSelf.get()
										  : Team.decorateName(entity.getScoreboardTeam(), entity.getName())
										  .getString())
										  : (NickHider.getInstance().hideOtherNames.get()
										  ? NickHider.getInstance().hiddenNameOthers.get()
										  : Team.decorateName(entity.getScoreboardTeam(), entity.getName())
										  .getString()))
							  / 2
							  + (AxolotlClient.CONFIG.customBadge.get() ? MinecraftClient.getInstance().textRenderer
						.getWidth(" " + Formatting.strip(AxolotlClient.CONFIG.badgeText.get())) : 10));

					RenderSystem.setShaderColor(1, 1, 1, 1);

					if (AxolotlClient.CONFIG.customBadge.get()) {
						Text badgeText = Util.formatFromCodes(AxolotlClient.CONFIG.badgeText.get());
						MinecraftClient.getInstance().textRenderer.draw(badgeText, x, 0, -1, AxolotlClient.CONFIG.useShadows.get(),
							matrices.peek().getModel(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
					} else {
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder builder = tessellator.getBufferBuilder();
						Matrix4f matrix4f = matrices.peek().getModel();
						builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
						builder.vertex(matrix4f, x, 0, 0).uv(0, 0);
						builder.vertex(matrix4f, x, 8, 0).uv(0, 8);
						builder.vertex(matrix4f, x + 8, 0, 0).uv(8, 0);
						builder.vertex(matrix4f, x + 8, 8, 0).uv(8, 8);
						BufferRenderer.drawWithShader(builder.end());
					}
				}
			}
		}
	}

	@ModifyArg(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"), index = 8)
	public int axolotlclient$bgColor(int color) {
		if (AxolotlClient.CONFIG.nametagBackground.get()) {
			return color;
		} else {
			return 0;
		}
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 0))
	public void axolotlclient$enableShadows(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Local boolean notSneaking, @Local Matrix4f matrix4f, @Local(ordinal = 1) int i, @Local(ordinal = 2) float h, @Local(ordinal = 2) int j, @Local TextRenderer textRenderer) {
		if(AxolotlClient.CONFIG.useShadows.get()) {
			textRenderer.draw(
				text, h, (float)i, notSneaking ? -1 : 553648127, true, matrix4f, vertexConsumers, notSneaking ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, light
			);
		}
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 1))
	public void axolotlclient$addLevel(T entity, Text string, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
									   CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity) {
			if (MinecraftClient.getInstance().getCurrentServerEntry() != null
				&& MinecraftClient.getInstance().getCurrentServerEntry().address.contains("hypixel.net")
				&& string.getString().contains(entity.getName().getString())) {
				TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
				if (BedwarsMod.getInstance().isEnabled() &&
					BedwarsMod.getInstance().inGame() &&
					BedwarsMod.getInstance().bedwarsLevelHead.get()) {
					String text = BedwarsMod.getInstance().getGame().get().getLevelHead((AbstractClientPlayerEntity) entity);
					if (text != null) {
						float x = -textRenderer.getWidth(text) / 2F;
						float y = string.getString().contains("deadmau5") ? -20 : -10;

						Matrix4f matrix4f = matrices.peek().getModel();
						MinecraftClient.getInstance().textRenderer.draw(text, x, y,
							LevelHead.getInstance().textColor.get().toInt(), AxolotlClient.CONFIG.useShadows.get(),
							matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, LevelHead.getInstance().background.get() ? 127 : 0,
							light);
					}
				} else if (LevelHead.getInstance().enabled.get()) {
					String text = "Level: " + HypixelAbstractionLayer.getPlayerLevel(String.valueOf(entity.getUuid()), LevelHead.getInstance().mode.get());

					if (LevelHead.getInstance().mode.get().equals(LevelHeadMode.BEDWARS)) {
						text += "☆";
					}

					float x = -textRenderer.getWidth(text) / 2F;
					float y = string.getString().contains("deadmau5") ? -20 : -10;

					Matrix4f matrix4f = matrices.peek().getModel();
					MinecraftClient.getInstance().textRenderer.draw(text, x, y,
						LevelHead.getInstance().textColor.get().toInt(), AxolotlClient.CONFIG.useShadows.get(),
						matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, LevelHead.getInstance().background.get() ? 127 : 0,
						light);
				}
			}
		}
	}
}
