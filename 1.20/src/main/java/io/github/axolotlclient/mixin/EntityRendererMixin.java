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
import net.minecraft.client.render.GameRenderer;
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
		if (entity instanceof AbstractClientPlayerEntity && text.equals(entity.getDisplayName())) {
			if (!entity.isSneaky()) {
				if (AxolotlClient.CONFIG.showBadges.get() && UserRequest.getOnline(entity.getUuid().toString())) {
					RenderSystem.enableDepthTest();

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
						if (AxolotlClient.CONFIG.useShadows.get()) {
							matrices.push();
							matrices.translate(0, 0, 0.1f);
							MinecraftClient.getInstance().textRenderer.draw(badgeText, x+6, 0, -1, true,
								matrices.peek().getModel(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
							matrices.pop();
						}
						MinecraftClient.getInstance().textRenderer.draw(badgeText, x+6, 0, -1, false,
							matrices.peek().getModel(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
					} else {
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderTexture(0, AxolotlClient.badgeIcon);
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder builder = tessellator.getBufferBuilder();
						Matrix4f matrix4f = matrices.peek().getModel();
						builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
						builder.vertex(matrix4f, x, 0, 0).uv(0, 0).next();
						builder.vertex(matrix4f, x, 8, 0).uv(0, 1).next();
						builder.vertex(matrix4f, x + 8, 8, 0).uv(1, 1).next();
						builder.vertex(matrix4f, x + 8, 0, 0).uv(1, 0).next();
						BufferRenderer.drawWithShader(builder.end());
					}
					RenderSystem.disableDepthTest();
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

	@ModifyArg(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 1), index = 4)
	private boolean enableShadows(boolean par5, @Local Matrix4f matrix4f) {
		matrix4f.scale(1, 1, -1);
		return AxolotlClient.CONFIG.useShadows.get();
	}

	@Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", ordinal = 1))
	public void axolotlclient$addLevel(T entity, Text string, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
									   CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity && string.equals(entity.getDisplayName())) {
			if (MinecraftClient.getInstance().getCurrentServerEntry() != null
				&& MinecraftClient.getInstance().getCurrentServerEntry().address.contains("hypixel.net")) {
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
