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
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.PotionsHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ActionBarHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.CrosshairHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ScoreboardHud;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ScoreboardRenderEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

	@Shadow private @Nullable Component overlayMessageString;

	@Shadow private int overlayMessageTime;

	@Shadow @Final private Minecraft minecraft;

	@Shadow @Final private ChatComponent chat;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void onHudRender(Minecraft client, CallbackInfo ci, @Local(ordinal = 1) LayeredDraw list) {
		list.add((guiGraphics, deltaTracker) -> {
			if (!client.options.hideGui) {
				HudManager.getInstance().render(guiGraphics, deltaTracker);
			}
		});
	}

	@Inject(method = "renderEffects", at = @At(value = "HEAD"))
	private void axolotlclient$onHudRender(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		if (!minecraft.options.hideGui) {
			HudManager.getInstance().render(graphics, tracker);
		}
	}

	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderStatusEffect(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCrosshair(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (minecraft.gui.getDebugOverlay().showDebugScreen() && !hud.overridesF3()) {
				return;
			}
			ci.cancel();
		}
	}

	@Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderScoreboard(GuiGraphics graphics, Objective objective, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().invoke(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderOverlayMessage", at = @At(value = "HEAD"))
	public void axolotlclient$clearActionBar(GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (overlayMessageString == null || overlayMessageTime <= 0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@WrapOperation(method = "renderOverlayMessage", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I"))
	public int axolotlclient$getActionBar(GuiGraphics instance, Font font, Component text, int x, int y, int width, int color, Operation<Integer> original) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			hud.setActionBar(text, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return original.call(instance, font, text, x, y, width, color);
		}
	}

	@WrapOperation(method = "renderHearts", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V"))
	public void axolotlclient$displayHardcoreHearts(Gui instance, GuiGraphics graphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half, Operation<Void> original) {
		boolean hardcoreMod = BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() &&
							  BedwarsMod.getInstance().hardcoreHearts.get() &&
							  !BedwarsMod.getInstance().getGame().get().getSelf().isBed();
		original.call(instance, graphics, type, x, y, hardcoreMod || hardcore, blinking, half);
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
	public void axolotlclient$dontHunger(Gui instance, GuiGraphics graphics, Player player, int y, int x, Operation<Void> original) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().showHunger.get()) {
			return;
		}
		original.call(instance, graphics, player, y, x);
	}

	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$removeVignette(GuiGraphics graphics, Entity entity, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.removeVignette.get()) {
			ci.cancel();
		}
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
	private void axolotlclient$dontShowArmor(GuiGraphics graphics, Player player, int y, int uncappedMaxHealth, int cappedMaxHealth, int x, Operation<Void> original) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().displayArmor.get()) {
			return;
		}
		original.call(graphics, player, y, uncappedMaxHealth, cappedMaxHealth, x);
	}
}
