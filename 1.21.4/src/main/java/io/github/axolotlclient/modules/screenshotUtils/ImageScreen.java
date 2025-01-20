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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ImageScreen extends Screen {

	private final Screen parent;
	private final ImageInstance image;
	private final boolean freeOnClose;

	static Screen create(Screen parent, CompletableFuture<ImageInstance> future, boolean freeOnClose) {
		if (future.isDone()) {
			if (future.join() != null) {
				return new ImageScreen(parent, future.join(), freeOnClose);
			} else {
				return parent;
			}
		}
		return new LoadingImageScreen(parent, future.thenAccept(i -> {
			if (i != null) {
				Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new ImageScreen(parent, i, freeOnClose)));
			} else {
				Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(parent));
			}
		}), freeOnClose);
	}

	private ImageScreen(Screen parent, ImageInstance instance, boolean freeOnClose) {
		super(Component.literal(instance.filename()));
		this.parent = parent;
		this.image = instance;
		this.freeOnClose = freeOnClose;
	}

	@Override
	protected void init() {
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		LinearLayout header = layout.addToHeader(LinearLayout.vertical()).spacing(4);
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(getTitle(), font));

		if (image instanceof ImageInstance.Remote remote) {
			layout.setHeaderHeight(38);
			header.addChild(new StringWidget(Component.translatable("gallery.image.upload_details", UUIDHelper.getUsername(remote.uploader()), remote.sharedAt().atZone(ZoneId.systemDefault()).format(AxolotlClientCommon.getInstance().formatter)), font));
		}

		int buttonWidth = 75;
		double imgAspectRatio = image.image().getWidth() / (double) image.image().getHeight();
		int imageWidth = Math.min((int) (layout.getContentHeight() * imgAspectRatio), layout.getWidth() - buttonWidth - 4 - 10);
		int imageHeight = (int) (imageWidth / imgAspectRatio);

		var contents = layout.addToContents(LinearLayout.horizontal().spacing(4));
		if (width/2 > (imageWidth / 2) + buttonWidth+4) {
			contents.addChild(new SpacerElement(buttonWidth + 4, imageHeight));
		}
		var footer = layout.addToFooter(LinearLayout.horizontal().spacing(4));
		contents.addChild(new ImageElement(imageWidth, imageHeight));
		var actions = contents.addChild(LinearLayout.vertical()).spacing(4);
		if (image instanceof ImageInstance.Local local) {
			if (API.getInstance().isAuthenticated() && !(image instanceof ImageInstance.Remote)) {
				actions.addChild(Button.builder(Component.translatable("gallery.image.upload"), b -> {
					b.active = false;
					ImageShare.getInstance().upload(local.location()).thenAccept(s -> {
						if (s.isEmpty()) {
							Notifications.getInstance().addStatus("gallery.image.upload.failure", "gallery.image.upload.description");
						} else {
							minecraft.execute(() -> minecraft.setScreen(new ImageScreen(parent, local.toShared(s, API.getInstance().getSelf().getUuid(), Instant.now()), freeOnClose)));
							minecraft.keyboardHandler.setClipboard(s);
							Notifications.getInstance().addStatus("gallery.image.upload.success", "gallery.image.upload.success.description", s);
						}
					});
				}).width(buttonWidth).build());
			}
			actions.addChild(Button.builder(Component.translatable("gallery.image.copy"), b -> ScreenshotCopying.copy(local.location())).width(buttonWidth).build());
			actions.addChild(Button.builder(Component.translatable("gallery.image.open.external"), b -> Util.getPlatform().openPath(local.location())).width(buttonWidth).build());
		}
		if (image instanceof ImageInstance.Remote remote) {
			if (!(image instanceof ImageInstance.Local)) {
				actions.addChild(Button.builder(Component.translatable("gallery.image.save"), b -> {
					b.active = false;
					try {
						Path out = saveSharedImage(remote);
						minecraft.setScreen(new ImageScreen(parent, remote.toShared(out), freeOnClose));
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.save.failure", "gallery.image.save.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to save shared image!", e);
					}
				}).width(buttonWidth).build());
				actions.addChild(Button.builder(Component.translatable("gallery.image.copy"), b -> {
					try {
						ScreenshotCopying.copy(DrawUtil.writeToByteArray(image.image()));
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.copy.failure", "gallery.image.copy.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to copy shared image!", e);
					}
				}).width(buttonWidth).build());
			}
			actions.addChild(Button.builder(Component.translatable("gallery.image.open.external.browser"), b -> Util.getPlatform().openUri(remote.url())).width(buttonWidth).build());
			actions.addChild(Button.builder(Component.translatable("gallery.image.copy_url"), b -> minecraft.keyboardHandler.setClipboard(remote.url())).width(buttonWidth).build());
		}

		footer.addChild(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void onClose() {
		if (freeOnClose) {
			minecraft.getTextureManager().release(image.id());
		}
		minecraft.setScreen(parent);
	}

	private Path saveSharedImage(ImageInstance.Remote remote) throws IOException {
		Path out = GalleryScreen.SCREENSHOTS_DIR.resolve("shared")
			.resolve(remote.uploader())
			.resolve(remote.filename());
		Path infoJson = out.resolveSibling(remote.filename() + ".json");

		Files.createDirectories(out.getParent());
		remote.image().writeToFile(out);
		Object json = Map.of("uploader", remote.uploader(), "shared_at", remote.sharedAt());
		Files.writeString(infoJson, GsonHelper.GSON.toJson(json));
		return out;
	}

	private class ImageElement extends AbstractWidget {

		public ImageElement(int width, int height) {
			super(0, 0, width, height, Component.empty());
			active = false;
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.blit(RenderType::guiTextured, image.id(), getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}
	}
}
