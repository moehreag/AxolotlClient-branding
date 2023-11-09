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

package io.github.axolotlclient.api;

import java.util.function.Consumer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;

public class SimpleTextInputScreen extends Screen {

	private final Screen parent;
	private TextFieldWidget input;
	private final String inputLabel;
	private final String title;
	private final Consumer<String> consumer;

	public SimpleTextInputScreen(Screen parent, String title, String inputLabel, Consumer<String> consumer) {
		super();
		this.parent = parent;
		this.inputLabel = inputLabel;
		this.title = title;
		this.consumer = consumer;
	}

	@Override
	public void init() {
		input = new TextFieldWidget(3, textRenderer, width / 2 - 100, height / 2 - 10, 200, 20);

		buttons.add(new ButtonWidget(0, width / 2 - 155, height - 50, 150, 20, I18n.translate("gui.cancel")));
		buttons.add(new ButtonWidget(1, width / 2 + 5, height - 50, 150, 20, I18n.translate("gui.done")));
	}

	@Override
	public void tick() {
		input.tick();
	}

	@Override
	public void render(int i, int j, float f) {
		renderBackground();
		super.render(i, j, f);
		textRenderer.drawWithShadow(inputLabel, width / 2F - 100, height / 2f - 20, -1);
		drawCenteredString(this.textRenderer, this.title, this.width / 2, 20, 16777215);
		input.render();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		input.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void keyPressed(char c, int i) {
		input.keyPressed(c, i);
		super.keyPressed(c, i);
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		switch (buttonWidget.id) {
			case 0:
				client.setScreen(parent);
				break;
			case 1:
				consumer.accept(input.getText());
				client.setScreen(parent);
				break;
		}
	}
}
