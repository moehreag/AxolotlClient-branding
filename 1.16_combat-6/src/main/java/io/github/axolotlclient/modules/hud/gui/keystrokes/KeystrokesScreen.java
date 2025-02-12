package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class KeystrokesScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super("keystrokes.keys");
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		super.init();
		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(new ButtonWidget(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20,
			new TranslatableText("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			AxolotlClientCommon.getInstance().saveConfig();
		}));
		addDrawableChild(new ButtonWidget(width / 2 + 4, height - 33 / 2 - 10, 150, 20, ScreenTexts.DONE, button -> this.onClose()));

	}

	@Override
	public void onClose() {
		this.client.openScreen(this.screen);
		AxolotlClientCommon.getInstance().saveConfig();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
