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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

public class ImageViewerScreen extends Screen {

	// Icon from https://lucide.dev, "arrow-right"
	private static final Identifier downloadIcon = new Identifier("axolotlclient", "textures/go.png");

	private static final URI aboutPage = URI.create("https://github.com/AxolotlClient/AxolotlClient-mod/wiki/Features#screenshot-sharing");
	private final Screen parent;
	private Identifier imageId;
	private DynamicTexture image;
	private BufferedImage rawImage;
	private String url = "";
	private String imageName;
	private TextFieldWidget urlBox;
	private double imgAspectRatio;

	public ImageViewerScreen(Screen parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();


		urlBox.render();

		if (imageId != null) {
			drawCenteredString(Minecraft.getInstance().textRenderer, imageName, width / 2, 25, -1);

			int imageWidth = Math.min((int) ((height - 150) * imgAspectRatio), width - 150);
			int imageHeight = (int) (imageWidth / imgAspectRatio);

			Minecraft.getInstance().getTextureManager().bind(imageId);
			drawTexture(width / 2 - imageWidth / 2, 50, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

			buttons.stream().filter(buttonWidget -> buttonWidget.id > 25).forEach(buttonWidget -> {
				buttonWidget.x = width / 2 + imageWidth / 2 + 10;

				if (buttonWidget.id == 28) {
					buttonWidget.y = 50 + imageHeight - 20;
				}
			});

			buttons.stream().filter(buttonWidget -> buttonWidget.id > 25).filter(buttonWidget -> !buttonWidget.visible).forEach(buttonWidget -> buttonWidget.visible = true);

		} else {
			drawCenteredString(Minecraft.getInstance().textRenderer, I18n.translate("viewScreenshot"), width / 2, height / 4, -1);
			buttons.stream().filter(buttonWidget -> buttonWidget.id > 25).filter(buttonWidget -> buttonWidget.visible).forEach(buttonWidget -> buttonWidget.visible = false);
		}

		super.render(mouseX, mouseY, delta);

		for (ButtonWidget buttonWidget : this.buttons) {
			if (buttonWidget.isHovered()) {
				buttonWidget.renderTooltip(mouseX, mouseY);
				break;
			}
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
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case 24:
				//Logger.info("Downloading image from "+urlBox.getText());
				imageId = downloadImage(url = urlBox.getText());
				init(Minecraft.getInstance(), width, height);
				break;
			case 25:
				Minecraft.getInstance().openScreen(parent);
				break;
			case 26:
				try {
					ImageIO.write(rawImage, "png", FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve("_share-" + imageName).toFile());
				} catch (IOException e) {
					AxolotlClient.LOGGER.info("Failed to save image!");
				}
				break;
			case 27:
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
					@Override
					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[]{DataFlavor.imageFlavor};
					}

					@Override
					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return DataFlavor.imageFlavor.equals(flavor);
					}

					@NotNull
					@Override
					public Object getTransferData(DataFlavor flavor) {
						return rawImage;
					}
				}, null);
				AxolotlClient.LOGGER.info("Copied image " + imageName + " to the clipboard!");
				break;
			case 28:
				OSUtil.getOS().open(aboutPage);
				break;
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	private Identifier downloadImage(String url) {

		try {
			if (image != null) {
				Minecraft.getInstance().getTextureManager().close(imageId);
			}
			ImageInstance instance = ImageShare.getInstance().downloadImage(url.trim());
			BufferedImage image = instance.getImage();
			rawImage = image;
			if (image != null) {
				Identifier id = new Identifier("screenshot_share_" + Hashing.sha256().hashUnencodedChars(url));
				Minecraft.getInstance().getTextureManager().register(id,
					this.image = new DynamicTexture(image));

				imgAspectRatio = image.getWidth() / (double) image.getHeight();
				imageName = instance.getFileName();
				return id;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	@Override
	public void init() {
		Keyboard.enableRepeatEvents(true);

		urlBox = new TextFieldWidget(23, Minecraft.getInstance().textRenderer, width / 2 - 100, imageId == null ? height / 2 - 10 : height - 80, 200, 20) {
			@Override
			public void render() {
				super.render();

				if (getText().isEmpty()) {
					drawString(Minecraft.getInstance().textRenderer, I18n.translate("pasteURL"), x + 3, y + 6, -8355712);
				}
			}
		};
		if (!url.isEmpty()) {
			urlBox.setText(url);
		}

		urlBox.setFocused(true);

		buttons.add(new ButtonWidget(24, width / 2 + 110, imageId == null ? height / 2 - 10 : height - 80,
			20, 20, I18n.translate("download")) {
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
				Minecraft.getInstance().getTextureManager().bind(downloadIcon);
				drawTexture(x, y, 0, 0, this.getWidth(), this.height, this.getWidth(), this.height);
			}
		});

		buttons.add(new ButtonWidget(25, width / 2 - 75, height - 50, 150, 20, I18n.translate("gui.back")));

		ButtonWidget save = new ButtonWidget(26, width - 60, 50, 50, 20, I18n.translate("saveAction")) {
			@Override
			public void renderTooltip(int mouseX, int mouseY) {
				ImageViewerScreen.this.renderTooltip(I18n.translate("save_image"), mouseX, mouseY);
			}
		};
		buttons.add(save);

		ButtonWidget copy = new ButtonWidget(27, width - 60, 75, 50, 20, I18n.translate("copyAction")) {
			@Override
			public void renderTooltip(int mouseX, int mouseY) {
				ImageViewerScreen.this.renderTooltip(I18n.translate("copy_image"), mouseX, mouseY);
			}
		};
		buttons.add(copy);

		buttons.add(new ButtonWidget(28, width - 60, 100, 50, 20, I18n.translate("aboutAction")) {
			@Override
			public void renderTooltip(int mouseX, int mouseY) {
				ImageViewerScreen.this.renderTooltip(I18n.translate("about_image"), mouseX, mouseY);
			}
		});
	}

	@Override
	public void tick() {
		urlBox.tick();
	}

	@Override
	public void removed() {
		super.removed();
		Keyboard.enableRepeatEvents(false);
		if (image != null) {
			Minecraft.getInstance().getTextureManager().close(imageId);
		}
	}
}
