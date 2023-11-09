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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class SimpleTextInputScreen extends Screen {

	private final Screen parent;
	private TextFieldWidget input;
	private final Text inputLabel;
	private final Consumer<String> consumer;

	public SimpleTextInputScreen(Screen parent, Text title, Text inputLabel, Consumer<String> consumer) {
		super(title);
		this.parent = parent;
		this.inputLabel = inputLabel;
		this.consumer = consumer;
	}

	@Override
	public void init() {
		addDrawableChild(input = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, inputLabel));

		addDrawableChild(new ButtonWidget.Builder(CommonTexts.CANCEL, button -> client.setScreen(parent))
			.positionAndSize(width / 2 - 155, height - 50, 150, 20).build());
		addDrawableChild(new ButtonWidget.Builder(CommonTexts.DONE, button -> {
			consumer.accept(input.getText());
			client.setScreen(parent);
		}).positionAndSize(width / 2 + 5, height - 50, 150, 20).build());
	}

	@Override
	public void tick() {
		input.tick();
	}

	@Override
	public void render(GuiGraphics graphics, int i, int j, float f) {
		renderBackground(graphics);
		super.render(graphics, i, j, f);
		graphics.drawShadowedText(client.textRenderer, inputLabel, (int) (width / 2F - 100), (int) (height / 2f - 20), -1);
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 20, 16777215);
	}
}
