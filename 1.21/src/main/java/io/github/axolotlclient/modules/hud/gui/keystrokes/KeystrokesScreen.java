package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class KeystrokesScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;
	public final HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
	private final KeyBindsList keyBindsList;
	private ButtonWidget resetButton;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(Text.translatable("keystrokes.keys"));
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
		this.keyBindsList = new KeyBindsList(this, keys);
	}

	@Override
	protected void init() {

		layout.addToHeader(getTitle(), textRenderer);
		layout.addToContents(keyBindsList);
		this.resetButton = ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			AxolotlClientCommon.getInstance().saveConfig();
		}).build();
		LinearLayoutWidget linearLayout = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
		linearLayout.add(this.resetButton);
		linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
		this.layout.visitWidgets(this::addDrawableSelectableElement);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		this.keyBindsList.setDimensionsWithLayout(this.width, this.layout);
		keyBindsList.reload(keys);
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
