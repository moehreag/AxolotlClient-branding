package io.github.axolotlclient.modules.screenshotUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.UUIDHelper;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.notifications.Notifications;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ImageScreen extends Screen {

	private final Screen parent;
	private final ImageInstance image;

	public ImageScreen(Screen parent, ImageInstance instance) {
		super(Component.literal(instance.filename()));
		this.parent = parent;
		this.image = instance;
	}

	@Override
	protected void init() {
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		LinearLayout header = layout.addToHeader(LinearLayout.vertical()).spacing(4);
		header.defaultCellSetting().alignHorizontallyCenter();
		header.addChild(new StringWidget(getTitle(), font));

		double imgAspectRatio = image.image().getWidth() / (double) image.image().getHeight();
		int imageWidth = Math.min((int) (layout.getContentHeight() * imgAspectRatio), layout.getWidth() - 20 - 60);
		int imageHeight = (int) (imageWidth / imgAspectRatio);

		var contents = layout.addToContents(LinearLayout.horizontal().spacing(4));
		var footer = layout.addToFooter(LinearLayout.horizontal().spacing(4));
		contents.addChild(new ImageElement(imageWidth, imageHeight));
		var actions = contents.addChild(LinearLayout.vertical()).spacing(4);
		if (image instanceof ImageInstance.Remote remote) {
			header.addChild(new StringWidget(Component.translatable("gallery.image.upload_details", UUIDHelper.getUsername(remote.uploader()), remote.sharedAt().atZone(ZoneId.systemDefault()).format(AxolotlClientCommon.getInstance().formatter)), font));
			if (!(image instanceof ImageInstance.Local)) {
				actions.addChild(Button.builder(Component.translatable("gallery.image.save"), b -> {
					b.active = false;
					try {
						Path out = saveSharedImage(remote);
						minecraft.setScreen(new ImageScreen(parent, remote.toShared(out)));
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.save.failure", "gallery.image.save.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to save shared image!", e);
					}
				}).width(50).build());
				actions.addChild(Button.builder(Component.translatable("gallery.image.copy"), b -> {
					try {
						ScreenshotCopying.copy(DrawUtil.writeToByteArray(image.image()));
					} catch (IOException e) {
						Notifications.getInstance().addStatus("gallery.image.copy.failure", "gallery.image.copy.failure.description", e.getMessage());
						AxolotlClient.LOGGER.warn("Failed to copy shared image!", e);
					}
				}).width(50).build());
			}
		}
		if (image instanceof ImageInstance.Local local) {
			if (API.getInstance().isAuthenticated() && !(image instanceof ImageInstance.Remote)) {
				actions.addChild(Button.builder(Component.translatable("gallery.image.upload"), b -> {
					b.active = false;
					ImageShare.getInstance().upload(local.location()).thenAccept(s -> {
						if (s.isEmpty()) {
							Notifications.getInstance().addStatus("gallery.image.upload.failure", "gallery.image.upload.description");
						} else {
							minecraft.execute(() -> minecraft.setScreen(new ImageScreen(parent, local.toShared(s, API.getInstance().getSelf().getUuid(), Instant.now()))));
							minecraft.keyboardHandler.setClipboard(s);
							Notifications.getInstance().addStatus("gallery.image.upload.success", "gallery.image.upload.success.description", s);
						}
					});
				}).width(50).build());
			}
			actions.addChild(Button.builder(Component.translatable("gallery.image.copy"), b -> ScreenshotCopying.copy(local.location())).width(50).build());
			actions.addChild(Button.builder(Component.translatable("gallery.image.open.external"), b -> Util.getPlatform().openPath(local.location())).width(50).build());
		}
		if (image instanceof ImageInstance.Remote remote) {
			actions.addChild(Button.builder(Component.translatable("gallery.image.open.external.browser"), b -> Util.getPlatform().openUri(remote.url())).width(50).build());
			actions.addChild(Button.builder(Component.translatable("gallery.image.copy_url"), b -> minecraft.keyboardHandler.setClipboard(remote.url())).width(50).build());
		}

		footer.addChild(Button.builder(CommonComponents.GUI_BACK, b -> minecraft.setScreen(parent)).build());

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	private Path saveSharedImage(ImageInstance.Remote remote) throws IOException {
		Path out = GalleryScreen.SCREENSHOT_DIR.resolve("shared")
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
