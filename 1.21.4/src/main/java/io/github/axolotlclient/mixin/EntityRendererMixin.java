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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHead;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

	@Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 0))
	public void axolotlclient$addBadges(S entityRenderState, Component text, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
		if (entityRenderState instanceof PlayerRenderState state && text.equals(entityRenderState.nameTag)) {
			if (!state.isDiscrete) {
				if (AxolotlClient.CONFIG.showBadges.get()) {
					RenderSystem.enableDepthTest();
					Player entity = (Player) Minecraft.getInstance().level.getEntity(state.id);
					if (entity != null && UserRequest.getOnline(entity.getStringUUID())) {
						assert Minecraft.getInstance().player != null;
						int x = -(Minecraft.getInstance().font.width(entity == Minecraft.getInstance().player ? (NickHider.getInstance().hideOwnName.get() ? NickHider.getInstance().hiddenNameSelf.get() : PlayerTeam.formatNameForTeam(entity.getTeam(), entity.getName()).getString()) : (NickHider.getInstance().hideOtherNames.get() ? NickHider.getInstance().hiddenNameOthers.get() : PlayerTeam.formatNameForTeam(entity.getTeam(), entity.getName()).getString())) / 2 + (AxolotlClient.CONFIG.customBadge.get() ? Minecraft.getInstance().font.width(" " + ChatFormatting.stripFormatting(AxolotlClient.CONFIG.badgeText.get())) : 10));

						if (AxolotlClient.CONFIG.customBadge.get()) {
							Component badgeText = Util.formatFromCodes(AxolotlClient.CONFIG.badgeText.get());
							Minecraft.getInstance().font.drawInBatch(badgeText, x+6, 0, -1, AxolotlClient.CONFIG.useShadows.get(), matrices.last().pose(), vertexConsumers, Font.DisplayMode.NORMAL, 0, 15728880);
						} else {
							var type = RenderType.guiTextured(AxolotlClient.badgeIcon);
							var builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(type);
							Matrix4f matrix4f = matrices.last().pose();
							builder.addVertex(matrix4f, x, 0, 0).setUv(0, 0).setColor(-1);
							builder.addVertex(matrix4f, x, 8, 0).setUv(0, 1).setColor(-1);
							builder.addVertex(matrix4f, x + 8, 8, 0).setUv(1, 1).setColor(-1);
							builder.addVertex(matrix4f, x + 8, 0, 0).setUv(1, 0).setColor(-1);
							Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
						}
					}
					RenderSystem.disableDepthTest();
				}
			}
		}
	}

	@ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"), index = 8)
	public int axolotlclient$bgColor(int color) {
		if (AxolotlClient.CONFIG.nametagBackground.get()) {
			return color;
		} else {
			return 0;
		}
	}

	@ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"), index = 4)
	public boolean axolotlclient$enableShadows(boolean shadow) {
		return AxolotlClient.CONFIG.useShadows.get();
	}

	@Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 1))
	public void axolotlclient$addLevel(S entityRenderState, Component c, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
		if (entityRenderState instanceof PlayerRenderState state && c.equals(entityRenderState.nameTag)) {
			if (Minecraft.getInstance().getCurrentServer() != null && Minecraft.getInstance().getCurrentServer().ip.contains("hypixel.net")) {
				AbstractClientPlayer entity = (AbstractClientPlayer) Minecraft.getInstance().level.getEntity(state.id);
				if (entity != null) {
					Font textRenderer = Minecraft.getInstance().font;
					if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().bedwarsLevelHead.get()) {
						String text = BedwarsMod.getInstance().getGame().get().getLevelHead(entity);
						if (text != null) {
							float x = -textRenderer.width(text) / 2F;
							float y = c.getString().contains("deadmau5") ? -20 : -10;

							Matrix4f matrix4f = matrices.last().pose();
							textRenderer.drawInBatch(text, x, y, LevelHead.getInstance().textColor.get().toInt(), AxolotlClient.CONFIG.useShadows.get(), matrix4f, vertexConsumers, Font.DisplayMode.NORMAL, LevelHead.getInstance().background.get() ? 127 : 0, light);
						}
					} else if (LevelHead.getInstance().enabled.get()) {
						String text = "Level: " + HypixelAbstractionLayer.getPlayerLevel(String.valueOf(entity.getUUID()), LevelHead.getInstance().mode.get());

						if (LevelHead.getInstance().mode.get().equals(LevelHeadMode.BEDWARS)) {
							text += "☆";
						}

						float x = -textRenderer.width(text) / 2F;
						float y = c.getString().contains("deadmau5") ? -20 : -10;

						Matrix4f matrix4f = matrices.last().pose();
						textRenderer.drawInBatch(text, x, y, LevelHead.getInstance().textColor.get().toInt(), AxolotlClient.CONFIG.useShadows.get(), matrix4f, vertexConsumers, Font.DisplayMode.NORMAL, LevelHead.getInstance().background.get() ? 127 : 0, light);
					}
				}
			}
		}
	}
}
