package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class AddSpecialKeystrokeScreen extends Screen {
	private static final Text TITLE = Text.translatable("keystrokes.stroke.add.special");
	private final Screen lastScreen;
	public final KeystrokeHud hud;
	private SpecialKeystrokeSelectionList keyBindsList;
	public HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);

	public AddSpecialKeystrokeScreen(Screen lastScreen, KeystrokeHud hud) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.hud = hud;
	}

	@Override
	public void init() {
		layout.addToHeader(getTitle(), textRenderer);
		this.keyBindsList = this.layout.addToContents(new SpecialKeystrokeSelectionList(this, this.client));

		LinearLayoutWidget linearLayout = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
		linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
		this.layout.visitWidgets(this::addDrawableSelectableElement);
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		this.keyBindsList.setDimensionsWithLayout(this.width, this.layout);
	}

	@Override
	public void closeScreen() {
		client.setScreen(lastScreen);
	}
}
