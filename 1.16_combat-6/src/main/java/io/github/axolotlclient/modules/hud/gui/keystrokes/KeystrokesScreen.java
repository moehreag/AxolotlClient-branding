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
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class KeystrokesScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super("keystrokes.keys");
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		super.init();
		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(new ButtonWidget(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20,
			new TranslatableText("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			hud.saveKeystrokes();
		}));
		addDrawableChild(new ButtonWidget(width / 2 + 4, height - 33 / 2 - 10, 150, 20, ScreenTexts.DONE, button -> this.onClose()));

	}

	@Override
	public void onClose() {
		this.client.openScreen(this.screen);
		hud.saveKeystrokes();
	}

	public void removeKey(KeystrokeHud.Keystroke key) {
		keys.remove(key);
	}
}
