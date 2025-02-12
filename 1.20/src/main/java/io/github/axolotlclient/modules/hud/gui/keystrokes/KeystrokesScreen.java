package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class KeystrokesScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(Text.translatable("keystrokes.keys"));
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), width/2, 33/2-textRenderer.fontHeight/2, -1);
	}

	@Override
	protected void init() {

		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			AxolotlClientCommon.getInstance().saveConfig();
		}).positionAndSize(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen())
			.positionAndSize(width/2+4, height-33/2-10, 150, 20).build());

	}

	@Override
	public void closeScreen() {
		this.client.setScreen(this.screen);
		AxolotlClientCommon.getInstance().saveConfig();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
