package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;

public class AddSpecialKeystrokeScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private final Screen lastScreen;
	public final KeystrokeHud hud;
	private SpecialKeystrokeSelectionList list;

	public AddSpecialKeystrokeScreen(Screen lastScreen, KeystrokeHud hud) {
		super("keystrokes.stroke.add.special");
		this.lastScreen = lastScreen;
		this.hud = hud;
	}

	@Override
	public void init() {
		super.init();
		list = addChild(new SpecialKeystrokeSelectionList(this, this.client));

		addDrawableChild(new ButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, ScreenTexts.DONE, button -> this.onClose()));
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		list.render(graphics, mouseX, mouseY, delta);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void onClose() {
		client.openScreen(lastScreen);
	}
}
