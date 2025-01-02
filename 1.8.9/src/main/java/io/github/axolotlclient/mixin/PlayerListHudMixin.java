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

import java.util.List;
import java.util.UUID;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.modules.tablist.Tablist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.network.PlayerInfo;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.network.Connection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Formatting;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin extends GuiElement {
	@Unique
	private final Minecraft axolotlclient$client = Minecraft.getInstance();
	@Shadow
	private Text header;
	@Shadow
	private Text footer;
	@Unique
	private PlayerInfo axolotlclient$playerListEntry;

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$nickHider(PlayerInfo playerEntry, CallbackInfoReturnable<String> cir) {
		if (playerEntry.getProfile().getId() == Minecraft.getInstance().player.getUuid()
			&& NickHider.getInstance().hideOwnName.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameSelf.get());
		} else if (playerEntry.getProfile().getId() != Minecraft.getInstance().player.getUuid()
				   && NickHider.getInstance().hideOtherNames.get()) {
			cir.setReturnValue(NickHider.getInstance().hiddenNameOthers.get());
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;getDisplayName(Lnet/minecraft/client/network/PlayerInfo;)Ljava/lang/String;"))
	public PlayerInfo axolotlclient$getPlayer(PlayerInfo playerInfo) {
		axolotlclient$playerListEntry = playerInfo;
		return playerInfo;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 0))
	public int axolotlclient$moveName(TextRenderer instance, String text) {
		if (AxolotlClient.CONFIG.showBadges.get() && UserRequest.getOnline(axolotlclient$playerListEntry.getProfile().getId().toString()))
			return instance.getWidth(text) + 10;
		return instance.getWidth(text);
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 1))
	public void axolotlclient$getCoords(Args args) {
		float x = args.get(1);
		float y = args.get(2);
		if (AxolotlClient.CONFIG.showBadges.get()
			&& UserRequest.getOnline(axolotlclient$playerListEntry.getProfile().getId().toString())) {
			axolotlclient$client.getTextureManager().bind(AxolotlClient.badgeIcon);
			GuiElement.drawTexture((int) x, (int) y, 0, 0, 8, 8, 8, 8);
			args.set(1, x + 10);
		}
	}

	@ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 2))
	public void axolotlclient$getCoords2(Args args) {
		float x = args.get(1);
		float y = args.get(2);
		if (AxolotlClient.CONFIG.showBadges.get()
			&& UserRequest.getOnline(axolotlclient$playerListEntry.getProfile().getId().toString())) {
			axolotlclient$client.getTextureManager().bind(AxolotlClient.badgeIcon);
			GuiElement.drawTexture((int) x, (int) y, 0, 0, 8, 8, 8, 8);
			args.set(1, x + 10);
		}
	}

	@Inject(method = "renderPing", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(int width, int x, int y, PlayerInfo entry, CallbackInfo ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (Tablist.getInstance().renderNumericPing(width, x, y, entry)) {
			ci.cancel();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z"))
	private boolean axolotlclient$showPlayerHeads$1(Minecraft instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isIntegratedServerRunning();
		}
		return false;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$2(Connection instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isEncrypted();
		}
		return false;
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;header:Lnet/minecraft/text/Text;"))
	private void axolotlclient$setRenderHeaderFooter(int width, Scoreboard scoreboard, ScoreboardObjective playerListScoreboardObjective, CallbackInfo ci) {
		if (!Tablist.getInstance().showHeader.get()) {
			header = null;
		}
		if (!Tablist.getInstance().showFooter.get()) {
			footer = null;
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getPlayer(Ljava/util/UUID;)Lnet/minecraft/entity/living/player/PlayerEntity;"))
	private UUID axolotlclient$makeStuff(UUID par1) {
		return Tablist.getInstance().alwaysShowHeadLayer.get() ? Minecraft.getInstance().player.getUuid() : par1;
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;renderPing(IIILnet/minecraft/client/network/PlayerInfo;)V"
		)
	)
	public void axolotlclient$renderWithoutObjective(
		int width, Scoreboard scoreboard, ScoreboardObjective playerListScoreboardObjective, CallbackInfo ci,
		@Local(ordinal = 1) int i, @Local(ordinal = 6) int n,
		@Local(ordinal = 13) int v, @Local(ordinal = 14) int y, @Local PlayerInfo playerListEntry2
	) {
		if (!BedwarsMod.getInstance().isEnabled() || !BedwarsMod.getInstance().isWaiting()) {
			return;
		}
		int startX = v + i + 1;
		int endX = startX + n;
		String render;
		try {
			if (playerListEntry2.getProfile().getName().contains(Formatting.OBFUSCATED.toString())) {
				return;
			}

			render = String.valueOf(HypixelAbstractionLayer.getPlayerLevel(playerListEntry2
					.getProfile().getId().toString().replace("-", ""),
				LevelHeadMode.BEDWARS));
		} catch (Exception e) {
			return;
		}
		this.axolotlclient$client.textRenderer.drawWithShadow(
			render,
			(float) (endX - this.axolotlclient$client.textRenderer.getWidth(render)) + 20,
			(float) y,
			-1
		);
	}

	@Inject(
		method = "renderDisplayScore",
		at = @At(
			value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I", ordinal = 1
		),
		cancellable = true
	)
	public void axolotlclient$renderCustomScoreboardObjective(
		ScoreboardObjective objective, int y, String player, int startX, int endX, PlayerInfo playerEntry, CallbackInfo ci
	) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}

		game.renderCustomScoreboardObjective(playerEntry.getProfile().getName(), objective, y, endX);
		ci.cancel();


	}

	@ModifyVariable(
		method = "render",
		at = @At(
			value = "STORE"
		),
		ordinal = 7
	)
	public int axolotlclient$changeWidth(int value) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			value -= 9;
		}
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().isWaiting()) {
			value += 20;
		}
		return value;
	}

	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerInfo playerEntry, CallbackInfoReturnable<String> cir) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}
		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null || !game.isStarted()) {
			return;
		}
		BedwarsPlayer player = game.getPlayer(playerEntry.getProfile().getName()).orElse(null);
		if (player == null) {
			return;
		}
		cir.setReturnValue(player.getTabListDisplay());
	}

	@ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false))
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
	public void axolotlclient$changeHeader(Text header, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.header = BedwarsMod.getInstance().getGame().get().getTopBarText();
		ci.cancel();
	}

	@Inject(method = "setFooter", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$changeFooter(Text footer, CallbackInfo ci) {
		if (!BedwarsMod.getInstance().inGame()) {
			return;
		}
		this.footer = BedwarsMod.getInstance().getGame().get().getBottomBarText();
		ci.cancel();
	}
}
