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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class DownloadImageScreen extends Screen {
	private static final Identifier SPRITE = new Identifier("axolotlclient", "textures/gui/sprites/go.png");

	private final Screen parent;

	public DownloadImageScreen(Screen parent) {
		super(new TranslatableText("viewScreenshot"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	protected void init() {
		var urlBox = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, new TranslatableText("urlBox"));
		urlBox.setSuggestion(I18n.translate("pasteURL"));
		urlBox.setChangedListener(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.translate("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		urlBox.setMaxLength(52);
		addButton(urlBox);
		addButton(new ButtonWidget(width / 2 + 100 + 4, height / 2 - 10, 20, 20, new TranslatableText("download"), b -> {
			String url = urlBox.getText().trim();
			if (url.isEmpty()) {
				return;
			}
			client.openScreen(ImageScreen.create(this, ImageShare.getInstance().downloadImage(url), true));
		}) {
			@Override
			public void renderButton(MatrixStack graphics, int mouseX, int mouseY, float delta) {
				Text message = getMessage();
				setMessage(LiteralText.EMPTY);
				super.renderButton(graphics, mouseX, mouseY, delta);
				setMessage(message);
				client.getTextureManager().bindTexture(SPRITE);
				drawTexture(graphics, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			}
		});

		addButton(new ButtonWidget(width / 2 - 75, height - 33 / 2 - 10, 150, 20, ScreenTexts.BACK, b -> onClose()));


		setInitialFocus(urlBox);
	}

	@Override
	public void onClose() {
		client.openScreen(parent);
	}
}
