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

import java.util.List;

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
			hud.saveKeystrokes();
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
		hud.saveKeystrokes();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
