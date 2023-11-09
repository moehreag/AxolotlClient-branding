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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class AddOfflineScreen extends Screen {

	private final Screen parent;
	private TextFieldWidget nameInput;

	public AddOfflineScreen(Screen parent) {
		super(Text.translatable("auth.add.offline"));
		this.parent = parent;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button) || nameInput.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics graphics, int i, int j, float f) {
		renderBackground(graphics);
		super.render(graphics, i, j, f);
		graphics.drawShadowedText(textRenderer, Text.translatable("auth.add.offline.name"), (int) (width / 2F - 100), (int) (height / 2f - 20), -1);
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 20, 16777215);
		nameInput.render(graphics, i, j, f);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) || nameInput.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void init() {
		addDrawableChild(nameInput = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.empty()));

		addDrawableChild(new ButtonWidget.Builder(CommonTexts.CANCEL, button -> client.setScreen(parent)).positionAndSize(width / 2 - 155, height - 50, 150, 20).build());
		addDrawableChild(new ButtonWidget.Builder(CommonTexts.DONE, button -> {
			Auth.getInstance().addAccount(new Account(nameInput.getText(), UUID.randomUUID().toString(), Account.OFFLINE_TOKEN));
			client.setScreen(parent);
		}).positionAndSize(width / 2 + 5, height - 50, 150, 20).build());
	}

	@Override
	public void tick() {
		nameInput.tick();
	}
}
