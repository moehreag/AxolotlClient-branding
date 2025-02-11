package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeystrokeKeyScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
    private final Screen screen;
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	public KeystrokeHud.Keystroke selectedKey;
	public long lastKeySelection;
	private final KeyBindsList keyBindsList;
	private Button resetButton;

	public KeystrokeKeyScreen(KeystrokeHud hud, Screen screen) {
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
			keyBindsList.refreshEntries();
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
		keyBindsList.refreshEntries();
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.screen);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.selectedKey != null) {

			this.selectedKey = null;
			return true;
		} else {
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.selectedKey != null) {
			if (keyCode == 256) {
				//this.selectedKey.setKey(InputConstants.UNKNOWN);
			} else {
				//this.selectedKey.setKey(InputConstants.getKey(keyCode, scanCode));
			}

			this.selectedKey = null;
			this.lastKeySelection = Util.getMillis();
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
