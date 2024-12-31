/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AddOfflineScreen extends Screen {

	private final Screen parent;
	private EditBox nameInput;

	public AddOfflineScreen(Screen parent) {
		super(Component.translatable("auth.add.offline"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int i, int j, float f) {
		renderBackground(graphics, i, j, f);
		super.render(graphics, i, j, f);
		graphics.drawString(font, Component.translatable("auth.add.offline.name"), (int) (width / 2F - 100),
							(int) (height / 2f - 20), -1
						   );
		graphics.drawString(this.font, this.title, this.width / 2, 20, 16777215);
	}

	@Override
	public void init() {
		addRenderableWidget(
			nameInput = new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, Component.empty()));

		addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> minecraft.setScreen(parent))
								.bounds(width / 2 - 155, height - 50, 150, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			Auth.getInstance()
				.addAccount(new Account(nameInput.getValue(), UUID.randomUUID().toString(), Account.OFFLINE_TOKEN));
			minecraft.setScreen(parent);
		}).bounds(width / 2 + 5, height - 50, 150, 20).build());
	}
}
