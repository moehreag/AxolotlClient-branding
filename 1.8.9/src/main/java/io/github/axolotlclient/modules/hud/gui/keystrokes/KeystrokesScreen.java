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

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;

public class KeystrokesScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(I18n.translate("keystrokes.keys"));
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {

		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(new VanillaButtonWidget(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20, I18n.translate("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			hud.saveKeystrokes();
		}));
		addDrawableChild(new VanillaButtonWidget(width / 2 + 4, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.done"), button -> this.closeScreen()));

	}


	public void closeScreen() {
		this.minecraft.openScreen(this.screen);
		hud.saveKeystrokes();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
