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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.Watcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class GalleryScreen extends Screen {

	public static final Path SCREENSHOTS_DIR = FabricLoader.getInstance().getGameDir().resolve("screenshots");

	private record Tab<T>(String title, Callable<List<T>> list, Map<T, ImageInstance> loadingCache,
						  Loader<T> loader) {

		private static <T> Tab<T> of(String title, Callable<List<T>> list, Loader<T> loader) {
			var cache = new HashMap<T, ImageInstance>();
			return new Tab<>(title, list, cache, o -> {
				if (cache.containsKey(o)) {
					return cache.get(o);
				}
				var val = loader.load(o);
				cache.put(o, val);
				return val;
			});
		}

		private static final Tab<Path> LOCAL = of(I18n.translate("gallery.title.local"), () -> {
			try (Stream<Path> screenshots = Files.list(SCREENSHOTS_DIR)) {
				return screenshots.sorted(Comparator.<Path>comparingLong(p -> {
					try {
						return Files.getLastModifiedTime(p).toMillis();
					} catch (IOException e) {
						return 0L;
					}
				}).reversed()).toList();
			}
		}, ImageInstance.LocalImpl::new);

		private static final Tab<String> SHARED = of(I18n.translate("gallery.title.shared"), () ->
			FriendRequest.getInstance().getFriendUuids()
				.thenApply(res -> res.stream().map(UserRequest::getUploadedImages)
					.map(CompletableFuture::join)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.reduce(new ArrayList<>(), (l1, l2) -> {
						l1.addAll(l2);
						return l1;
					})).join(), url -> ImageShare.getInstance().downloadImage(url).join());

		interface Loader<T> {
			ImageInstance load(T obj) throws Exception;
		}
	}

	private Tab<?> current;

	private final Screen parent;
	private final Watcher watcher;
	private final String title;

	public GalleryScreen(Screen parent) {
		super();
		title = I18n.translate("gallery.title");
		this.parent = parent;
		this.current = Tab.LOCAL;
		this.watcher = Watcher.createSelfTicking(SCREENSHOTS_DIR, () -> {
			if (current == Tab.LOCAL) {
				init(minecraft, width, height);
			}
		});
	}

	private static final int entrySpacing = 4,
		entryWidth = 100,
		entryHeight = 75,
		marginLeftRight = 10;

	private boolean isError, online;
	private ImageList area;

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		area.render(mouseX, mouseY, delta);
		super.render(mouseX, mouseY, delta);

		if (online) {
			drawCenteredString(textRenderer, title, width / 2, 40 / 2 - 2 - textRenderer.fontHeight, -1);
			drawCenteredString(textRenderer, current.title(), width / 2, 40 / 2 + 2, -1);
		} else {
			drawCenteredString(textRenderer, title, width / 2, 40 / 2 - textRenderer.fontHeight / 2, -1);
		}

		if (isError) {
			drawCenteredString(textRenderer, I18n.translate("gallery.error.loading"), width / 2, 36, -1);
		}
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		area.handleMouse();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		area.mouseClicked(i, j, k);
	}

	@Override
	protected void mouseReleased(int i, int j, int k) {
		super.mouseReleased(i, j, k);
		area.mouseReleased(i, j, k);
	}

	@Override
	public void init() {
		online = API.getInstance().isAuthenticated();

		int columnCount = (width - (marginLeftRight * 2) + entrySpacing - 13) / (entryWidth + entrySpacing); // -13 to always have enough space for the scrollbar

		area = new ImageList(minecraft, width, height, 33, height - 40, entryHeight + entrySpacing, columnCount);


		CompletableFuture.runAsync(() -> {
			try {
				loadTab(current, columnCount, area);
			} catch (Exception e) {
				isError = true;
				buttons.add(new ButtonWidget(0, width / 2 - 75, 36 + textRenderer.fontHeight + 8, 150, 20, I18n.translate("gallery.reload")));
			}
		});

		int buttonWidth = columnCount <= 5 && online ? 100 : 150;
		int footerButtonX = online ? width / 2 - buttonWidth - buttonWidth / 2 - 4 : width / 2 - buttonWidth - 2;
		int footerButtonY = height - 33 / 2 - 10;
		if (online) {
			ButtonWidget switchTab;
			if (current == Tab.SHARED) {
				switchTab = new ButtonWidget(1, footerButtonX, footerButtonY, buttonWidth, 20, I18n.translate("gallery.tab.local"));
			} else {
				switchTab = new ButtonWidget(2, footerButtonX, footerButtonY, buttonWidth, 20, I18n.translate("gallery.tab.shared"));
			}
			buttons.add(switchTab);
			footerButtonX += buttonWidth + 4;
		}
		buttons.add(new ButtonWidget(3, footerButtonX, footerButtonY, buttonWidth, 20, I18n.translate("gallery.download_external")));
		footerButtonX += buttonWidth + 4;
		buttons.add(new ButtonWidget(4, footerButtonX, footerButtonY, buttonWidth, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		switch (buttonWidget.id) {
			case 0 -> init(minecraft, width, height);
			case 1 -> setTab(Tab.LOCAL);
			case 2 -> setTab(Tab.SHARED);
			case 3 -> minecraft.openScreen(new DownloadImageScreen(this));
			case 4 -> {
				Tab.LOCAL.loadingCache().forEach((path, instance) -> minecraft.getTextureManager().close(instance.id()));
				Tab.LOCAL.loadingCache().clear();
				Tab.SHARED.loadingCache().forEach((s, instance) -> minecraft.getTextureManager().close(instance.id()));
				Tab.SHARED.loadingCache().clear();
				Watcher.close(watcher);
				minecraft.openScreen(parent);
			}
		}
	}

	private void setTab(Tab<?> tab) {
		current = tab;
		init(minecraft, width, height);
	}

	private <T> void loadTab(Tab<T> tab, int columnCount, ImageList area) throws Exception {
		List<T> images = tab.list.call();
		int size = images.size();
		for (int i = 0; i < size; i += columnCount) {
			ImageListEntry row = new ImageListEntry(columnCount, area);
			area.addEntry(row);
			for (int x = 0; x < columnCount; x++) {
				if (i + x >= size) {
					break;
				}
				T p = images.get(i + x);
				var entry = new ImageEntry(entryWidth, entryHeight, () -> tab.loader.load(p), row);
				row.add(entry);
			}
		}
	}

	// This image list is loading its entries lazily! :)
	private class ImageEntry extends ButtonWidget {

		private static final int bgColor = Colors.DARK_GRAY.toInt();
		private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

		private final TextRenderer font;
		private final Callable<ImageInstance> instanceSupplier;
		private final ImageListEntry row;
		private long loadStart;
		private CompletableFuture<ImageInstance> future;

		protected ImageEntry(int width, int height, Callable<ImageInstance> instanceSupplier, ImageListEntry row) {
			super(99, 0, 0, width, height, "");
			this.instanceSupplier = instanceSupplier;
			this.row = row;
			this.font = Minecraft.getInstance().textRenderer;
		}

		private CompletableFuture<ImageInstance> load() {
			if (future == null) {
				loadStart = Minecraft.getTime();
				future = CompletableFuture.supplyAsync(() -> {
					try {
						var instance = instanceSupplier.call();
						message = instance.filename();
						return instance;
					} catch (Exception e) {
						row.remove(this);
						return null;
					}
				});
			}
			return future;
		}

		public void onPress() {
			minecraft.openScreen(ImageScreen.create(GalleryScreen.this, load(), false));
			playClickSound(minecraft.getSoundManager());
		}

		@Override
		public void render(Minecraft client, int mouseX, int mouseY) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			if (load().isDone() && client.getTextureManager().get(load().join().id()) != null) {
				client.getTextureManager().bind(load().join().id());
				GlStateManager.color3f(1, 1, 1);
				drawTexture(x, y, 0, 0, getWidth(), getHeight() - font.fontHeight - 2, getWidth(), getHeight() - font.fontHeight - 2);
				drawScrollingText(font, 2, -1);
			} else {
				float delta = (float) easeInOutCubic((Minecraft.getTime() - loadStart) % 1000f / 1000f);

				fill(getX() + 2, getY() + 2, getXEnd() - 2, getYEnd() - font.fontHeight - 2, bgColor);
				drawHorizontalGradient(getX() + 2, getY() + 2, getYEnd() - font.fontHeight - 2, lerp(delta, getX() + 2, getXEnd() - 2));

				fill(getX() + 2, getYEnd() - font.fontHeight - 1, getXEnd() - 2, getYEnd() - 2, bgColor);
				drawHorizontalGradient(getX() + 2, getYEnd() - font.fontHeight - 1, getYEnd() - 2, lerp(delta, getX() + 2, getXEnd() - 2));
			}
			DrawUtil.outlineRect(getX(), getY(), getWidth(), getHeight(), row.list.isInListContent(mouseX, mouseY) && isHovered() ? -1 : bgColor);
		}

		private int getX() {
			return x;
		}

		private int getY() {
			return y;
		}

		protected int getYEnd() {
			return y + getHeight();
		}

		private int getHeight() {
			return height;
		}

		protected int getXEnd() {
			return x + getWidth();
		}

		private void drawHorizontalGradient(int x1, int y1, int y2, int x2) {
			BufferBuilder consumer = Tessellator.getInstance().getBuilder();
			consumer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
			consumer.vertex(x1, y1, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
			consumer.vertex(x1, y2, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
			consumer.vertex(x2, y2, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
			consumer.vertex(x2, y1, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
			Tessellator.getInstance().end();
		}

		private double easeInOutCubic(double x) {
			return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
		}

		private int lerp(float delta, int start, int end) {
			return (int) MathHelper.clamp(Util.lerp(delta, start, end), start, end);
		}

		protected void drawScrollingText(TextRenderer font, int offset, int color) {
			int i = this.x + offset;
			int j = this.x + this.getWidth() - offset;
			DrawUtil.drawScrollableText(font, this.message, i, this.y + getHeight() - font.fontHeight - 1, j, this.y + this.getHeight(), color);
		}
	}

	private static class ImageListEntry implements EntryListWidget.Entry {

		private final List<ImageEntry> buttons;
		private final int size;
		private final ImageList list;

		public ImageListEntry(int size, ImageList list) {
			this.size = size;
			buttons = new ArrayList<>(size);
			this.list = list;
		}

		@Override
		public void render(int index, int left, int top, int width, int height, int mouseX, int mouseY, boolean hovered) {
			if (Math.max(left, list.getRowLeft()) <= Math.min(left + width, list.getRowLeft() + list.getWidth()) - 1 &&
				Math.max(top - height, list.getY()) <= Math.min(top + height * 2, list.getY() + list.viewHeight()) - 1) {
				buttons.forEach(e -> {
					e.y = top;
					e.render(Minecraft.getInstance(), mouseX, mouseY);
				});
			} else {
				buttons.forEach(e -> {
					e.y = top;
				});
			}
		}

		public void add(ImageEntry e) {
			buttons.add(e);
			repositionButtons();
		}

		public void remove(ImageEntry e) {
			buttons.remove(e);
			if (buttons.isEmpty()) {
				list.removeEntry(this);
			} else if (buttons.size() < size) {
				list.shiftEntries(this);
			}
			repositionButtons();
		}

		private void repositionButtons() {
			int x = list.getRowLeft();
			for (ImageEntry e : buttons) {
				e.x = x;
				x += e.getWidth() + entrySpacing;
			}
		}

		public ImageEntry pop() {
			var entry = buttons.remove(0);
			if (buttons.isEmpty()) {
				list.removeEntry(this);
			} else if (buttons.size() < size) {
				list.shiftEntries(this);
			}
			repositionButtons();
			return entry;
		}

		@Override
		public void renderOutOfBounds(int i, int j, int k) {

		}

		@Override
		public boolean mouseClicked(int i, int j, int k, int l, int m, int n) {
			Optional<ImageEntry> present = buttons.stream().filter(ButtonWidget::isHovered).findFirst();
			present.ifPresent(ImageEntry::onPress);
			return present.isPresent();
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}
	}

	private static class ImageList extends EntryListWidget {

		private final int rowWidth;
		private final List<ImageListEntry> entries = new ArrayList<>();

		public ImageList(Minecraft minecraft, int screenWidth, int screenHeight, int top, int bottom, int entryHeight, int columns) {
			super(minecraft, screenWidth, screenHeight, top, bottom, entryHeight);
			this.rowWidth = columns * (entryWidth + entrySpacing) - entrySpacing;
		}

		public void addEntry(ImageListEntry entry) {
			entries.add(entry);
		}

		@Override
		public int getRowWidth() {
			return rowWidth;
		}

		public int getRowLeft() {
			return this.minX + this.width / 2 - this.getRowWidth() / 2;
		}

		public void removeEntry(ImageListEntry entry) {
			entries.remove(entry);
		}

		public void shiftEntries(ImageListEntry origin) {
			int originIndex = entries.indexOf(origin);
			int lastIndex = entries.size() - 1;
			if (originIndex == lastIndex) {
				return;
			}
			ImageListEntry next = entries.get(originIndex + 1);
			origin.add(next.pop());
		}

		@Override
		protected int getScrollbarPosition() {
			return getRowLeft() + getRowWidth() + 6 + 2;
		}

		public boolean isInListContent(int x, int y) {
			return x >= minX && x < maxX && y >= minY && y < maxY;
		}

		public int getY() {
			return minY;
		}

		public int getWidth() {
			return width;
		}

		@Override
		protected int size() {
			return entries.size();
		}

		public int viewHeight() {
			return maxY - minY;
		}

		@Override
		public Entry getEntry(int i) {
			return entries.get(i);
		}
	}
}
