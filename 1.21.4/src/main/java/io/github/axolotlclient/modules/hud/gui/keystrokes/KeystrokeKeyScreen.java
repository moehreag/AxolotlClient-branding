package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeystrokeKeyScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	private final KeystrokeHud hud;
	public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	public KeystrokeHud.Keystroke selectedKey;
	public long lastKeySelection;
	private KeyBindsList keyBindsList;
	private Button resetButton;

	public KeystrokeKeyScreen(KeystrokeHud hud) {
		super(Component.translatable(""));
		this.keys = hud.keystrokes;
		this.hud = hud;
	}

	@Override
	protected void init() {

		this.resetButton = Button.builder(Component.translatable("controls.resetAll"), button -> {


			this.keyBindsList.resetMappingAndUpdateButtons();
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
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}
}
