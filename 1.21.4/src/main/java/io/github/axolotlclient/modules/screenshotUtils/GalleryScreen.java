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

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.util.Watcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class GalleryScreen extends Screen {

	public static final Path SCREENSHOTS_DIR = FabricLoader.getInstance().getGameDir().resolve(Screenshot.SCREENSHOT_DIR);

	private record Tab<T>(Component title, Callable<List<T>> list, Map<T, ImageInstance> loadingCache,
						  GalleryScreen.Tab.Loader<T> loader) {

		private static <T> Tab<T> of(Component title, Callable<List<T>> list, GalleryScreen.Tab.Loader<T> loader) {
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

		private static final Tab<Path> LOCAL = of(Component.translatable("gallery.title.local"), () -> {
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

		private static final Tab<String> SHARED = of(Component.translatable("gallery.title.shared"), () ->
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
		super(Component.translatable("gallery.title"));
		this.parent = parent;
		this.current = Tab.LOCAL;
		this.watcher = Watcher.createSelfTicking(SCREENSHOTS_DIR, () -> {
			if (current == Tab.LOCAL) {
				rebuildWidgets();
			}
		});
	}

	private static final int entrySpacing = 4,
		entryWidth = 100,
		entryHeight = 75,
		marginLeftRight = 10;

	@Override
	protected void init() {
		boolean online = API.getInstance().isAuthenticated();
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		layout.setHeaderHeight(40);
		LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(4));
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(title, font));
		if (online) {
			header.addChild(new StringWidget(current.title(), font));
		}

		int columnCount = (width - (marginLeftRight * 2) + entrySpacing - 13) / (entryWidth + entrySpacing); // -13 to always have enough space for the scrollbar

		final var area = new ImageList(minecraft, layout.getWidth(), layout.getContentHeight(), layout.getHeaderHeight(), entryHeight + entrySpacing, columnCount);

		layout.addToContents(area, LayoutSettings::alignHorizontallyLeft);
		setInitialFocus(area);
		CompletableFuture.runAsync(() -> {
			try {
				loadTab(current, columnCount, area);
			} catch (Exception e) {
				LinearLayout error = LinearLayout.vertical().spacing(8);
				error.defaultCellSetting().alignVerticallyMiddle();
				error.addChild(new StringWidget(Component.translatable("gallery.error.loading"), font));
				setInitialFocus(error.addChild(Button.builder(Component.translatable("gallery.reload"), b -> rebuildWidgets()).build()));
				layout.addToContents(error);
			}
		});

		var footer = layout.addToFooter(LinearLayout.horizontal()).spacing(4);
		footer.defaultCellSetting().alignHorizontallyCenter();
		int buttonWidth = columnCount <= 5 && online ? 100 : 150;
		if (online) {
			Button.Builder switchTab;
			if (current == Tab.SHARED) {
				switchTab = Button.builder(Component.translatable("gallery.tab.local"), b -> setTab(Tab.LOCAL));
			} else {
				switchTab = Button.builder(Component.translatable("gallery.tab.shared"), b -> setTab(Tab.SHARED));
			}
			footer.addChild(switchTab.width(buttonWidth).build());
		}
		footer.addChild(Button.builder(Component.translatable("gallery.download_external"), b -> minecraft.setScreen(new DownloadImageScreen(this)))
			.width(buttonWidth).build());
		footer.addChild(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
			.width(buttonWidth).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void onClose() {
		Tab.LOCAL.loadingCache().forEach((path, instance) -> minecraft.getTextureManager().release(instance.id()));
		Tab.LOCAL.loadingCache().clear();
		Tab.SHARED.loadingCache().forEach((s, instance) -> minecraft.getTextureManager().release(instance.id()));
		Tab.SHARED.loadingCache().clear();
		Watcher.close(watcher);
		minecraft.setScreen(parent);
	}

	private void setTab(Tab<?> tab) {
		current = tab;
		rebuildWidgets();
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
	private class ImageEntry extends Button {

		private static final int bgColor = Colors.DARK_GRAY.toInt();
		private static final int accent = Colors.GRAY.withBrightness(0.5f).withAlpha(128).toInt();

		private final Font font;
		private final Callable<ImageInstance> instanceSupplier;
		private final ImageListEntry row;
		private long loadStart;
		private CompletableFuture<ImageInstance> future;

		protected ImageEntry(int width, int height, Callable<ImageInstance> instanceSupplier, ImageListEntry row) {
			super(0, 0, width, height, Component.empty(), b -> {
			}, DEFAULT_NARRATION);
			this.instanceSupplier = instanceSupplier;
			this.row = row;
			this.font = Minecraft.getInstance().font;
		}

		private CompletableFuture<ImageInstance> load() {
			if (future == null) {
				loadStart = Util.getMillis();
				future = CompletableFuture.supplyAsync(() -> {
					try {
						var instance = instanceSupplier.call();
						setMessage(Component.literal(instance.filename()));
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
			minecraft.setScreen(ImageScreen.create(GalleryScreen.this, load(), false));
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			if (load().isDone()) {
				guiGraphics.blit(RenderType::guiTextured, load().join().id(), getX(), getY(), 0, 0, getWidth(), getHeight() - font.lineHeight - 2, getWidth(), getHeight() - font.lineHeight - 2);
				renderString(guiGraphics, font, -1);
			} else {
				float delta = (float) easeInOutCubic((Util.getMillis() - loadStart) % 1000f / 1000f);

				guiGraphics.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - font.lineHeight - 2, bgColor);
				drawHorizontalGradient(guiGraphics, getX() + 2, getY() + 2, getBottom() - font.lineHeight - 2, lerp(delta, getX() + 2, getRight() - 2));

				guiGraphics.fill(getX() + 2, getBottom() - font.lineHeight - 1, getRight() - 2, getBottom() - 2, bgColor);
				drawHorizontalGradient(guiGraphics, getX() + 2, getBottom() - font.lineHeight - 1, getBottom() - 2, lerp(delta, getX() + 2, getRight() - 2));
			}
			guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused() ? -1 : bgColor);
		}

		private void drawHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int y2, int x2) {
			VertexConsumer consumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.gui());
			Matrix4f matrix4f = guiGraphics.pose().last().pose();
			consumer.addVertex(matrix4f, x1, y1, 0).setColor(ImageEntry.bgColor);
			consumer.addVertex(matrix4f, x1, y2, 0).setColor(ImageEntry.bgColor);
			consumer.addVertex(matrix4f, x2, y2, 0).setColor(ImageEntry.accent);
			consumer.addVertex(matrix4f, x2, y1, 0).setColor(ImageEntry.accent);
		}

		private double easeInOutCubic(double x) {
			return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
		}

		private int lerp(float delta, int start, int end) {
			return (int) Mth.clamp(Mth.lerp(delta, start, end), start, end);
		}

		@Override
		protected @NotNull MutableComponent createNarrationMessage() {
			return wrapDefaultNarrationMessage(Component.translatable("gallery.image.view"));
		}

		@Override
		protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
			int i = this.getX() + width;
			int j = this.getX() + this.getWidth() - width;
			renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY() + getHeight() - font.lineHeight - 1, j, this.getY() + this.getHeight(), color);
		}
	}

	private static class ImageListEntry extends ContainerObjectSelectionList.Entry<ImageListEntry> {

		private final List<ImageEntry> buttons;
		private final int size;
		private final ImageList list;

		public ImageListEntry(int size, ImageList list) {
			this.size = size;
			buttons = new ArrayList<>(size);
			this.list = list;
		}

		@Override
		public @NotNull List<? extends NarratableEntry> narratables() {
			return buttons;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			if (Math.max(left, list.getX()) <= Math.min(left + width, list.getX() + list.getWidth()) - 1 &&
				Math.max(top - height, list.getY()) <= Math.min(top + height * 2, list.getY() + list.getHeight()) - 1) {
				buttons.forEach(e -> {
					e.setY(top);
					e.render(guiGraphics, mouseX, mouseY, partialTick);
				});
			} else {
				buttons.forEach(e -> {
					e.setY(top);
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
				e.setX(x);
				x += e.getWidth() + entrySpacing;
			}
		}

		public ImageEntry pop() {
			var entry = buttons.removeFirst();
			if (buttons.isEmpty()) {
				list.removeEntry(this);
			} else if (buttons.size() < size) {
				list.shiftEntries(this);
			}
			repositionButtons();
			return entry;
		}

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return buttons;
		}
	}

	private static class ImageList extends ContainerObjectSelectionList<ImageListEntry> {

		private final int rowWidth;

		public ImageList(Minecraft minecraft, int i, int j, int k, int l, int columns) {
			super(minecraft, i, j, k, l);
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
			return this.getX() + this.width / 2 - this.getRowWidth() / 2;
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
	}
}
