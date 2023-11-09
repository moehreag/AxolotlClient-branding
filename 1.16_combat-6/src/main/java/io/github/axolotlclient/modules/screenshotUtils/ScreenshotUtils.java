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

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.GenericOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import lombok.AllArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class ScreenshotUtils extends AbstractModule {

	private static final ScreenshotUtils Instance = new ScreenshotUtils();
	private final OptionCategory category = new OptionCategory("screenshotUtils");
	private final BooleanOption enabled = new BooleanOption("enabled", false);
	private final List<Action> actions = Util.make(() -> {
		List<Action> actions = new ArrayList<>();
		actions.add(new Action("copyAction", Formatting.AQUA,
			"copy_image",
			new CustomClickEvent((file) -> {
				FileTransferable selection = new FileTransferable(file);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
			})));

		actions.add(new Action("deleteAction", Formatting.LIGHT_PURPLE,
			"delete_image",
			new CustomClickEvent((file) -> {
				try {
					Files.delete(file.toPath());
					io.github.axolotlclient.util.Util.sendChatMessage(
						new LiteralText(I18n.translate("screenshot_deleted").replace("<name>", file.getName())));
				} catch (Exception e) {
					AxolotlClient.LOGGER.warn("Couldn't delete Screenshot " + file.getName());
				}
			})));

		actions.add(new Action("openAction", Formatting.WHITE,
			"open_image",
			new CustomClickEvent((file) -> Util.getOperatingSystem().open(file.toURI()))));

		actions.add(new Action("uploadAction", Formatting.LIGHT_PURPLE,
			"upload_image",
			new CustomClickEvent(file -> {
				new Thread("Image Uploader") {
					@Override
					public void run() {
						ImageShare.getInstance().uploadImage(file);
					}
				}.start();
			})));

		// If you have further ideas to what actions could be added here, please let us know!

		return actions;
	});

	private final EnumOption autoExec = new EnumOption("autoExec", Util.make(() -> {
		List<String> names = new ArrayList<>();
		names.add("off");
		actions.forEach(action -> names.add(action.getName()));
		return names.toArray(new String[0]);
	}), "off");

	public static ScreenshotUtils getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled, autoExec, new GenericOption("imageViewer", "openViewer", (m1, m2) -> {
			MinecraftClient.getInstance().openScreen(new ImageViewerScreen(MinecraftClient.getInstance().currentScreen));
		}));

		AxolotlClient.CONFIG.general.addSubCategory(category);
	}

	public Text onScreenshotTaken(MutableText text, File shot) {
		if (enabled.get()) {
			Text t = getUtilsText(shot);
			if (t != null) {
				return text.append("\n").append(t);
			}
		}
		return text;
	}

	private @Nullable Text getUtilsText(File file) {
		if (!autoExec.get().equals("off")) {
			actions.parallelStream().filter(action -> autoExec.get().equals(action.getName())).collect(Collectors.toList())
				.get(0).clickEvent.setFile(file).doAction();
			return null;
		}

		MutableText message = LiteralText.EMPTY.copy();
		actions.parallelStream().map(action -> action.getText(file)).iterator().forEachRemaining(text -> {
			message.append(text);
			message.append(" ");
		});
		return message;
	}

	interface OnActionCall {

		void doAction(File file);
	}

	@AllArgsConstructor
	public static class Action {

		private final String translationKey;
		private final Formatting formatting;
		private final String hoverTextKey;
		private final CustomClickEvent clickEvent;

		public Text getText(File file) {
			return new TranslatableText(translationKey).setStyle(Style.EMPTY.withFormatting(formatting)
				.withClickEvent(clickEvent.setFile(file)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText(hoverTextKey))));
		}

		public String getName() {
			return translationKey;
		}
	}

	@AllArgsConstructor
	protected static class FileTransferable implements Transferable {

		private final File file;

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) {
			final ArrayList<File> files = new ArrayList<>();
			files.add(file);
			return files;
		}
	}

	public static class CustomClickEvent extends ClickEvent {

		private final OnActionCall action;
		private File file;

		public CustomClickEvent(OnActionCall action) {
			super(Action.byName(""), "");
			this.action = action;
		}

		public void doAction() {
			if (file != null) {
				action.doAction(file);
			} else {
				AxolotlClient.LOGGER.warn("How'd you manage to do this? "
					+ "Now there's a screenshot ClickEvent without a File attached to it!");
			}
		}

		public CustomClickEvent setFile(File file) {
			this.file = file;
			return this;
		}
	}
}
