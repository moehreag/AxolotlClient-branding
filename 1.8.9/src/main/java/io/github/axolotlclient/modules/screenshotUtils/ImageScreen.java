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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class ImageScreen extends Screen {

	private final Screen parent;
	private final ImageInstance image;
	private final boolean freeOnClose;
	private final boolean isRemote;
	private final String title;

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
				Minecraft.getInstance().submit(() -> Minecraft.getInstance().openScreen(new ImageScreen(parent, i, freeOnClose)));
			} else {
				Minecraft.getInstance().submit(() -> Minecraft.getInstance().openScreen(parent));
			}
		}), freeOnClose);
	}

	private ImageScreen(Screen parent, ImageInstance instance, boolean freeOnClose) {
		super();
		this.title = instance.filename();
		this.parent = parent;
		this.image = instance;
		this.freeOnClose = freeOnClose;
		this.isRemote = image instanceof ImageInstance.Remote;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);
		if (isRemote) {
			ImageInstance.Remote r = (ImageInstance.Remote) image;
			drawCenteredString(textRenderer, title, width / 2, 38 / 2 - textRenderer.fontHeight - 2, -1);
			drawCenteredString(textRenderer,
				I18n.translate("gallery.image.upload_details", UUIDHelper.getUsername(r.uploader()),
					r.sharedAt().atZone(ZoneId.systemDefault()).format(AxolotlClientCommon.getInstance().formatter)),
				width / 2, 38 / 2 + 2, -1);
		} else {
			drawCenteredString(textRenderer, title, width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
		}
	}

	@Override
	public void init() {

		int contentsHeight = height - ((isRemote ? 38 : 33) + 33);
		int buttonWidth = 75;
		double imgAspectRatio = image.image().getWidth() / (double) image.image().getHeight();
		int imageWidth = Math.min((int) (contentsHeight * imgAspectRatio), width - buttonWidth - 4 - 10);
		int imageHeight = (int) (imageWidth / imgAspectRatio);


		var element = new ImageElement(imageWidth, imageHeight);
		buttons.add(element);
		if (width / 2 > (imageWidth / 2) + buttonWidth + 4) {
			element.setPosition(width / 2 - imageWidth / 2, 36);
		} else {
			element.setPosition(10, 36);
		}
		int actionX = element.x + imageWidth + 4;
		var actions = new ArrayList<ButtonWidget>();
		if (image instanceof ImageInstance.Local) {
			if (API.getInstance().isAuthenticated() && !(image instanceof ImageInstance.Remote)) {
				actions.add(new ButtonWidget(0, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.upload")));
			}
			actions.add(new ButtonWidget(1, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy")));
			actions.add(new ButtonWidget(2, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.open.external")));
		}
		if (image instanceof ImageInstance.Remote) {
			if (!(image instanceof ImageInstance.Local)) {
				actions.add(new ButtonWidget(3, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.save")));
				actions.add(new ButtonWidget(4, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy")));
			}
			actions.add(new ButtonWidget(5, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.open.external.browser")));
			actions.add(new ButtonWidget(6, 0, 0, buttonWidth, 20, I18n.translate("gallery.image.copy_url")));
		}
		int actionY = element.y;
		for (ButtonWidget w : actions) {
			w.x = actionX;
			w.y = actionY;
			buttons.add(w);
			actionY += 4 + 20;
		}

		buttons.add(new ButtonWidget(7, width / 2 - 75, height - 33 + 33 / 2 - 10, 150, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget b) {
		if (b.id == 0) {
			b.active = false;
			ImageInstance.Local local = (ImageInstance.Local) image;
			ImageShare.getInstance().upload(local.location()).thenAccept(s -> {
				if (s.isEmpty()) {
					Notifications.getInstance().addStatus("gallery.image.upload.failure", "gallery.image.upload.description");
				} else {
					minecraft.submit(() -> minecraft.openScreen(new ImageScreen(parent, local.toShared(s, API.getInstance().getSelf().getUuid(), Instant.now()), freeOnClose)));
					setClipboard(s);
					Notifications.getInstance().addStatus("gallery.image.upload.success", "gallery.image.upload.success.description", s);
				}
			});
		} else if (b.id == 1) {
			ImageInstance.Local local = (ImageInstance.Local) image;
			ScreenshotCopying.copy(local.location());
		} else if (b.id == 2) {
			ImageInstance.Local local = (ImageInstance.Local) image;
			OSUtil.getOS().open(local.location().toUri());
		} else if (b.id == 3) {
			ImageInstance.Remote remote = (ImageInstance.Remote) image;
			b.active = false;
			try {
				Path out = saveSharedImage(remote);
				minecraft.openScreen(new ImageScreen(parent, remote.toShared(out), freeOnClose));
			} catch (IOException e) {
				Notifications.getInstance().addStatus("gallery.image.save.failure", "gallery.image.save.failure.description", e.getMessage());
				AxolotlClient.LOGGER.warn("Failed to save shared image!", e);
			}
		} else if (b.id == 4) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(image.image(), "png", baos);
				ScreenshotCopying.copy(baos.toByteArray());
			} catch (IOException e) {
				Notifications.getInstance().addStatus("gallery.image.copy.failure", "gallery.image.copy.failure.description", e.getMessage());
				AxolotlClient.LOGGER.warn("Failed to copy shared image!", e);
			}

		} else if (b.id == 5) {
			ImageInstance.Remote remote = (ImageInstance.Remote) image;
			OSUtil.getOS().open(remote.url());

		} else if (b.id == 6) {
			ImageInstance.Remote remote = (ImageInstance.Remote) image;
			setClipboard(remote.url());

		} else if (b.id == 7) {
			if (freeOnClose) {
				minecraft.getTextureManager().close(image.id());
			}
			minecraft.openScreen(parent);

		}
	}

	private Path saveSharedImage(ImageInstance.Remote remote) throws IOException {
		Path out = GalleryScreen.SCREENSHOTS_DIR.resolve("shared")
			.resolve(remote.uploader())
			.resolve(remote.filename());
		Path infoJson = out.resolveSibling(remote.filename() + ".json");

		Files.createDirectories(out.getParent());
		try (OutputStream s = Files.newOutputStream(out)) {
			ImageIO.write(remote.image(), "png", s);
		}
		Object json = Map.of("uploader", remote.uploader(), "shared_at", remote.sharedAt());
		Files.writeString(infoJson, GsonHelper.GSON.toJson(json));
		return out;
	}

	private class ImageElement extends ButtonWidget {

		public ImageElement(int width, int height) {
			super(99, 0, 0, width, height, "");
			active = false;
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			client.getTextureManager().bind(image.id());
			drawTexture(x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
		}

		private int getHeight() {
			return height;
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
