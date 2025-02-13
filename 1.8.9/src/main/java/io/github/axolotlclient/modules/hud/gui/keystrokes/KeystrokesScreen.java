package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public class KeystrokesScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(I18n.translate("keystrokes.keys"));
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {

		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(new VanillaButtonWidget(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20, I18n.translate("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			hud.saveKeystrokes();
		}));
		addDrawableChild(new VanillaButtonWidget(width / 2 + 4, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.done"), button -> this.closeScreen()));

	}


	public void closeScreen() {
		this.minecraft.openScreen(this.screen);
		hud.saveKeystrokes();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
