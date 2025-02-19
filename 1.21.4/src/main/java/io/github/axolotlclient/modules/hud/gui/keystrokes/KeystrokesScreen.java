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
		if (hud.keystrokes == null) {
			hud.setKeystrokes();
		}
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
		this.keyBindsList = new KeyBindsList(this);
	}

	@Override
	protected void init() {
		layout.addTitleHeader(getTitle(), font);
		layout.addToContents(keyBindsList);
		this.resetButton = Button.builder(Component.translatable("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload();
			hud.saveKeystrokes();
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
		keyBindsList.reload();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.screen);
		hud.saveKeystrokes();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
