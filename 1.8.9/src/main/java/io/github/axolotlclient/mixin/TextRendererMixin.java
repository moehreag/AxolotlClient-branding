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
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin {

	// Pain at its finest

	private final Identifier texture_g = new Identifier("axolotlclient", "textures/font/g_breve_capital.png");
	@Shadow
	public int fontHeight;
	@Shadow
	private float r;
	@Shadow
	private float g;
	@Shadow
	private float b;
	@Shadow
	private float a;
	@Shadow
	private float x;
	@Shadow
	private float y;
	private boolean shouldHaveShadow;

	@Inject(method = "drawLayer(Ljava/lang/String;FFIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TextRenderer;drawLayer(Ljava/lang/String;Z)V"))
	public void axolotlclient$getData(String text, float x, float y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
		if (text != null) {
			shouldHaveShadow = shadow;
		}
	}

	@Inject(method = "drawGlyph", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$gBreve(char c, boolean bl, CallbackInfoReturnable<Float> cir) {
		if (c == 'Ğ' && !Minecraft.getInstance().options.forceUnicodeFont) {
			Minecraft.getInstance().getTextureManager().bind(texture_g);

			if (!bl || shouldHaveShadow) {
				GlStateManager.color4f(this.r / 4, this.g / 4, this.b / 4, this.a);
				drawTexture(this.x + 1, this.y - this.fontHeight + 7);
			}

			GlStateManager.color4f(this.r, this.g, this.b, this.a);
			drawTexture(this.x, this.y - this.fontHeight + 6);

			GlStateManager.color4f(this.r, this.g, this.b, this.a);
			cir.setReturnValue(7.0F);
		}
	}

	private void drawTexture(float x, float y) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(x, y + 10, 0.0).texture(0, 1).nextVertex();
		bufferBuilder.vertex((x + 5), (y + 10), 0.0).texture(1, 1).nextVertex();
		bufferBuilder.vertex((x + 5), y, 0.0).texture(1, 0).nextVertex();
		bufferBuilder.vertex(x, y, 0.0).texture(0, 0).nextVertex();
		tessellator.end();
	}

	@Inject(method = "getWidth(C)I", at = @At(value = "HEAD"), cancellable = true)
	public void axolotlclient$modifiedCharWidth(char c, CallbackInfoReturnable<Integer> cir) {
		if (c == 'Ğ' && !Minecraft.getInstance().options.forceUnicodeFont) {
			cir.setReturnValue(7);
		}
	}
}
