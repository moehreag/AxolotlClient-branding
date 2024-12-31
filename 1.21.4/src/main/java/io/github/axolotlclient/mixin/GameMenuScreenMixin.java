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

import java.util.Objects;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.APIOptions;
import io.github.axolotlclient.api.ChatsSidebar;
import io.github.axolotlclient.api.FriendsScreen;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Component title) {
		super(title);
	}

	@Unique
	private static boolean axolotlclient$hasModMenu() {
		return FabricLoader.getInstance().isModLoaded("modmenu") && !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "createPauseMenu", at = @At("TAIL"))
	private void axolotlclient$addButtons(CallbackInfo ci, @Local GridLayout widget) {
		if (API.getInstance().isAuthenticated()) {
			int buttonY = height-30;
			if (APIOptions.getInstance().addShortcutButtons.get()) {
				addRenderableWidget(Button.builder(Component.translatable("api.friends"),
						button -> minecraft.setScreen(new FriendsScreen(this)))
					.bounds(10, buttonY, 75, 20).build());
				buttonY -= 25;
			}
			addRenderableWidget(Button.builder(Component.translatable("api.chats"),
				button -> minecraft.setScreen(new ChatsSidebar(this)))
				.bounds(10, buttonY, 75, 20).build());
		}
		if (!axolotlclient$hasModMenu()) {
			addRenderableWidget(new Button(widget.getX() + widget.getWidth(),
				widget.getY() + 50, 20, 20,
				Component.empty(),
				button -> minecraft.setScreen(new HudEditScreen(this)), Supplier::get) {
				@Override
				public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
					super.renderWidget(graphics, mouseX, mouseY, delta);
					graphics.blit(RenderType::guiTextured, AxolotlClient.badgeIcon, this.getX() + 2, this.getY() + 2, 0, 0, this.width - 4, this.height - 4, this.width - 4, this.height - 4);
				}
			});
		}
	}

	@ModifyArg(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", ordinal = 1), index = 1)
	private Button.OnPress axolotlclient$clearFeatureRestrictions(Button.OnPress onPress) {
		return (buttonWidget) -> {
			if (Objects.equals(HypixelMods.getInstance().cacheMode.get(),
				HypixelMods.HypixelCacheMode.ON_CLIENT_DISCONNECT)) {
				HypixelAbstractionLayer.clearPlayerData();
			}
			onPress.onPress(buttonWidget);
		};
	}
}
