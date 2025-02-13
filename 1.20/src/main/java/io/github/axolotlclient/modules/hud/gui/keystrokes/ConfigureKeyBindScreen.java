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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class ConfigureKeyBindScreen extends Screen {

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

		width = new IntegerOption("", stroke.getBounds().width(), v -> {
			stroke.getBounds().width(v);
		}, 1, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> {
			stroke.getBounds().height(v);
		}, 1, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	protected void init() {

		int leftColX = super.width / 2 - 4 - 150;
		int leftColY = 36 + 5;
		int rightColX = super.width / 2 + 4;
		int rightColY = 36;

		addDrawableChild(new ClickableWidget(super.width / 2 - 100, rightColY, 200, 40, Text.empty()) {
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
		leftColY += 48;
		rightColY += 48;

		currentKey = addDrawable(new TextWidget(0, rightColY, super.width, 9, Text.empty(), textRenderer));
		if (stroke.getKey() != null) {
			currentKey.setMessage(Text.translatable("keystrokes.stroke.key", stroke.getKey().getKeyName()));
		} else {
			currentKey.setMessage(Text.empty());
		}
		leftColY += 9 + 8;
		rightColY += 9 + 8;

		if (stroke.isLabelEditable()) {
			addDrawableChild(new TextWidget(leftColX, leftColY, 150, 20, Text.translatable("keystrokes.stroke.label"), textRenderer));
			leftColY += 28;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;

			var label = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, supportsSynchronization ? 73 : 150, 20, Text.empty()));

			label.setText(stroke.getLabel());
			label.setChangedListener(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				synchronizeButton = addDrawableChild(ButtonWidget.builder(Text.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonTexts.ON : CommonTexts.OFF), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(Text.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonTexts.ON : CommonTexts.OFF));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setText(stroke.getLabel());
					}
				}).positionAndSize(rightColX + 75 + 2, rightColY, 73, 20).build());
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
			}
			rightColY += 28;
		}
		addDrawableChild(new TextWidget(leftColX, leftColY, 150, 20, Text.translatable("keystrokes.stroke.width"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, width));
		rightColY += 28;
		addDrawableChild(new TextWidget(leftColX, leftColY, 150, 20, Text.translatable("keystrokes.stroke.height"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, height));

		rightColY += 28;

		addDrawableChild(ButtonWidget.builder(Text.translatable("keystrokes.stroke.configure_key"), b -> {
			client.setScreen(new KeyBindSelectionScreen(this, stroke));
		}).positionAndSize(super.width / 2 - 150 - 4, rightColY, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("keystrokes.stroke.configure_position"), b -> {
			client.setScreen(new KeystrokePositioningScreen(this, hud, stroke));
		}).positionAndSize(super.width / 2 + 4, rightColY, 150, 20).build());


		if (isAddScreen) {
			addButton = addDrawableChild(ButtonWidget.builder(Text.translatable("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				closeScreen();
			}).positionAndSize(super.width / 2 - 150 - 4, super.height - 33 / 2 - 10, 150, 20).build());
			addButton.active = stroke.getKey() != null;
		}
		addDrawableChild(ButtonWidget.builder(isAddScreen ? CommonTexts.CANCEL : CommonTexts.BACK, b -> closeScreen())
			.positionAndSize(isAddScreen ? super.width / 2 + 4 : super.width / 2 - 75, super.height - 33 / 2 - 10, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), super.width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
		hud.saveKeystrokes();
	}
}
