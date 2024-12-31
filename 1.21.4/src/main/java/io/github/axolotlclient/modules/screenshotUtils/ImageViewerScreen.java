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
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.util.OSUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ImageViewerScreen extends Screen {

	// Icon from https://lucide.dev, "arrow-right"
	private static final ResourceLocation downloadIcon = ResourceLocation.fromNamespaceAndPath("axolotlclient", "textures/go.png");

	private static final URI aboutPage = URI.create("https://github.com/AxolotlClient/AxolotlClient-mod/wiki/Features#screenshot-sharing");
	private final Screen parent;
	private final HashMap<Button, Boolean> editButtons = new HashMap<>();
	private ResourceLocation imageId;
	private DynamicTexture image;
	private String url = "";
	private String imageName;
	private EditBox urlBox;
	private double imgAspectRatio;

	public ImageViewerScreen(Screen parent) {
		super(Component.literal("Image viewer"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		if (imageId != null) {
			graphics.drawCenteredString(font, imageName, width / 2, 25, -1);

			int imageWidth = Math.min((int) ((height - 150) * imgAspectRatio), width - 150);
			int imageHeight = (int) (imageWidth / imgAspectRatio);

			RenderSystem.setShaderTexture(0, imageId);
			graphics.blit(RenderType::guiTextured, imageId, width / 2 - imageWidth / 2, 50, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

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
			graphics.drawCenteredString(font, Component.translatable("viewScreenshot"), width / 2, height / 4, -1);
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		if (image != null) {
			minecraft.getTextureManager().release(imageId);
			image.close();
		}
	}

	@Override
	protected void init() {

		urlBox = new EditBox(font, width / 2 - 100, imageId == null ? height / 2 - 10 : height - 80, 200, 20, Component.translatable("urlBox"));
		urlBox.setSuggestion(I18n.get("pasteURL"));
		urlBox.setResponder(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.get("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		urlBox.setMaxLength(52);
		if (!url.isEmpty()) {
			urlBox.setValue(url);
		}
		addRenderableWidget(urlBox);

		setInitialFocus(urlBox);

		addRenderableWidget(new Button(width / 2 + 110, imageId == null ? height / 2 - 10 : height - 80,
			20, 20, Component.translatable("download"), buttonWidget -> {
			//Logger.info("Downloading image from "+urlBox.getText());
			imageId = downloadImage(url = urlBox.getValue());
			clearWidgets();
		}, Supplier::get) {
			@Override
			public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				super.renderWidget(graphics, mouseX, mouseY, delta);
				RenderSystem.enableDepthTest();
				graphics.blit(RenderType::guiTextured, downloadIcon, this.getX(), this.getY(), 0, 0, this.getWidth(), this.getHeight(), getWidth(), getHeight());
			}

			@Override
			public void renderString(GuiGraphics graphics, Font renderer, int color) {
			}
		});

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK,
				buttonWidget -> minecraft.setScreen(parent))
			.pos(width / 2 - 75, height - 50).build());

		Button save = Button.builder(Component.translatable("saveAction"),
			buttonWidget -> {
				try {
					image.getPixels().writeToFile(FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve("_share-" + imageName));
					AxolotlClient.LOGGER.info("Saved image " + imageName + " to screenshots folder!");
				} catch (IOException e) {
					AxolotlClient.LOGGER.info("Failed to save image!");
				}
			}).pos(width - 60, 50).width(50).tooltip(Tooltip.create(Component.translatable("save_image"))).build();
		addImageButton(save, true);

		Button copy = Button.builder(Component.translatable("copyAction"), buttonWidget -> {
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
					return ImageIO.read(new ByteArrayInputStream(DrawUtil.writeToByteArray(Objects.requireNonNull(image.getPixels()))));
				}
			}, null);
			AxolotlClient.LOGGER.info("Copied image " + imageName + " to the clipboard!");
		}).pos(width - 60, 75).width(50).tooltip(Tooltip.create(Component.translatable("copy_image"))).build();
		addImageButton(copy, true);

		Button about = Button.builder(Component.translatable("aboutAction"), buttonWidget -> {
			OSUtil.getOS().open(aboutPage);
		}).pos(width - 60, 100).width(50).tooltip(Tooltip.create(Component.translatable("about_image"))).build();
		addImageButton(about, true);
	}

	private ResourceLocation downloadImage(String url) {

		try {
			if (image != null) {
				minecraft.getTextureManager().release(imageId);
				image.close();
			}
			ImageInstance instance = ImageShare.getInstance().downloadImage(url.trim());
			NativeImage image = instance.getImage();
			if (image != null) {
				ResourceLocation id = ResourceLocation.fromNamespaceAndPath("axolotlclient", "screenshot_share_" + Hashing.sha256().hashUnencodedChars(url));
				minecraft.getTextureManager().register(id,
					this.image = new DynamicTexture(image));

				imgAspectRatio = image.getWidth() / (double) image.getHeight();
				imageName = instance.getFileName();
				return id;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private void addImageButton(Button button, boolean right) {
		addWidget(button);
		editButtons.put(button, right);
	}
}
