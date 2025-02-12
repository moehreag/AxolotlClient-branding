package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class KeyBindSelectionScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private static final String TITLE = "keystrokes.strokes.keys.select";
	private final Screen lastScreen;
	public final KeystrokeHud.Keystroke stroke;

	public KeyBindSelectionScreen(Screen lastScreen, KeystrokeHud.Keystroke stroke) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.stroke = stroke;
	}

	@Override
	public void init() {
		super.init();
		addDrawableChild(new KeyBindSelectionList(this, this.client, key -> stroke.setKey(key)));

		addDrawableChild(new ButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, ScreenTexts.DONE, button -> this.onClose()));
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void onClose() {
		client.openScreen(lastScreen);
	}
}
