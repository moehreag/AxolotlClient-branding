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

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.User;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsGame;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsPlayer;
import io.github.axolotlclient.modules.hypixel.levelhead.LevelHeadMode;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.modules.tablist.Tablist;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

	@Shadow
	private Text header;
	@Shadow
	private Text footer;
	@Unique
	private GameProfile axolotlclient$profile;

	@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$nickHider(PlayerListEntry playerEntry, CallbackInfoReturnable<Text> cir) {
		assert MinecraftClient.getInstance().player != null;
		if (playerEntry.getProfile().equals(MinecraftClient.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOwnName.get()) {
			cir.setReturnValue(this.applyGameModeFormatting(playerEntry, Text.literal(NickHider.getInstance().hiddenNameSelf.get())));
		} else if (!playerEntry.getProfile().equals(MinecraftClient.getInstance().player.getGameProfile())
			&& NickHider.getInstance().hideOtherNames.get()) {
			cir.setReturnValue(this.applyGameModeFormatting(playerEntry, Text.literal(NickHider.getInstance().hiddenNameOthers.get())));
		}
	}

	@Shadow
	protected abstract Text applyGameModeFormatting(PlayerListEntry entry, MutableText name);

	@Shadow
	@Final
	private MinecraftClient client;

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;"))
	private PlayerListEntry axolotlclient$getPlayer(PlayerListEntry playerEntry) {
		axolotlclient$profile = playerEntry.getProfile();
		return playerEntry;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I"))
	private int axolotlclient$moveName(TextRenderer instance, StringVisitable text) {
		if (axolotlclient$profile != null && AxolotlClient.CONFIG.showBadges.get() && User.getOnline(axolotlclient$profile.getId().toString()))
			return instance.getWidth(text) + 10;
		return instance.getWidth(text);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
	public int axolotlclient$moveName2(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int color) {
		if (axolotlclient$profile != null && AxolotlClient.CONFIG.showBadges.get() && User.getOnline(axolotlclient$profile.getId().toString())) {
			RenderSystem.setShaderColor(1, 1, 1, 1);

			instance.drawTexture(AxolotlClient.badgeIcon, (int) x, (int) y, 8, 8, 0, 0, 8, 8, 8, 8);

			x += 9;
		}
		axolotlclient$profile = null;
		return instance.drawShadowedText(renderer, text, x, y, color);
	}

	@ModifyArg(method = "getPlayerName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;applyGameModeFormatting(Lnet/minecraft/client/network/PlayerListEntry;Lnet/minecraft/text/MutableText;)Lnet/minecraft/text/Text;"), index = 1)
	private MutableText axolotlclient$hideNames(MutableText name) {
		if (NickHider.getInstance().hideOwnName.get()) {
			return Text.literal(NickHider.getInstance().hiddenNameSelf.get());
		}
		if (NickHider.getInstance().hideOtherNames.get()) {
			return Text.literal(NickHider.getInstance().hiddenNameOthers.get());
		}
		return name;
	}

	@Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$numericalPing(GuiGraphics graphics, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().blockLatencyIcon() && (BedwarsMod.getInstance().isWaiting() || BedwarsMod.getInstance().inGame())) {
			ci.cancel();
		} else if (Tablist.getInstance().renderNumericPing(graphics, width, x, y, entry)) {
			ci.cancel();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
	private boolean showPlayerHeads$1(MinecraftClient instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isInSingleplayer();
		}
		return false;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
	private boolean axolotlclient$showPlayerHeads$1(ClientConnection instance) {
		if (Tablist.getInstance().showPlayerHeads.get()) {
			return instance.isEncrypted();
		}
		return false;
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;header:Lnet/minecraft/text/Text;"))
	private void axolotlclient$setRenderHeaderFooter(GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
		if (!Tablist.getInstance().showHeader.get()) {
			header = null;
		}
		if (!Tablist.getInstance().showFooter.get()) {
			footer = null;
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/util/Identifier;IIIZZ)V"), index = 5)
	private boolean axolotlclient$renderHatLayer(boolean drawHat) {
		return drawHat || Tablist.getInstance().alwaysShowHeadLayer.get();
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderLatencyIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/network/PlayerListEntry;)V"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void axolotlclient$renderWithoutObjective(
		GuiGraphics graphics, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective, CallbackInfo ci,
		List list, int i, int j, int l, int m, int k,
		boolean bl, int n, int o, int p, int q, int r, List list2, int t, int u, int s, int v, int y, int z, PlayerListEntry playerListEntry2
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
				LevelHeadMode.BEDWARS.toString()));
		} catch (Exception e) {
			return;
		}
		graphics.drawShadowedText(client.textRenderer,
			render,
			(endX - this.client.textRenderer.getWidth(render)) + 20,
			y,
			-1
		);
	}

	@Inject(
		method = "renderScoreboardObjective",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
		),
		cancellable = true
	)
	private void axolotlclient$renderCustomScoreboardObjective(
		ScoreboardObjective objective, int y, String player, int startX, int endX, UUID uuid, GuiGraphics graphics, CallbackInfo ci
	) {
		if (!BedwarsMod.getInstance().isEnabled()) {
			return;
		}

		BedwarsGame game = BedwarsMod.getInstance().getGame().orElse(null);
		if (game == null) {
			return;
		}

		game.renderCustomScoreboardObjective(graphics, player, objective, y, endX);

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

	@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getPlayerName(PlayerListEntry playerEntry, CallbackInfoReturnable<Text> cir) {
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
		cir.setReturnValue(Text.of(player.getTabListDisplay()));
	}

	@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
	public List<PlayerListEntry> axolotlclient$overrideSortedPlayers(List<PlayerListEntry> original) {
		if (!BedwarsMod.getInstance().inGame()) {
			return original;
		}
		List<PlayerListEntry> players = BedwarsMod.getInstance().getGame().get().getTabPlayerList(original);
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
