package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class KeyBindSelectionScreen extends Screen {
	private static final Text TITLE = Text.translatable("keystrokes.strokes.keys.select");
	private final Screen lastScreen;
	public final KeystrokeHud.Keystroke stroke;

	public KeyBindSelectionScreen(Screen lastScreen, KeystrokeHud.Keystroke stroke) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.stroke = stroke;
	}

	@Override
	public void init() {
		addDrawableChild(new KeyBindSelectionList(this, this.client, key -> stroke.setKey(key)));

		addDrawableChild(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen())
			.positionAndSize(width/2-75, height-33/2-10, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), width/2, 33/2-textRenderer.fontHeight/2, -1);
	}

	@Override
	public void closeScreen() {
		client.setScreen(lastScreen);
	}
}
