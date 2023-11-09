/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.auth;

import java.util.UUID;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;

public class AddOfflineScreen extends Screen {

	private final Screen parent;
	private TextFieldWidget nameInput;

	public AddOfflineScreen(Screen parent) {
		this.parent = parent;
	}


	@Override
	public void render(int i, int j, float f) {
		renderBackground();
		super.render(i, j, f);
		textRenderer.drawWithShadow(I18n.translate("auth.add.offline.name"), width / 2F - 100, height / 2f - 20, -1);
		drawCenteredString(this.textRenderer, I18n.translate("auth.add.offline"), this.width / 2, 20, 16777215);
		nameInput.render();
	}

	@Override
	protected void keyPressed(char c, int i) {
		nameInput.keyPressed(c, i);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		nameInput.mouseClicked(i, j, k);
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 1) {
			client.setScreen(parent);
		} else if (buttonWidget.id == 2) {
			Auth.getInstance().addAccount(new Account(nameInput.getText(), UUID.randomUUID().toString(), Account.OFFLINE_TOKEN));
			client.setScreen(parent);
		}
	}

	@Override
	public void init() {
		nameInput = new TextFieldWidget(0, textRenderer, width / 2 - 100, height / 2 - 10, 200, 20);

		buttons.add(new ButtonWidget(1, width / 2 - 155, height - 50, 150, 20, I18n.translate("gui.cancel")));
		buttons.add(new ButtonWidget(2, width / 2 + 5, height - 50, 150, 20, I18n.translate("gui.done")));
	}

	@Override
	public void tick() {
		nameInput.tick();
	}
}
