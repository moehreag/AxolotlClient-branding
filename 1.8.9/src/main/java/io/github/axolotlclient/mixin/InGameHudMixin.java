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
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.*;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ScoreboardRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;color(FFFF)V", ordinal = 0))
	private void axolotlclient$onHudRender(float tickDelta, CallbackInfo ci) {
		HudManager.getInstance().render(MinecraftClient.getInstance(), tickDelta);
	}

	@Inject(method = "renderScoreboardObjective", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customScoreBoard(ScoreboardObjective objective, Window window, CallbackInfo ci) {
		ScoreboardHud hud = (ScoreboardHud) HudManager.getInstance().get(ScoreboardHud.ID);
		ScoreboardRenderEvent event = new ScoreboardRenderEvent(window, objective);
		Events.SCOREBOARD_RENDER_EVENT.invoker().invoke(event);
		if (event.isCancelled() || hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;showCrosshair()Z"))
	public boolean axolotlclient$noCrosshair(InGameHud instance) {
		CrosshairHud hud = (CrosshairHud) HudManager.getInstance().get(CrosshairHud.ID);
		if (hud.isEnabled()) {
			GlStateManager.blendFuncSeparate(775, 769, 1, 0);
			GlStateManager.enableAlphaTest();
			return false;
		}
		return showCrosshair();
	}

	@Shadow
	protected abstract boolean showCrosshair();

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;III)I", ordinal = 0))
	public int axolotlclient$actionBar(TextRenderer instance, String text, int x, int y, int color) {
		ActionBarHud hud = (ActionBarHud) HudManager.getInstance().get(ActionBarHud.ID);
		if (hud.isEnabled()) {
			hud.setActionBar(text, color);
			return 0;
		}
		return instance.draw(text, x, y, color);
	}

	@Inject(method = "renderBossBar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customBossBar(CallbackInfo ci) {
		BossBarHud hud = (BossBarHud) HudManager.getInstance().get(BossBarHud.ID);
		if (hud.isEnabled()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$customHotbar(Window window, float tickDelta, CallbackInfo ci) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			ci.cancel();
		}
	}

	@ModifyArgs(method = "renderHeldItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Ljava/lang/String;FFI)I"))
	public void axolotlclient$setItemNamePos(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(1, ((Integer) hud.getX()).floatValue() + (hud.getWidth() * hud.getScale()
				- MinecraftClient.getInstance().textRenderer.getStringWidth(args.get(0))) / 2);
			args.set(2, ((Integer) hud.getY()).floatValue() - 36
				+ (!MinecraftClient.getInstance().interactionManager.hasStatusBars() ? 14 : 0));
		}
	}

	@ModifyArgs(method = "renderHorseHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(IIIIII)V"))
	public void axolotlclient$moveHorseHealth(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(0, hud.getX());
			args.set(1, hud.getY() - 7);
		}
	}

	@ModifyArgs(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(IIIIII)V"))
	public void axolotlclient$moveXPBar(Args args) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			args.set(0, hud.getX());
			args.set(1, hud.getY() - 7);
		}
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHeight()I", ordinal = 1))
	public int axolotlclient$moveXPBarHeight(Window instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return instance.getHeight();
	}

	@Redirect(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getWidth()I"))
	public int axolotlclient$moveXPBarWidth(Window instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getX() * 2 + hud.getWidth();
		}
		return instance.getWidth();
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHeight()I"))
	public int axolotlclient$moveStatusBarsHeight(Window instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getY() + 22;
		}
		return instance.getHeight();
	}

	@Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getWidth()I"))
	public int axolotlclient$moveStatusBarsWidth(Window instance) {
		HotbarHUD hud = (HotbarHUD) HudManager.getInstance().get(HotbarHUD.ID);
		if (hud.isEnabled()) {
			return hud.getX() * 2 + hud.getWidth();
		}
		return instance.getWidth();
	}

	@Unique
	private static final Entity axolotlclient$noHungerEntityTM = new MinecartEntity(null);

	@ModifyVariable(
		method = "renderStatusBars",
		at = @At(
			value = "STORE"
		),
		ordinal = 18
	)
	public int axolotlclient$displayHardcoreHearts(int offset) {
		boolean hardcore = BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() && BedwarsMod.getInstance().hardcoreHearts.get() &&
			!BedwarsMod.getInstance().getGame().get().getSelf().isBed();
		return hardcore ? 5 : offset;
	}

	@ModifyVariable(
		method = "renderStatusBars",
		at = @At(
			value = "STORE"
		),
		ordinal = 0
	)
	public Entity axolotlclient$dontHunger(Entity normal) {
		if (normal == null && BedwarsMod.getInstance().isEnabled() &&
			BedwarsMod.getInstance().inGame() &&
			!BedwarsMod.getInstance().showHunger.get()) {
			return axolotlclient$noHungerEntityTM;
		}
		return normal;
	}

	@Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
	private void axolotlclient$removeVignette(float f, Window window, CallbackInfo ci){
		if(AxolotlClient.CONFIG.removeVignette.get()){
			ci.cancel();
		}
	}
}
