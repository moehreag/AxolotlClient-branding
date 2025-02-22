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

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ChatsSidebar;
import io.github.axolotlclient.api.FriendsScreen;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hypixel.HypixelAbstractionLayer;
import io.github.axolotlclient.modules.hypixel.HypixelMods;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

	protected GameMenuScreenMixin(Text title) {
		super(title);
	}

	private static boolean axolotlclient$hasModMenu() {
		return FabricLoader.getInstance().isModLoaded("modmenu") && !FabricLoader.getInstance().isModLoaded("axolotlclient-modmenu");
	}

	@Inject(method = "initWidgets", at = @At("TAIL"))
	private void axolotlclient$addSidebarButton(CallbackInfo ci) {
		if (API.getInstance().isSocketConnected()) {
			addDrawableChild(ButtonWidget.builder(Text.translatable("api.chats"),
					button -> client.setScreen(new ChatsSidebar(this)))
				.positionAndSize(10, height - 55, 75, 20).build());
			addDrawableChild(ButtonWidget.builder(Text.translatable("api.friends"),
					button -> client.setScreen(new FriendsScreen(this)))
				.positionAndSize(10, height - 30, 75, 20).build());
		}
	}

	@Redirect(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;createLinkConfirmationButton(Lnet/minecraft/text/Text;Ljava/lang/String;)Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 1))
	private ButtonWidget axolotlclient$addClientOptionsButton(GameMenuScreen instance, Text text, String string) {
		if (axolotlclient$hasModMenu())
			return createLinkConfirmationButton(text, string);

		return createButton(Text.translatable("title_short"), () -> new HudEditScreen(this));
	}

	@Shadow
	protected abstract ButtonWidget createLinkConfirmationButton(Text par1, String par2);

	@Shadow
	protected abstract ButtonWidget createButton(Text par1, Supplier<Screen> par2);

	@ModifyArg(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 1), index = 1)
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
