package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ButtonWidget.class)
public class ButtonWidgetMixin {
	@Shadow
	public int x;

	@Shadow
	public int y;

	@Shadow
	protected int width;

	@Shadow
	protected int height;

	@Shadow
	public String message;

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;drawCenteredString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V"))
	private void drawScrollableString(ButtonWidget instance, TextRenderer renderer, String s, int centerX, int y_original, int color) {
		int left = x + 2;
		int right = x + width - 1 - 2;
		DrawUtil.drawScrollableText(Minecraft.getInstance().textRenderer, message, left, y, right, y + height, color);
	}
}
