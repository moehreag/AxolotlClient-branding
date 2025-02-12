package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeystrokesScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
    private final Screen screen;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final KeyBindsList keyBindsList;
	private Button resetButton;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(Component.translatable("keystrokes.keys"));
		this.keys = hud.keystrokes;
		this.hud = hud;
        this.screen = screen;
        this.keyBindsList = new KeyBindsList(this, keys);
	}

	@Override
	protected void init() {

		layout.addTitleHeader(getTitle(), font);
		layout.addToContents(keyBindsList);
		this.resetButton = Button.builder(Component.translatable("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			AxolotlClientCommon.getInstance().saveConfig();
		}).build();
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		linearLayout.addChild(this.resetButton);
		linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		this.keyBindsList.updateSize(this.width, this.layout);
		keyBindsList.reload(keys);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.screen);
		AxolotlClientCommon.getInstance().saveConfig();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
