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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;

public class DownloadImageScreen extends Screen {
	private static final Identifier SPRITE = new Identifier("axolotlclient", "textures/gui/sprites/go.png");

	private final Screen parent;

	private final String title;
	private TextFieldWidget urlBox;
	public DownloadImageScreen(Screen parent) {
		super();
		this.title = I18n.translate("viewScreenshot");
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		urlBox.render();
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, title, width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void init() {
		urlBox = new TextFieldWidget(0, textRenderer, width / 2 - 100, height / 2 - 10, 200, 20){
			@Override
			public void render() {
				super.render();

				if (getText().isEmpty()) {
					drawString(Minecraft.getInstance().textRenderer, I18n.translate("pasteURL"), x + 3, y + 6, -8355712);
				}
			}
		};
		urlBox.setMaxLength(52);
		buttons.add(new ButtonWidget(1, width / 2 + 100 + 4, height / 2 - 10, 20, 20, I18n.translate("download")) {
			@Override
			public void render(Minecraft client, int mouseX, int mouseY) {
				client.getTextureManager().bind(WIDGETS_LOCATION);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.hovered);
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
				GlStateManager.blendFunc(770, 771);
				this.drawTexture(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
				this.drawTexture(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				Minecraft.getInstance().getTextureManager().bind(SPRITE);
				drawTexture(x, y, 0, 0, this.getWidth(), this.height, this.getWidth(), this.height);
			}
		});

		buttons.add(new ButtonWidget(2, width / 2 - 75, height - 33 / 2 - 10, 150, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 1) {
			String url = urlBox.getText().trim();
			if (url.isEmpty()) {
				return;
			}
			minecraft.openScreen(ImageScreen.create(this, ImageShare.getInstance().downloadImage(url), true));
		} else if (buttonWidget.id == 2) {
			minecraft.openScreen(parent);
		}
	}

	@Override
	protected void keyPressed(char character, int code) {
		super.keyPressed(character, code);
		urlBox.keyPressed(character, code);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		urlBox.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void tick() {
		urlBox.tick();
	}
}
