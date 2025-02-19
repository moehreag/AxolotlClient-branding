/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.IntegerWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class ConfigureKeyBindScreen extends Screen {

	private final HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
	private final Screen parent;
	private final KeystrokeHud hud;
	public final KeystrokeHud.Keystroke stroke;
	private final IntegerOption width;
	private final IntegerOption height;
	private final boolean isAddScreen;
	private ButtonWidget addButton, synchronizeButton;
	private TextWidget currentKey;

	public ConfigureKeyBindScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke stroke, boolean isAddScreen) {
		super(Text.translatable("keystrokes.stroke.configure_stroke"));
		this.parent = parent;
		this.hud = hud;
		this.stroke = stroke;

		width = new IntegerOption("", stroke.getBounds().width(), v -> stroke.getBounds().width(v), 10, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> stroke.getBounds().height(v), 10, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	protected void init() {
		layout.addToHeader(getTitle(), textRenderer);

		var body = LinearLayoutWidget.createVertical().setSpacing(8);
		body.getDefaultSettings().alignVerticallyTop();
		var labelFrame = new FrameWidget();
		labelFrame.setMinWidth(super.width);
		labelFrame.add(new ClickableWidget(0, 0, 200, 40, Text.empty()) {
			@Override
			protected void drawWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				var rect = stroke.getRenderPosition();
				guiGraphics.getMatrices().push();
				guiGraphics.getMatrices().translate(getX(), getY(), 0);
				float scale = Math.min((float) getHeight() / rect.height(), (float) getWidth() / rect.width());
				guiGraphics.getMatrices().translate(getWidth() / 2f - (rect.width() * scale) / 2f, 0, 0);
				guiGraphics.getMatrices().scale(scale, scale, 1);
				guiGraphics.getMatrices().translate(-rect.x(), -rect.y(), 0);
				DrawUtil.fillRect(guiGraphics, rect, Colors.WHITE.withAlpha(128));
				stroke.render(guiGraphics);
				guiGraphics.getMatrices().pop();
			}

			@Override
			protected void updateNarration(NarrationMessageBuilder narrationElementOutput) {

			}
		}).active = false;
		body.add(labelFrame);
		currentKey = body.add(new TextWidget(Text.empty(), textRenderer));

		var optionsFrame = new FrameWidget();
		optionsFrame.setMinWidth(super.width);
		var contents = LinearLayoutWidget.createHorizontal().setSpacing(4);
		contents.getDefaultSettings().alignHorizontallyCenter();
		var names = contents.add(LinearLayoutWidget.createVertical().setSpacing(8));
		var options = contents.add(LinearLayoutWidget.createVertical().setSpacing(8));
		if (stroke.isLabelEditable()) {
			names.add(new TextWidget(150, 20, Text.translatable("keystrokes.stroke.label"), textRenderer));
			LinearLayoutWidget labelLayout;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;
			if (supportsSynchronization) {
				labelLayout = options.add(LinearLayoutWidget.createHorizontal()).setSpacing(4);
			} else {
				labelLayout = options;
			}
			var label = labelLayout.add(new TextFieldWidget(textRenderer, supportsSynchronization ? 73 : 150, 20, Text.empty()));
			label.setText(stroke.getLabel());
			label.setChangedListener(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				synchronizeButton = labelLayout.add(ButtonWidget.builder(Text.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonTexts.ON : CommonTexts.OFF), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(Text.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonTexts.ON : CommonTexts.OFF));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setText(stroke.getLabel());
					}
				}).width(73).build());
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
			}
		}
		names.add(new TextWidget(150, 20, Text.translatable("keystrokes.stroke.width"), textRenderer));
		options.add(new IntegerWidget(0, 0, 150, 20, width));
		names.add(new TextWidget(150, 20, Text.translatable("keystrokes.stroke.height"), textRenderer));
		options.add(new IntegerWidget(0, 0, 150, 20, height));
		optionsFrame.add(contents);
		body.add(optionsFrame);

		var buttonsFrame = new FrameWidget();
		buttonsFrame.setMinWidth(super.width);
		var row4 = LinearLayoutWidget.createHorizontal().setSpacing(8);
		row4.getDefaultSettings().alignHorizontallyCenter();
		row4.add(ButtonWidget.builder(Text.translatable("keystrokes.stroke.configure_key"), b -> {
			client.setScreen(new KeyBindSelectionScreen(this, stroke));
		}).width(150).build());
		row4.add(ButtonWidget.builder(Text.translatable("keystrokes.stroke.configure_position"), b -> {
			client.setScreen(new KeystrokePositioningScreen(this, hud, stroke));
		}).width(150).build());
		buttonsFrame.add(row4);
		body.add(buttonsFrame);

		layout.addToContents(body);

		var footer = LinearLayoutWidget.createHorizontal().setSpacing(8);
		if (isAddScreen) {
			addButton = footer.add(ButtonWidget.builder(Text.translatable("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				closeScreen();
			}).build());
			addButton.active = false;
		}
		footer.add(ButtonWidget.builder(isAddScreen ? CommonTexts.CANCEL : CommonTexts.BACK, b -> closeScreen()).build());

		layout.addToFooter(footer);

		layout.visitWidgets(this::addDrawableSelectableElement);
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
		if (isAddScreen && stroke.getKey() != null) {
			addButton.active = true;
		}
		currentKey.setWidth(super.width);
		if (stroke.getKey() != null) {
			currentKey.setMessage(Text.translatable("keystrokes.stroke.key", stroke.getKey().getKeyName(), Text.translatable(stroke.getKey().getTranslationKey())));
		} else {
			currentKey.setMessage(Text.empty());
		}
		if (synchronizeButton != null) {
			synchronizeButton.active = stroke.getKey() != null;
		}
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
		hud.saveKeystrokes();
	}
}
