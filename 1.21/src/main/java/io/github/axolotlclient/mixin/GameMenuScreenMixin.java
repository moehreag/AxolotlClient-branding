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

import java.util.Objects;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.FriendsSidebar;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	@Unique
	private static boolean axolotlclient$hasModMenu() {
		return QuiltLoader.isModLoaded("modmenu") && !QuiltLoader.isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void axolotlclient$addButtons(CallbackInfo ci, @Local GridWidget widget) {
		if (API.getInstance().isSocketConnected()) {
			addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.friends"),
				button -> MinecraftClient.getInstance().setScreen(new FriendsSidebar(this))).positionAndSize(10, height - 30, 75, 20).build());
		}
		if (!axolotlclient$hasModMenu()) {
			addDrawableSelectableElement(new ButtonWidget(widget.getX() + widget.getWidth(),
				widget.getY()+50, 20, 20,
				Text.empty(),
				button -> client.setScreen(new HudEditScreen(this)), Supplier::get){
				@Override
				public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
					super.drawWidget(graphics, mouseX, mouseY, delta);
					graphics.drawTexture(AxolotlClient.badgeIcon, this.getX()+2, this.getY()+2, 0, 0, this.width-4, this.height-4, this.width-4, this.height-4);
				}
			});
		}
	}

	@ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/button/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/button/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/button/ButtonWidget$Builder;", ordinal = 1), index = 1)
	private ButtonWidget.PressAction axolotlclient$clearFeatureRestrictions(ButtonWidget.PressAction onPress) {
		return (buttonWidget) -> {
			if (Objects.equals(HypixelMods.getInstance().cacheMode.get(),
				HypixelMods.HypixelCacheMode.ON_CLIENT_DISCONNECT)) {
				HypixelAbstractionLayer.clearPlayerData();
			}
			onPress.onPress(buttonWidget);
		};
	}
}
