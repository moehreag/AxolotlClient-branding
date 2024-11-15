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
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.modules.tablist.Tablist;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {

	@Shadow
	private Component header;
	@Shadow
	private Component footer;
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract Component decorateName(PlayerInfo entry, MutableComponent name);

	@Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$nickHider(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
		assert minecraft.player != null;
		if (entry.getProfile().equals(minecraft.player.getGameProfile()) && NickHider.getInstance().hideOwnName.get()) {
			cir.setReturnValue(
				this.decorateName(entry, Component.literal(NickHider.getInstance().hiddenNameSelf.get())));
		} else if (!entry.getProfile().equals(minecraft.player.getGameProfile()) &&
			NickHider.getInstance().hideOtherNames.get()) {
			cir.setReturnValue(
				this.decorateName(entry, Component.literal(NickHider.getInstance().hiddenNameOthers.get())));
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I", ordinal = 0))
	private int axolotlclient$moveName(Font instance, FormattedText text, Operation<Integer> original, @Local PlayerInfo info) {
		int width = original.call(instance, text);
		if (AxolotlClient.CONFIG.showBadges.get() &&
			UserRequest.getOnline(info.getProfile().getId().toString())) width += 10;
		if (Tablist.getInstance().numericalPing.get())
			width += (instance.width(String.valueOf(info.getLatency())) - 10);
		return width;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
	public int axolotlclient$moveName2(GuiGraphics instance, Font font, Component component, int x, int y, int color, Operation<Integer> original, @Local PlayerInfo info) {
		if (AxolotlClient.CONFIG.showBadges.get() &&
			UserRequest.getOnline(info.getProfile().getId().toString())) {

			instance.blit(RenderType::guiTextured, AxolotlClient.badgeIcon, x, y, 0, 0, 8, 8, 8, 8);

			x += 9;
		}
		return original.call(instance, font, component, x, y, color);
	}

	@ModifyArg(method = "getNameForDisplay", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;decorateName(Lnet/minecraft/client/multiplayer/PlayerInfo;Lnet/minecraft/network/chat/MutableComponent;)Lnet/minecraft/network/chat/Component;"),
		index = 1)
	private MutableComponent axolotlclient$hideNames(MutableComponent name) {
		if (NickHider.getInstance().hideOwnName.get()) {
			return Component.literal(NickHider.getInstance().hiddenNameSelf.get());
		}
		if (NickHider.getInstance().hideOtherNames.get()) {
			return Component.literal(NickHider.getInstance().hiddenNameOthers.get());
		}
		return name;
	}

	@Inject(method = "renderPingIcon", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(GuiGraphics graphics, int width, int x, int y, PlayerInfo entry, CallbackInfo ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() &&
			(BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (Tablist.getInstance().renderNumericPing(graphics, width, x, y, entry)) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "render",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"))
	private boolean showPlayerHeads$1(Minecraft instance, Operation<Boolean> original) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@WrapOperation(method = "render",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$1(Connection instance, Operation<Boolean> original) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return original.call(instance);
		}
		return false;
	}

	@Inject(method = "render", at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;"))
	private void axolotlclient$setRenderHeaderFooter(GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
		if (!Tablist.getInstance().showHeader.get()) {
			header = null;
		}
		if (!Tablist.getInstance().showFooter.get()) {
			footer = null;
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZI)V"),
		index = 5)
	private boolean axolotlclient$renderHatLayer(boolean drawHat) {
		return drawHat || Tablist.getInstance().alwaysShowHeadLayer.get();
	}

	@Inject(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
	public void axolotlclient$renderWithoutObjective(GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci,
													 @Local(ordinal = 1) int i,
													 @Local(ordinal = 7) int n,
													 @Local(ordinal = 14) int v,
													 @Local(ordinal = 15) int y,
													 @Local PlayerInfo playerListEntry2) {
		if (!BedwarsMod.getInstance().isEnabled() || !BedwarsMod.getInstance().isWaiting()) {
			return;
		}
		int startX = v + i + 1;
		int endX = startX + n;
		String render;
		try {
			if (playerListEntry2.getProfile().getName().contains(ChatFormatting.OBFUSCATED.toString())) {
				return;
			}

			render = String.valueOf(HypixelAbstractionLayer.getPlayerLevel(
				playerListEntry2.getProfile().getId().toString().replace("-", ""), LevelHeadMode.BEDWARS));
		} catch (Exception e) {
			return;
		}
		graphics.drawString(minecraft.font, render,
			(endX - this.minecraft.font.width(render)) + 20, y, -1
		);
	}

	@Inject(method = "renderTablistScore", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"),
		cancellable = true)
	private void axolotlclient$renderCustomScoreboardObjective(Objective objective, int y, PlayerTabOverlay.ScoreDisplayEntry entry, int startX, int endX, UUID uuid, GuiGraphics graphics, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}

		game.renderCustomScoreboardObjective(graphics, entry.name().getString(), entry.score(), y, endX);

		ci.cancel();
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 1)
	public int axolotlclient$changeWidth(int value) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() &&
			(BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null || !game.isStarted()) {
			return;
		}
		BedwarsPlayer player = game.getPlayer(entry.getProfile().getName()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(Component.literal(player.getTabListDisplay()));
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
	public List<PlayerInfo> axolotlclient$overrideSortedPlayers(List<PlayerInfo> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		List<PlayerInfo> players = BedwarsMod.getInstance().getGame().get().getTabPlayerList(original);
		if (players == null) {
			return original;
		}
		return players;
	}

	@Inject(method = "setHeader", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeHeader(Component header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.header = BedwarsMod.getInstance().getGame().get().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Component footer, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.footer = BedwarsMod.getInstance().getGame().get().getBottomBarText();
		ci.cancel();
	}
}
