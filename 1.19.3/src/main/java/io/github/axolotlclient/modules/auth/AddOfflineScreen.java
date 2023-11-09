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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
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
	public void render(MatrixStack matrices, int i, int j, float f) {
		renderBackground(matrices);
		super.render(matrices, i, j, f);
		textRenderer.drawWithShadow(matrices, Text.translatable("auth.add.offline.name"), width / 2F - 100, height / 2f - 20, -1);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
		nameInput.render(matrices, i, j, f);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) || nameInput.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void init() {
		addDrawableChild(nameInput = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.empty()));

		addDrawableChild(new ButtonWidget.Builder(ScreenTexts.CANCEL, button -> client.setScreen(parent)).positionAndSize(width / 2 - 155, height - 50, 150, 20).build());
		addDrawableChild(new ButtonWidget.Builder(ScreenTexts.DONE, button -> {
			Auth.getInstance().addAccount(new Account(nameInput.getText(), UUID.randomUUID().toString(), Account.OFFLINE_TOKEN));
			client.setScreen(parent);
		}).positionAndSize(width / 2 + 5, height - 50, 150, 20).build());
	}

	@Override
	public void tick() {
		nameInput.tick();
	}
}
