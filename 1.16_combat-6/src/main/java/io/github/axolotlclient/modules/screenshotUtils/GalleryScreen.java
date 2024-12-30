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

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.Watcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.NotNull;

public class GalleryScreen extends Screen {

	public static final Path SCREENSHOTS_DIR = FabricLoader.getInstance().getGameDir().resolve("screenshots");

	private record Tab<T>(Text title, Callable<List<T>> list, Map<T, ImageInstance> loadingCache,
						  Loader<T> loader) {

		private static <T> Tab<T> of(Text title, Callable<List<T>> list, Loader<T> loader) {
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

		private static final Tab<Path> LOCAL = of(new TranslatableText("gallery.title.local"), () -> {
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

		private static final Tab<String> SHARED = of(new TranslatableText("gallery.title.shared"), () ->
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

	public GalleryScreen(Screen parent) {
		super(new TranslatableText("gallery.title"));
		this.parent = parent;
		this.current = Tab.LOCAL;
		this.watcher = Watcher.createSelfTicking(SCREENSHOTS_DIR, () -> {
			if (current == Tab.LOCAL) {
				init(client, width, height);
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
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		area.render(graphics, mouseX, mouseY, delta);
		super.render(graphics, mouseX, mouseY, delta);

		if (online) {
			drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 40 / 2 - 2 - textRenderer.fontHeight, -1);
			drawCenteredText(graphics, textRenderer, current.title(), width / 2, 40 / 2 + 2, -1);
		} else {
			drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 40 / 2 - textRenderer.fontHeight / 2, -1);
		}

		if (isError) {
			drawCenteredText(graphics, textRenderer, new TranslatableText("gallery.error.loading"), width / 2, 36, -1);
		}
	}

	@Override
	protected void init() {
		online = API.getInstance().isAuthenticated();

		int columnCount = (width - (marginLeftRight * 2) + entrySpacing - 13) / (entryWidth + entrySpacing); // -13 to always have enough space for the scrollbar

		area = new ImageList(client, width, height, 33, height - 40, entryHeight + entrySpacing, columnCount);
		addChild(area);

		setInitialFocus(area);
		CompletableFuture.runAsync(() -> {
			try {
				loadTab(current, columnCount, area);
			} catch (Exception e) {
				isError = true;
				setInitialFocus(addButton(new ButtonWidget(width / 2 - 75, 36 + textRenderer.fontHeight + 8, 150, 20, new TranslatableText("gallery.reload"), b -> init(client, width, height))));
			}
		});

		int buttonWidth = columnCount <= 5 && online ? 100 : 150;
		int footerButtonX = online ? width / 2 - buttonWidth - buttonWidth / 2 - 4 : width / 2 - buttonWidth - 2;
		int footerButtonY = height - 33 / 2 - 10;
		if (online) {
			ButtonWidget switchTab;
			if (current == Tab.SHARED) {
				switchTab = new ButtonWidget(footerButtonX, footerButtonY, buttonWidth, 20, new TranslatableText("gallery.tab.local"), b -> setTab(Tab.LOCAL));
			} else {
				switchTab = new ButtonWidget(footerButtonX, footerButtonY, buttonWidth, 20, new TranslatableText("gallery.tab.shared"), b -> setTab(Tab.SHARED));
			}
			addButton(switchTab);
			footerButtonX += buttonWidth + 4;
		}
		addButton(new ButtonWidget(footerButtonX, footerButtonY, buttonWidth, 20, new TranslatableText("gallery.download_external"), b -> client.openScreen(new DownloadImageScreen(this))));
		footerButtonX += buttonWidth + 4;
		addButton(new ButtonWidget(footerButtonX, footerButtonY, buttonWidth, 20, ScreenTexts.BACK, b -> onClose()));
	}

	@Override
	public void onClose() {
		Tab.LOCAL.loadingCache().forEach((path, instance) -> client.getTextureManager().destroyTexture(instance.id()));
		Tab.LOCAL.loadingCache().clear();
		Tab.SHARED.loadingCache().forEach((s, instance) -> client.getTextureManager().destroyTexture(instance.id()));
		Tab.SHARED.loadingCache().clear();
		Watcher.close(watcher);
		client.openScreen(parent);
	}

	private void setTab(Tab<?> tab) {
		current = tab;
		init(client, width, height);
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
			super(0, 0, width, height, LiteralText.EMPTY, b -> {
			});
			this.instanceSupplier = instanceSupplier;
			this.row = row;
			this.font = MinecraftClient.getInstance().textRenderer;
		}

		private CompletableFuture<ImageInstance> load() {
			if (future == null) {
				loadStart = Util.getMeasuringTimeMs();
				future = CompletableFuture.supplyAsync(() -> {
					try {
						var instance = instanceSupplier.call();
						setMessage(new LiteralText(instance.filename()));
						return instance;
					} catch (Exception e) {
						row.remove(this);
						return null;
					}
				});
			}
			return future;
		}

		@Override
		public void onPress() {
			client.openScreen(ImageScreen.create(GalleryScreen.this, load(), false));
		}

		@Override
		public void renderButton(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
			if (load().isDone()) {
				client.getTextureManager().bindTexture(load().join().id());
				drawTexture(guiGraphics, x, y, 0, 0, getWidth(), getHeight() - font.fontHeight - 2, getWidth(), getHeight() - font.fontHeight - 2);
				drawScrollingText(guiGraphics, font, 2, -1);
			} else {
				float delta = (float) easeInOutCubic((Util.getMeasuringTimeMs() - loadStart) % 1000f / 1000f);

				fill(guiGraphics, getX() + 2, getY() + 2, getXEnd() - 2, getYEnd() - font.fontHeight - 2, bgColor);
				drawHorizontalGradient(guiGraphics, getX() + 2, getY() + 2, getYEnd() - font.fontHeight - 2, lerp(delta, getX() + 2, getXEnd() - 2));

				fill(guiGraphics, getX() + 2, getYEnd() - font.fontHeight - 1, getXEnd() - 2, getYEnd() - 2, bgColor);
				drawHorizontalGradient(guiGraphics, getX() + 2, getYEnd() - font.fontHeight - 1, getYEnd() - 2, lerp(delta, getX() + 2, getXEnd() - 2));
			}
			DrawUtil.outlineRect(guiGraphics, getX(), getY(), getWidth(), getHeight(), row.list.isInListContent(mouseX, mouseY) && isHovered() ? -1 : bgColor);
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

		protected int getXEnd() {
			return x + getWidth();
		}

		private void drawHorizontalGradient(MatrixStack guiGraphics, int x1, int y1, int y2, int x2) {
			BufferBuilder consumer = Tessellator.getInstance().getBuffer();
			Matrix4f matrix4f = guiGraphics.peek().getModel();
			consumer.vertex(matrix4f, x1, y1, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
			consumer.vertex(matrix4f, x1, y2, 0).color(bgColor >> 16 & 255, bgColor >> 8 & 255, bgColor & 255, bgColor >> 24 & 255);
			consumer.vertex(matrix4f, x2, y2, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
			consumer.vertex(matrix4f, x2, y1, 0).color(accent >> 16 & 255, accent >> 8 & 255, accent & 255, accent >> 24 & 255);
			Tessellator.getInstance().draw();
		}

		private double easeInOutCubic(double x) {
			return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
		}

		private int lerp(float delta, int start, int end) {
			return (int) MathHelper.clamp(MathHelper.lerp(delta, start, end), start, end);
		}

		@Override
		protected @NotNull MutableText getNarrationMessage() {
			return new TranslatableText("gui.narrate.button", new TranslatableText("gallery.image.view"));
		}

		protected void drawScrollingText(MatrixStack guiGraphics, TextRenderer font, int offset, int color) {
			int i = this.x + offset;
			int j = this.x + this.getWidth() - offset;
			DrawUtil.drawScrollableText(guiGraphics, font, this.getMessage(), i, this.y + getHeight() - font.fontHeight - 1, j, this.y + this.getHeight(), color);
		}
	}

	private static class ImageListEntry extends ElementListWidget.Entry<ImageListEntry> {

		private final List<ImageEntry> buttons;
		private final int size;
		private final ImageList list;

		public ImageListEntry(int size, ImageList list) {
			this.size = size;
			buttons = new ArrayList<>(size);
			this.list = list;
		}

		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			if (Math.max(left, list.getRowLeft()) <= Math.min(left + width, list.getRowLeft() + list.getWidth()) - 1 &&
				Math.max(top - height, list.getY()) <= Math.min(top + height * 2, list.getY() + list.getHeight()) - 1) {
				buttons.forEach(e -> {
					e.y = top;
					e.render(guiGraphics, mouseX, mouseY, partialTick);
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
		public @NotNull List<? extends Element> children() {
			return buttons;
		}
	}

	private static class ImageList extends ElementListWidget<ImageListEntry> {

		private final int rowWidth;

		public ImageList(MinecraftClient minecraft, int screenWidth, int screenHeight, int top, int bottom, int entryHeight, int columns) {
			super(minecraft, screenWidth, screenHeight, top, bottom, entryHeight);
			this.rowWidth = columns * (entryWidth + entrySpacing) - entrySpacing;
		}

		@Override
		public int addEntry(ImageListEntry entry) {
			return super.addEntry(entry);
		}

		@Override
		public int getRowWidth() {
			return rowWidth;
		}

		@Override
		public int getRowLeft() {
			return this.left + this.width / 2 - this.getRowWidth() / 2;
		}

		@Override
		public boolean removeEntry(ImageListEntry entry) {
			return super.removeEntry(entry);
		}

		public void shiftEntries(ImageListEntry origin) {
			int originIndex = children().indexOf(origin);
			int lastIndex = children().size() - 1;
			if (originIndex == lastIndex) {
				return;
			}
			ImageListEntry next = getEntry(originIndex + 1);
			origin.add(next.pop());
		}

		@Override
		protected int getScrollbarPositionX() {
			return getRowLeft() + getRowWidth() + 6 + 2;
		}

		public boolean isInListContent(int x, int y) {
			return x >= left && x < right && y >= top && y < bottom;
		}

		public int getY() {
			return top;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return bottom - top;
		}
	}
}
