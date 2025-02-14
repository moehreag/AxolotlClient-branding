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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class KeystrokesScreen extends Screen {

	private final List<KeystrokeHud.Keystroke> keys;
	public final KeystrokeHud hud;
	private final Screen screen;

	public KeystrokesScreen(KeystrokeHud hud, Screen screen) {
		super(Text.translatable("keystrokes.keys"));
		if (hud.keystrokes == null) {
			hud.setKeystrokes();
		}
		this.keys = hud.keystrokes;
		this.hud = hud;
		this.screen = screen;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), width/2, 33/2-textRenderer.fontHeight/2, -1);
	}

	@Override
	protected void init() {
		var keyBindsList = addDrawableChild(new KeyBindsList(this, keys));
		addDrawableChild(ButtonWidget.builder(Text.translatable("controls.resetAll"), button -> {
			keys.clear();
			hud.setDefaultKeystrokes();
			keyBindsList.reload(keys);
			hud.saveKeystrokes();
		}).positionAndSize(width / 2 - 150 - 4, height - 33 / 2 - 10, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen())
			.positionAndSize(width/2+4, height-33/2-10, 150, 20).build());

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
