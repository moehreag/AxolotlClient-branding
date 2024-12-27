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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ImageViewerScreen extends Screen {

	// Icon from https://lucide.dev, "arrow-right"
	private static final Identifier downloadIcon = new Identifier("axolotlclient", "textures/gui/sprites/go.png");

	private static final URI aboutPage = URI.create("https://github.com/AxolotlClient/AxolotlClient-mod/wiki/Features#screenshot-sharing");
	private final Screen parent;
	private final HashMap<ButtonWidget, Boolean> editButtons = new HashMap<>();
	private Identifier imageId;
	private NativeImageBackedTexture image;
	private String url = "";
	private String imageName;
	private TextFieldWidget urlBox;
	private double imgAspectRatio;

	public ImageViewerScreen(Screen parent) {
		super(Text.of("Image viewer"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);

		super.render(matrices, mouseX, mouseY, delta);

		if (imageId != null) {
			drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, imageName, width / 2, 25, -1);

			int imageWidth = Math.min((int) ((height - 150) * imgAspectRatio), width - 150);
			int imageHeight = (int) (imageWidth / imgAspectRatio);

			MinecraftClient.getInstance().getTextureManager().bindTexture(imageId);
			drawTexture(matrices, width / 2 - imageWidth / 2, 50, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

			editButtons.keySet().forEach(buttonWidget -> {

				if (editButtons.get(buttonWidget)) {
					buttonWidget.x = (width / 2 + imageWidth / 2 + 10);
				} else {
					buttonWidget.x = (width / 2 - imageWidth / 2 - 10 - buttonWidget.getWidth());
				}

				if (buttonWidget.getMessage().getString().toLowerCase(Locale.ENGLISH).contains("about")) {
					buttonWidget.y = (50 + imageHeight - buttonWidget.getHeight());
				}

				buttonWidget.render(matrices, mouseX, mouseY, delta);
			});
		} else {
			drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, new TranslatableText("viewScreenshot"), width / 2, height / 4, -1);
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		if (image != null) {
			MinecraftClient.getInstance().getTextureManager().destroyTexture(imageId);
			image.close();
		}
	}

	@Override
	protected void init() {

		urlBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 100, imageId == null ? height / 2 - 10 : height - 80, 200, 20, new TranslatableText("urlBox"));
		urlBox.setSuggestion(I18n.translate("pasteURL"));
		urlBox.setChangedListener(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.translate("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		if (!url.isEmpty()) {
			urlBox.setText(url);
		}
		addButton(urlBox);

		setInitialFocus(urlBox);

		addButton(new ButtonWidget(width / 2 + 110, imageId == null ? height / 2 - 10 : height - 80,
			20, 20, new TranslatableText("download"), buttonWidget -> {
			//Logger.info("Downloading image from "+urlBox.getText());
			imageId = downloadImage(url = urlBox.getText());
			init(client, width, height);
		}) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
				int i = this.getYImage(this.isHovered());
				this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
				this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				RenderSystem.enableDepthTest();
				MinecraftClient.getInstance().getTextureManager().bindTexture(downloadIcon);
				drawTexture(matrices, this.x, this.y, 0, 0, this.getWidth(), this.getHeight(), getWidth(), getHeight());
			}
		});

		addButton(new ButtonWidget(width / 2 - 75, height - 50, 150, 20, ScreenTexts.BACK,
			buttonWidget -> MinecraftClient.getInstance().openScreen(parent)));

		ButtonWidget save = new ButtonWidget(width - 60, 50, 50, 20, new TranslatableText("saveAction"),
			buttonWidget -> {
				try {
					Files.write(FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve("_share-" + imageName), Objects.requireNonNull(image.getImage()).getBytes());
					AxolotlClient.LOGGER.info("Saved image " + imageName + " to screenshots folder!");
				} catch (IOException e) {
					AxolotlClient.LOGGER.info("Failed to save image!");
				}
			}, (buttonWidget, matrixStack, i, j) -> ImageViewerScreen.this.renderTooltip(matrixStack, new TranslatableText("save_image"), i, j));
		addImageButton(save, true);

		ButtonWidget copy = new ButtonWidget(width - 60, 75, 50, 20, new TranslatableText("copyAction"), buttonWidget -> {
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
				public Object getTransferData(DataFlavor flavor) throws IOException {
					return ImageIO.read(new ByteArrayInputStream(Objects.requireNonNull(image.getImage()).getBytes()));
				}
			}, null);
			AxolotlClient.LOGGER.info("Copied image " + imageName + " to the clipboard!");
		}, (buttonWidget, matrixStack, i, j) -> ImageViewerScreen.this.renderTooltip(matrixStack, new TranslatableText("copy_image"), i, j));

		addImageButton(copy, true);

		ButtonWidget about = new ButtonWidget(width - 60, 100, 50, 20, new TranslatableText("aboutAction"), buttonWidget -> {
			OSUtil.getOS().open(aboutPage);
		}, (buttonWidget, matrixStack, i, j) -> ImageViewerScreen.this.renderTooltip(matrixStack, new TranslatableText("about_image"), i, j));

		addImageButton(about, true);
	}

	@SuppressWarnings("UnstableApiUsage")
	private Identifier downloadImage(String url) {

		try {
			if (image != null) {
				MinecraftClient.getInstance().getTextureManager().destroyTexture(imageId);
				image.close();
			}
			ImageInstance instance = ImageShare.getInstance().downloadImage(url.trim());
			NativeImage image = instance.getImage();
			if (image != null) {
				Identifier id = new Identifier("screenshot_share_" + Hashing.sha256().hashUnencodedChars(url));
				MinecraftClient.getInstance().getTextureManager().registerTexture(id,
					this.image = new NativeImageBackedTexture(image));

				imgAspectRatio = image.getWidth() / (double) image.getHeight();
				imageName = instance.getFileName();
				return id;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private void addImageButton(ButtonWidget button, boolean right) {
		addChild(button);
		editButtons.put(button, right);
	}

	@Override
	public void tick() {
		urlBox.tick();
	}
}
