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

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.PotionsHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ActionBarHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.CrosshairHud;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.HotbarHUD;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.ScoreboardHud;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ScoreboardRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.in_game.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow
	private int scaledHeight;

	@Shadow
	private int scaledWidth;

	@Shadow
	private @Nullable Text overlayMessage;

	@Shadow
	private int overlayRemaining;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void axolotlclient$onHudRender(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
		if (!MinecraftClient.getInstance().options.hudHidden) {
			HudManager.getInstance().render(graphics, tickDelta);
		}
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderStatusEffect(GuiGraphics graphics, CallbackInfo ci) {
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCrosshair(GuiGraphics graphics, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (MinecraftClient.getInstance().inGameHud.method_53531().chartsVisible() && !hud.overridesF3()) {
				return;
			}
			ci.cancel();
		}
	}

	@Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderScoreboard(GuiGraphics graphics, ScoreboardObjective objective, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().invoke(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;overlayMessage:Lnet/minecraft/text/Text;", ordinal = 0))
	public void axolotlclient$clearActionBar(GuiGraphics graphics, float tickDelta, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (overlayMessage == null || overlayRemaining <= 0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0))
	public int axolotlclient$getActionBar(GuiGraphics instance, TextRenderer renderer, Text text, int x, int y, int color) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			hud.setActionBar(text, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return instance.drawShadowedText(renderer, text, x, y, color);
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customHotbar(float tickDelta, GuiGraphics graphics, CallbackInfo ci) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			ci.cancel();
		}
	}

	@ModifyArgs(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawShadowedText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
	public void axolotlclient$setItemNamePos(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(2, hud.getX() + (int) ((hud.getWidth() * hud.getScale())
				- MinecraftClient.getInstance().textRenderer.getWidth((StringVisitable) args.get(1))) / 2);
			args.set(3, hud.getY() - 36
				+ (!MinecraftClient.getInstance().interactionManager.hasStatusBars() ? 14 : 0));
		}
	}

	@ModifyArgs(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
	public void axolotlclient$moveHorseHealth(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(1, hud.getX());
			args.set(2, hud.getY() - 7);
		}
	}

	@ModifyArgs(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"))
	public void axolotlclient$moveXPBar(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(1, hud.getX());
			args.set(2, hud.getY() - 7);
		}
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;scaledHeight:I"))
	public int axolotlclient$moveXPBarHeight(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;scaledWidth:I"))
	public int axolotlclient$moveXPBarWidth(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getX() * 2 + hud.getWidth();
		}
		return scaledWidth;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;scaledHeight:I"))
	public int axolotlclient$moveStatusBarsHeight(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/in_game/InGameHud;scaledWidth:I"))
	public int axolotlclient$moveStatusBarsWidth(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getX() * 2 + hud.getWidth();
		}
		return scaledWidth;
	}

	@ModifyVariable(
		method = "renderHealthBar",
		at = @At(
			value = "STORE"
		),
		ordinal = 13
	)
	public int axolotlclient$displayHardcoreHearts(int v) {
		boolean hardcore = BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed();
		return hardcore ? 9 * 5 : v;
	}

	@ModifyVariable(
		method = "renderStatusBars",
		at = @At(
			value = "STORE"
		), ordinal = 15
	)
	public int axolotlclient$dontHunger(int heartCount) {
		if (heartCount == 0 && BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().showHunger.get()) {
			return 3;
		}
		return heartCount;
	}

	@Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$removeVignette(GuiGraphics graphics, Entity entity, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.removeVignette.get()) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "renderStatusBars", at = @At("STORE"), ordinal = 12)
	private int axolotlclient$dontShowArmor(int armorValue) {
		if (BedwarsMod.getInstance().isEnabled() && BedwarsMod.getInstance().inGame() && !BedwarsMod.getInstance().displayArmor.get()) {
			return 0;
		}
		return armorValue;
	}
}
