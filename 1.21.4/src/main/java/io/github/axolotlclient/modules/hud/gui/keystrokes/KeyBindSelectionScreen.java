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

import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeyBindSelectionScreen extends Screen {
	private static final Component TITLE = Component.translatable("keystrokes.strokes.keys.select");
	private final Screen lastScreen;
	public final KeystrokeHud.Keystroke stroke;
	private KeyBindSelectionList keyBindsList;
	public HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public KeyBindSelectionScreen(Screen lastScreen, KeystrokeHud.Keystroke stroke) {
		super(TITLE);
		this.lastScreen = lastScreen;
		this.stroke = stroke;
	}

	@Override
	public void init() {
		layout.addTitleHeader(getTitle(), getFont());
		this.keyBindsList = this.layout.addToContents(new KeyBindSelectionList(this, this.minecraft, key -> {
			stroke.setKey(key);
		}));

		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
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
	public void onClose() {
		minecraft.setScreen(lastScreen);
	}
}
