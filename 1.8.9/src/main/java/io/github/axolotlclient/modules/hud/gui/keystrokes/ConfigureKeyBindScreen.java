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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ClickableWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.IntegerWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;

public class ConfigureKeyBindScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final Screen parent;
	private final KeystrokeHud hud;
	public final KeystrokeHud.Keystroke stroke;
	private final IntegerOption width;
	private final IntegerOption height;
	private final boolean isAddScreen;

	public ConfigureKeyBindScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke stroke, boolean isAddScreen) {
		super(I18n.translate("keystrokes.stroke.configure_stroke"));
		this.parent = parent;
		this.hud = hud;
		this.stroke = stroke;

		width = new IntegerOption("", stroke.getBounds().width(), v -> stroke.getBounds().width(v), 10, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> stroke.getBounds().height(v), 10, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	public void init() {

		int leftColX = super.width / 2 - 4 - 150;
		int leftColY = 36 + 5;
		int rightColX = super.width / 2 + 4;
		int rightColY = 36;

		addDrawableChild(new ClickableWidget(super.width / 2 - 100, rightColY, 200, 40, "") {
			@Override
			protected void drawWidget(int mouseX, int mouseY, float partialTick) {
				var rect = stroke.getRenderPosition();
				GlStateManager.pushMatrix();
				GlStateManager.translatef(getX(), getY(), 0);
				float scale = Math.min((float) getHeight() / rect.height(), (float) getWidth() / rect.width());
				GlStateManager.translatef(getWidth() / 2f - (rect.width() * scale) / 2f, 0, 0);
				GlStateManager.scalef(scale, scale, 1);
				GlStateManager.translatef(-rect.x(), -rect.y(), 0);
				DrawUtil.fillRect(rect, Colors.WHITE.withAlpha(128));
				stroke.render();
				GlStateManager.popMatrix();
			}
		}).active = false;
		leftColY += 48;
		rightColY += 48;

		ClickableWidget currentKey = addDrawable(textWidget(0, rightColY, super.width, 9, "", textRenderer));
		if (stroke.getKey() != null) {
			currentKey.setMessage(I18n.translate("keystrokes.stroke.key", GameOptions.getKeyName(stroke.getKey().getKeyCode())));
		} else {
			currentKey.setMessage("");
		}
		leftColY += 9 + 8;
		rightColY += 9 + 8;

		if (stroke.isLabelEditable()) {
			addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.label"), textRenderer));
			leftColY += 28;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;

			var label = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, supportsSynchronization ? 73 : 150, 20, ""));

			label.setText(stroke.getLabel());
			label.setChangedListener(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				ButtonWidget synchronizeButton = addDrawableChild(new VanillaButtonWidget(rightColX + 75 + 2, rightColY, 73, 20, I18n.translate("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? I18n.translate("options.on") : I18n.translate("options.off")), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(I18n.translate("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? I18n.translate("options.on") : I18n.translate("options.off")));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setText(stroke.getLabel());
					}
				}));
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
			}
			rightColY += 28;
		}
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.width"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, width));
		rightColY += 28;
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, I18n.translate("keystrokes.stroke.height"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, height));

		rightColY += 28;

		addDrawableChild(new VanillaButtonWidget(super.width / 2 - 150 - 4, rightColY, 150, 20, I18n.translate("keystrokes.stroke.configure_key"), b -> {
			minecraft.openScreen(new KeyBindSelectionScreen(this, stroke));
		}));
		addDrawableChild(new VanillaButtonWidget(super.width / 2 + 4, rightColY, 150, 20, I18n.translate("keystrokes.stroke.configure_position"), b -> {
			minecraft.openScreen(new KeystrokePositioningScreen(this, hud, stroke));
		}));


		if (isAddScreen) {
			ButtonWidget addButton = addDrawableChild(new VanillaButtonWidget(super.width / 2 - 150 - 4, super.height - 33 / 2 - 10, 150, 20, I18n.translate("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				closeScreen();
			}));
			addButton.active = stroke.getKey() != null;
		}
		addDrawableChild(new VanillaButtonWidget(isAddScreen ? super.width / 2 + 4 : super.width / 2 - 75, super.height - 33 / 2 - 10, 150, 20, isAddScreen ? I18n.translate("gui.cancel") : I18n.translate("gui.back"), b -> closeScreen()));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), super.width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}


	public void closeScreen() {
		minecraft.openScreen(parent);
		hud.saveKeystrokes();
	}

	private static ClickableWidget textWidget(int x, int y, int width, int height, String message, TextRenderer textRenderer) {
		return new ClickableWidget(x, y, width, height, message) {
			@Override
			public void render(int mouseX, int mouseY, float delta) {
				drawCenteredString(textRenderer, getMessage(), getX() + getWidth() / 2, getY() + getHeight() / 2 - textRenderer.fontHeight / 2, -1);
			}
		};
	}
}
