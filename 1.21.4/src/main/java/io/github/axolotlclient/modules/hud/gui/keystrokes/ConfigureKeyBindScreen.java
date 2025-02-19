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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigureKeyBindScreen extends Screen {

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private final KeystrokeHud hud;
	public final KeystrokeHud.Keystroke stroke;
	private final IntegerOption width;
	private final IntegerOption height;
	private final boolean isAddScreen;
	private Button addButton, synchronizeButton;
	private StringWidget currentKey;

	public ConfigureKeyBindScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke stroke, boolean isAddScreen) {
		super(Component.translatable("keystrokes.stroke.configure_stroke"));
		this.parent = parent;
		this.hud = hud;
		this.stroke = stroke;

		width = new IntegerOption("", stroke.getBounds().width(), v -> stroke.getBounds().width(v), 10, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> stroke.getBounds().height(v), 10, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	protected void init() {
		layout.addTitleHeader(getTitle(), font);

		var body = LinearLayout.vertical().spacing(8);
		body.defaultCellSetting().alignVerticallyTop();
		var labelFrame = new FrameLayout();
		labelFrame.setMinWidth(super.width);
		labelFrame.addChild(new AbstractWidget(0, 0, 200, 40, Component.empty()) {
			@Override
			protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				var rect = stroke.getRenderPosition();
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate(getX(), getY(), 0);
				float scale = Math.min((float) getHeight() / rect.height(), (float) getWidth() / rect.width());
				guiGraphics.pose().translate(getWidth() / 2f - (rect.width() * scale) / 2f, 0, 0);
				guiGraphics.pose().scale(scale, scale, 1);
				guiGraphics.pose().translate(-rect.x(), -rect.y(), 0);
				DrawUtil.fillRect(guiGraphics, rect, Colors.WHITE.withAlpha(128));
				stroke.render(guiGraphics);
				guiGraphics.pose().popPose();
			}

			@Override
			protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

			}
		}).active = false;
		body.addChild(labelFrame);
		currentKey = body.addChild(new StringWidget(Component.empty(), font));

		var optionsFrame = new FrameLayout();
		optionsFrame.setMinWidth(super.width);
		var contents = LinearLayout.horizontal().spacing(4);
		contents.defaultCellSetting().alignHorizontallyCenter();
		var names = contents.addChild(LinearLayout.vertical().spacing(8));
		var options = contents.addChild(LinearLayout.vertical().spacing(8));
		if (stroke.isLabelEditable()) {
			names.addChild(new StringWidget(150, 20, Component.translatable("keystrokes.stroke.label"), font));
			LinearLayout labelLayout;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;
			if (supportsSynchronization) {
				labelLayout = options.addChild(LinearLayout.horizontal()).spacing(4);
			} else {
				labelLayout = options;
			}
			var label = labelLayout.addChild(new EditBox(font, supportsSynchronization ? 73 : 150, 20, Component.empty()));
			label.setValue(stroke.getLabel());
			label.setResponder(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				synchronizeButton = labelLayout.addChild(Button.builder(Component.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(Component.translatable("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setValue(stroke.getLabel());
					}
				}).width(73).build());
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
			}
		}
		names.addChild(new StringWidget(150, 20, Component.translatable("keystrokes.stroke.width"), font));
		options.addChild(new IntegerWidget(0, 0, 150, 20, width));
		names.addChild(new StringWidget(150, 20, Component.translatable("keystrokes.stroke.height"), font));
		options.addChild(new IntegerWidget(0, 0, 150, 20, height));
		optionsFrame.addChild(contents);
		body.addChild(optionsFrame);

		var buttonsFrame = new FrameLayout();
		buttonsFrame.setMinWidth(super.width);
		var row4 = LinearLayout.horizontal().spacing(8);
		row4.defaultCellSetting().alignHorizontallyCenter();
		row4.addChild(Button.builder(Component.translatable("keystrokes.stroke.configure_key"), b -> {
			minecraft.setScreen(new KeyBindSelectionScreen(this, stroke));
		}).width(150).build());
		row4.addChild(Button.builder(Component.translatable("keystrokes.stroke.configure_position"), b -> {
			minecraft.setScreen(new KeystrokePositioningScreen(this, hud, stroke));
		}).width(150).build());
		buttonsFrame.addChild(row4);
		body.addChild(buttonsFrame);

		layout.addToContents(body);

		var footer = LinearLayout.horizontal().spacing(8);
		if (isAddScreen) {
			addButton = footer.addChild(Button.builder(Component.translatable("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				onClose();
			}).build());
			addButton.active = false;
		}
		footer.addChild(Button.builder(isAddScreen ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK, b -> onClose()).build());

		layout.addToFooter(footer);

		layout.visitWidgets(this::addRenderableWidget);
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
			currentKey.setMessage(Component.translatable("keystrokes.stroke.key", stroke.getKey().getTranslatedKeyMessage(), Component.translatable(stroke.getKey().getName())));
		} else {
			currentKey.setMessage(Component.empty());
		}
		if (synchronizeButton != null) {
			synchronizeButton.active = stroke.getKey() != null;
		}
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
		hud.saveKeystrokes();
	}
}
