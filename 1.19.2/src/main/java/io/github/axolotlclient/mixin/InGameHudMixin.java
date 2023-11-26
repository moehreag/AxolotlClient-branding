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
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
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

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/util/math/MatrixStack;)V"))
	private void axolotlclient$onHudRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (!MinecraftClient.getInstance().options.hudHidden) {
			HudManager.getInstance().render(matrices, tickDelta);
		}
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderStatusEffect(MatrixStack matrices, CallbackInfo ci) {
		PotionsHud hud = (PotionsHud) HudManager.getInstance().get(PotionsHud.ID);
		if (hud != null && hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (MinecraftClient.getInstance().options.debugEnabled && !hud.overridesF3()) {
				return;
			}
			ci.cancel();
		}
	}

	@Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$renderScoreboard(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().invoke(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;overlayMessage:Lnet/minecraft/text/Text;", ordinal = 0))
	public void axolotlclient$clearActionBar(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			if (overlayMessage == null || overlayRemaining <= 0 && hud.getActionBar() != null) {
				hud.setActionBar(null, 0);
			}
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0))
	public int axolotlclient$getActionBar(TextRenderer instance, MatrixStack matrices, Text message, float x, float y, int color) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud != null && hud.isEnabled()) {
			hud.setActionBar(message, color);// give ourselves the correct values
			return 0; // Doesn't matter since return value is not used
		} else {
			return instance.draw(matrices, message, x, y, color);
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customHotbar(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			ci.cancel();
		}
	}

	@ModifyArgs(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
	public void axolotlclient$setItemNamePos(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(2, ((Integer) hud.getX()).floatValue() + ((hud.getWidth() * hud.getScale())
				- MinecraftClient.getInstance().textRenderer.getWidth((StringVisitable) args.get(1))) / 2);
			args.set(3, ((Integer) hud.getY()).floatValue() - 36
				+ (!MinecraftClient.getInstance().interactionManager.hasStatusBars() ? 14 : 0));
		}
	}

	@ModifyArgs(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
	public void axolotlclient$moveHorseHealth(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(1, hud.getX());
			args.set(2, hud.getY() - 7);
		}
	}

	@ModifyArgs(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
	public void axolotlclient$moveXPBar(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(1, hud.getX());
			args.set(2, hud.getY() - 7);
		}
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I"))
	public int axolotlclient$moveXPBarHeight(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I"))
	public int axolotlclient$moveXPBarWidth(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getX() * 2 + hud.getWidth();
		}
		return scaledWidth;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledHeight:I"))
	public int axolotlclient$moveStatusBarsHeight(InGameHud instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return scaledHeight;
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;scaledWidth:I"))
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
		ordinal = 7
	)
	public int axolotlclient$displayHardcoreHearts(int v) {
		boolean hardcore = BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed();
		return (hardcore ? 9 * 5 : v);
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
	private void axolotlclient$removeVignette(Entity entity, CallbackInfo ci) {
		if (AxolotlClient.CONFIG.removeVignette.get()) {
			ci.cancel();
		}
	}
}
