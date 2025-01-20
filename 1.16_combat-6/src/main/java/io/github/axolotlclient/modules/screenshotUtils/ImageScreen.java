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
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class ImageScreen extends Screen {

	private final Screen parent;
	private final ImageInstance image;
	private final boolean freeOnClose;
	private final boolean isRemote;

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
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().openScreen(new ImageScreen(parent, i, freeOnClose)));
			} else {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().openScreen(parent));
			}
		}), freeOnClose);
	}

	private ImageScreen(Screen parent, ImageInstance instance, boolean freeOnClose) {
		super(new LiteralText(instance.filename()));
		this.parent = parent;
		this.image = instance;
		this.freeOnClose = freeOnClose;
		this.isRemote = image instanceof ImageInstance.Remote;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		if (isRemote) {
			ImageInstance.Remote r = (ImageInstance.Remote) image;
			drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 38 / 2 - textRenderer.fontHeight - 2, -1);
			drawCenteredText(graphics, textRenderer,
				new TranslatableText("gallery.image.upload_details", UUIDHelper.getUsername(r.uploader()),
					r.sharedAt().atZone(ZoneId.systemDefault()).format(AxolotlClientCommon.getInstance().formatter)),
				width / 2, 38 / 2 + 2, -1);
		} else {
			drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
		}
	}

	@Override
	protected void init() {

		int contentsHeight = height - ((isRemote ? 38 : 33) + 33);
		int buttonWidth = 75;
		double imgAspectRatio = image.image().getWidth() / (double) image.image().getHeight();
		int imageWidth = Math.min((int) (contentsHeight * imgAspectRatio), width - buttonWidth - 4 - 10);
		int imageHeight = (int) (imageWidth / imgAspectRatio);


		var element = addButton(new ImageElement(imageWidth, imageHeight));
		if (width / 2 > (imageWidth / 2) + buttonWidth + 4) {
			element.setPosition(width / 2 - imageWidth / 2, 36);
		} else {
			element.setPosition(10, 36);
		}
		int actionX = element.x + imageWidth + 4;
		var actions = new ArrayList<ButtonWidget>();
		if (image instanceof ImageInstance.Local local) {
			if (API.getInstance().isAuthenticated() && !(image instanceof ImageInstance.Remote)) {
				actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.upload"), b -> {
					b.active = false;
					ImageShare.getInstance().upload(local.location()).thenAccept(s -> {
						if (s.isEmpty()) {
							Notifications.getInstance().addStatus("gallery.image.upload.failure", "gallery.image.upload.description");
						} else {
							client.execute(() -> client.openScreen(new ImageScreen(parent, local.toShared(s, API.getInstance().getSelf().getUuid(), Instant.now()), freeOnClose)));
							client.keyboard.setClipboard(s);
							Notifications.getInstance().addStatus("gallery.image.upload.success", "gallery.image.upload.success.description", s);
						}
					});
				}));
			}
			actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.copy"), b -> ScreenshotCopying.copy(local.location())));
			actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.open.external"), b -> Util.getOperatingSystem().open(local.location().toUri())));
		}
		if (image instanceof ImageInstance.Remote remote) {
			if (!(image instanceof ImageInstance.Local)) {
				actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.save"), b -> {
					b.active = false;
					try {
						Path out = saveSharedImage(remote);
						client.openScreen(new ImageScreen(parent, remote.toShared(out), freeOnClose));
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.save.failure", "gallery.image.save.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to save shared image!", e);
					}
				}));
				actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.copy"), b -> {
					try {
						ScreenshotCopying.copy(image.image().getBytes());
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.copy.failure", "gallery.image.copy.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to copy shared image!", e);
					}
				}));
			}
			actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.open.external.browser"), b -> Util.getOperatingSystem().open(remote.url())));
			actions.add(new ButtonWidget(0, 0, buttonWidth, 20, new TranslatableText("gallery.image.copy_url"), b -> client.keyboard.setClipboard(remote.url())));
		}
		int actionY = element.y;
		for (ButtonWidget w : actions) {
			w.x = actionX;
			w.y = actionY;
			addButton(w);
			actionY += 4 + w.getHeight();
		}

		addButton(new ButtonWidget(width / 2 - 75, height - 33 + 33 / 2 - 10, 150, 20, ScreenTexts.BACK, b -> onClose()));
	}

	@Override
	public void onClose() {
		if (freeOnClose) {
			client.getTextureManager().destroyTexture(image.id());
		}
		client.openScreen(parent);
	}

	private Path saveSharedImage(ImageInstance.Remote remote) throws IOException {
		Path out = GalleryScreen.SCREENSHOTS_DIR.resolve("shared")
			.resolve(remote.uploader())
			.resolve(remote.filename());
		Path infoJson = out.resolveSibling(remote.filename() + ".json");

		Files.createDirectories(out.getParent());
		remote.image().writeFile(out);
		Object json = Map.of("uploader", remote.uploader(), "shared_at", remote.sharedAt());
		Files.writeString(infoJson, GsonHelper.GSON.toJson(json));
		return out;
	}

	private class ImageElement extends AbstractButtonWidget {

		public ImageElement(int width, int height) {
			super(0, 0, width, height, LiteralText.EMPTY);
			active = false;
		}

		@Override
		public void renderButton(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
			client.getTextureManager().bindTexture(image.id());
			drawTexture(guiGraphics, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
		}

		@Override
		protected MutableText getNarrationMessage() {
			return LiteralText.EMPTY.copy();
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
