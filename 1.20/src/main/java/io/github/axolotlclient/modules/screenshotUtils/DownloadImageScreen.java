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

package io.github.axolotlclient.modules.screenshotUtils;

import java.util.function.Supplier;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DownloadImageScreen extends Screen {
	private static final Identifier SPRITE = new Identifier("axolotlclient", "textures/gui/sprites/go.png");

	private final Screen parent;

	public DownloadImageScreen(Screen parent) {
		super(Text.translatable("viewScreenshot"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	protected void init() {
		var urlBox = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.translatable("urlBox"));
		urlBox.setSuggestion(I18n.translate("pasteURL"));
		urlBox.setChangedListener(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.translate("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		urlBox.setMaxLength(52);
		addDrawableChild(urlBox);
		addDrawableChild(new ButtonWidget(width / 2 + 100 + 4, height / 2 - 10, 20, 20, Text.translatable("download"), b -> {
			String url = urlBox.getText().trim();
			if (url.isEmpty()) {
				return;
			}
			client.setScreen(ImageScreen.create(this, ImageShare.getInstance().downloadImage(url), true));
		}, Supplier::get) {
			@Override
			protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				super.drawWidget(graphics, mouseX, mouseY, delta);
				graphics.drawTexture(SPRITE, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			}

			@Override
			public void drawScrollableText(GuiGraphics graphics, TextRenderer renderer, int color) {
			}
		});

		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).positionAndSize(width / 2 - 75, height - 33 / 2 - 10, 150, 20).build());


		setInitialFocus(urlBox);
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
	}
}
