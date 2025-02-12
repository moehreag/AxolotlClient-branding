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

import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.ButtonWidgetTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin {

	@Shadow
	public int x;

	@Shadow
	public int y;

	@Shadow
	protected int width;

	@Shadow
	protected int height;

	@Shadow
	protected boolean hovered;

	@Shadow
	protected abstract int getYImage(boolean par1);

	@Shadow
	@Final
	protected static Identifier WIDGETS_LOCATION;

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;drawCenteredString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"))
	private void drawScrollableString(ButtonWidget instance, TextRenderer renderer, String message, int centerX, int y_original, int color) {
		int left = x + 2;
		int right = x + width - 1 - 2;
		DrawUtil.drawScrollableText(Minecraft.getInstance().textRenderer, message, left, y, right, y + height, color);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;drawTexture(IIIIII)V"))
	private void remove2Slice(ButtonWidget instance, int x, int y, int u, int v, int width, int height) {

	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;renderBackground(Lnet/minecraft/client/Minecraft;II)V"))
	private void addSlices(Minecraft minecraft, int i, int j, CallbackInfo ci) {
		Identifier tex = ButtonWidgetTextures.get(getYImage(hovered));
		DrawUtil.blitSprite(tex, x, y, width, height, new DrawUtil.NineSlice(200, 20, 3));
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);
	}
}
