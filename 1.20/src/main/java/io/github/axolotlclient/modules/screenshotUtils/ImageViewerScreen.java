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
import java.util.function.Supplier;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;

public class ImageViewerScreen extends Screen {

	// Icon from https://lucide.dev, "arrow-right"
	private static final Identifier downloadIcon = new Identifier("axolotlclient", "textures/go.png");

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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);

		super.render(graphics, mouseX, mouseY, delta);

		if (imageId != null) {
			graphics.drawCenteredShadowedText(client.textRenderer, imageName, width / 2, 25, -1);

			int imageWidth = Math.min((int) ((height - 150) * imgAspectRatio), width - 150);
			int imageHeight = (int) (imageWidth / imgAspectRatio);

			RenderSystem.setShaderTexture(0, imageId);
			graphics.drawTexture(imageId, width / 2 - imageWidth / 2, 50, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

			editButtons.keySet().forEach(buttonWidget -> {

				if (editButtons.get(buttonWidget)) {
					buttonWidget.setX(width / 2 + imageWidth / 2 + 10);
				} else {
					buttonWidget.setX(width / 2 - imageWidth / 2 - 10 - buttonWidget.getWidth());
				}

				if (buttonWidget.getMessage().getString().toLowerCase(Locale.ENGLISH).contains("about")) {
					buttonWidget.setY(50 + imageHeight - buttonWidget.getHeight());
				}

				buttonWidget.render(graphics, mouseX, mouseY, delta);
			});
		} else {
			graphics.drawCenteredShadowedText(client.textRenderer, Text.translatable("viewScreenshot"), width / 2, height / 4, -1);
		}
	}

	@Override
	public void closeScreen() {
		super.closeScreen();
		if (image != null) {
			MinecraftClient.getInstance().getTextureManager().destroyTexture(imageId);
			image.close();
		}
	}

	@Override
	protected void init() {

		urlBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 100, imageId == null ? height / 2 - 10 : height - 80, 200, 20, Text.translatable("urlBox"));
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
		addDrawableChild(urlBox);

		setFocusedChild(urlBox);

		addDrawableChild(new ButtonWidget(width / 2 + 110, imageId == null ? height / 2 - 10 : height - 80,
			20, 20, Text.translatable("download"), buttonWidget -> {
			//Logger.info("Downloading image from "+urlBox.getText());
			imageId = downloadImage(url = urlBox.getText());
			clearAndInit();
		}, Supplier::get) {
			@Override
			public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				super.drawWidget(graphics, mouseX, mouseY, delta);
				RenderSystem.enableDepthTest();
				graphics.drawTexture(downloadIcon, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), getWidth(), getHeight());
			}
		});

		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK,
				buttonWidget -> MinecraftClient.getInstance().setScreen(parent))
			.position(width / 2 - 75, height - 50).build());

		ButtonWidget save = ButtonWidget.builder(Text.translatable("saveAction"),
			buttonWidget -> {
				try {
					Files.write(QuiltLoader.getGameDir().resolve("screenshots").resolve("_share-" + imageName), Objects.requireNonNull(image.getImage()).getBytes());
					AxolotlClient.LOGGER.info("Saved image " + imageName + " to screenshots folder!");
				} catch (IOException e) {
					AxolotlClient.LOGGER.info("Failed to save image!");
				}
			}).position(width - 60, 50).width(50).tooltip(Tooltip.create(Text.translatable("save_image"))).build();
		addImageButton(save, true);

		ButtonWidget copy = ButtonWidget.builder(Text.translatable("copyAction"), buttonWidget -> {
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
		}).position(width - 60, 75).width(50).tooltip(Tooltip.create(Text.translatable("copy_image"))).build();
		addImageButton(copy, true);

		ButtonWidget about = ButtonWidget.builder(Text.translatable("aboutAction"), buttonWidget -> {
			OSUtil.getOS().open(aboutPage, AxolotlClient.LOGGER);
		}).position(width - 60, 100).width(50).tooltip(Tooltip.create(Text.translatable("about_image"))).build();
		addImageButton(about, true);
	}

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
		addSelectableChild(button);
		editButtons.put(button, right);
	}

	@Override
	public void tick() {
		urlBox.tick();
	}
}
