package io.github.axolotlclient.modules.screenshotUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import io.github.axolotlclient.util.ThreadExecuter;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GalleryScreen extends Screen {

	public static final Path SCREENSHOT_DIR = FabricLoader.getInstance().getGameDir().resolve(Screenshot.SCREENSHOT_DIR);

	private final Screen parent;

	public GalleryScreen(Screen parent) {
		super(Component.translatable("gallery.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		HeaderAndFooterLayout header = new HeaderAndFooterLayout(this);
		header.addTitleHeader(title, font);

		int entryWidth = 100;
		int entryHeight = 75;

		int marginLeftRight = 10;
		int entrySpacing = 10;

		int columnCount = (width - (marginLeftRight * 2)) / (entryWidth + entrySpacing);

		try (Stream<Path> screenshots = Files.list(SCREENSHOT_DIR)) {
			final var area = new ImageList(minecraft, header.getWidth(), header.getContentHeight(), header.getHeaderHeight(), entryHeight + entrySpacing, columnCount * (entryWidth + entrySpacing) + marginLeftRight);

			List<Path> images = screenshots.sorted(Comparator.<Path>comparingLong(p -> {
				try {
					return Files.getLastModifiedTime(p).toMillis();
				} catch (IOException e) {
					return 0L;
				}
			}).reversed()).toList();
			for (int i = 0; i < images.size(); i += columnCount) {
				ImageListEntry row = new ImageListEntry(columnCount, area);
				int entryX = area.getRowLeft();
				for (int x = 0; x < columnCount; x++) {
					if (i + x >= images.size()) {
						break;
					}
					Path p = images.get(i + x);
					var entry = new ImageEntry(entryX, entryWidth, entryHeight, () -> new ImageInstance.LocalImpl(p), row);
					row.buttons.add(entry);
					entryX += entryWidth + entrySpacing;
				}
				area.addEntry(row);
			}

			header.addToContents(area, LayoutSettings::alignHorizontallyLeft);
			setInitialFocus(area);
		} catch (IOException e) {
			LinearLayout error = LinearLayout.vertical().spacing(8);
			error.defaultCellSetting().alignVerticallyMiddle();
			error.addChild(new StringWidget(Component.translatable("gallery.error.loading"), font));
			error.addChild(Button.builder(Component.translatable("gallery.reload"), b -> rebuildWidgets()).build());
			header.addToContents(error);
		}

		var footer = header.addToFooter(LinearLayout.horizontal()).spacing(4);
		footer.addChild(Button.builder(Component.translatable("viewScreenshot"), b -> minecraft.setScreen(new DownloadImageScreen(this))).build());
		footer.addChild(Button.builder(CommonComponents.GUI_BACK, b -> minecraft.setScreen(parent))
			.build());

		header.arrangeElements();
		header.visitWidgets(this::addRenderableWidget);
	}

	// This image list is loading its entries lazily! :)
	private class ImageEntry extends Button {

		private ImageInstance instance;
		private final Font font;
		private final Callable<ImageInstance> instanceSupplier;
		private final ImageListEntry row;
		private boolean loading;

		protected ImageEntry(int x, int width, int height, Callable<ImageInstance> instanceSupplier, ImageListEntry row) {
			super(x, 0, width, height, Component.empty(), b -> {
			}, DEFAULT_NARRATION);
			this.instanceSupplier = instanceSupplier;
			this.row = row;
			this.font = Minecraft.getInstance().font;
		}

		private boolean load() {
			if (instance == null && !loading) {
				loading = true;
				ThreadExecuter.scheduleTask(() -> {
					try {
						instance = instanceSupplier.call();
						setMessage(Component.literal(instance.filename()));
					} catch (Exception e) {
						minecraft.execute(() -> row.remove(this));
					}
				});
			}
			return instance != null;
		}

		@Override
		public void onPress() {
			if (load()) {
				minecraft.setScreen(new ImageScreen(GalleryScreen.this, instance));
			}
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			if (load()) {
				guiGraphics.blit(RenderType::guiTextured, instance.id(), getX(), getY(), 0, 0, getWidth(), getHeight() - font.lineHeight - 2, getWidth(), getHeight() - font.lineHeight - 2);
				renderString(guiGraphics, font, -1);
				if (isHoveredOrFocused()) {
					guiGraphics.renderOutline(getX(), getY(), getWidth(), getHeight(), -1);
				}
			}
		}

		@Override
		protected MutableComponent createNarrationMessage() {
			return wrapDefaultNarrationMessage(Component.translatable("gallery.image.view"));
		}

		@Override
		protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
			int i = this.getX() + width;
			int j = this.getX() + this.getWidth() - width;
			renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY() + getHeight() - font.lineHeight + 1, j, this.getY() + this.getHeight(), color);
		}
	}

	private static class ImageListEntry extends ContainerObjectSelectionList.Entry<ImageListEntry> {

		private final List<ImageEntry> buttons;
		private final ImageList list;

		public ImageListEntry(int size, ImageList list) {
			buttons = new ArrayList<>(size);
			this.list = list;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return buttons;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			if (!new ScreenRectangle(left, top, width, height).overlaps(list.getRectangle())) {
				buttons.forEach(e -> {
					e.setY(top);
				});
				return;
			}
			buttons.forEach(e -> {
				e.setY(top);
				e.render(guiGraphics, mouseX, mouseY, partialTick);
			});
		}

		public void remove(ImageEntry e) {
			buttons.remove(e);
			if (buttons.isEmpty()) {
				list.removeEntry(this);
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return buttons;
		}
	}

	private static class ImageList extends ContainerObjectSelectionList<ImageListEntry> {

		private final int rowWidth;

		public ImageList(Minecraft minecraft, int i, int j, int k, int l, int rowWidth) {
			super(minecraft, i, j, k, l);
			this.rowWidth = rowWidth;
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
		public boolean removeEntry(ImageListEntry entry) {
			return super.removeEntry(entry);
		}
	}
}
