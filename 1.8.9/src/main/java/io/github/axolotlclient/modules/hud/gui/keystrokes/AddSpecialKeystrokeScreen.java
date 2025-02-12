package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public class AddSpecialKeystrokeScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private static final String TITLE = I18n.translate("keystrokes.stroke.add.special");
	private final Screen lastScreen;
	public final KeystrokeHud hud;

	public AddSpecialKeystrokeScreen(Screen lastScreen, KeystrokeHud hud) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.hud = hud;
	}

	@Override
	public void init() {
		addDrawableChild(new SpecialKeystrokeSelectionList(this, this.minecraft));

		addDrawableChild(new VanillaButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.done"), button -> minecraft.openScreen(lastScreen)));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}
}
