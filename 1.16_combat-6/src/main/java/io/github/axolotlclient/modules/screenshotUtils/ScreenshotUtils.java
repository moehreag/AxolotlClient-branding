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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class ScreenshotUtils extends AbstractModule {

	@Getter
	private static final ScreenshotUtils Instance = new ScreenshotUtils();
	private final OptionCategory category = OptionCategory.create("screenshotUtils");
	private final BooleanOption enabled = new BooleanOption("enabled", false);
	private final Map<BooleanSupplier, Action> actions = Util.make(() -> {
		Map<BooleanSupplier, Action> actions = new LinkedHashMap<>();
		actions.put(() -> true, new Action("copyAction", Formatting.AQUA,
			"copy_image",
			ScreenshotCopying::copy));

		actions.put(() -> true, new Action("deleteAction", Formatting.LIGHT_PURPLE,
			"delete_image",
			(file) -> {
				try {
					Files.delete(file);
					io.github.axolotlclient.util.Util.sendChatMessage(
						new LiteralText(I18n.translate("screenshot_deleted").replace("<name>", file.getFileName().toString())));
				} catch (Exception e) {
					AxolotlClient.LOGGER.warn("Couldn't delete Screenshot " + file.getFileName().toString());
				}
			}));

		actions.put(() -> true, new Action("openAction", Formatting.WHITE,
			"open_image",
			(file) -> Util.getOperatingSystem().open(file.toUri())));

		actions.put(() -> true, new Action("viewInGalleryAction", Formatting.LIGHT_PURPLE, "view_in_gallery",
			file -> {
				try {
					ImageInstance instance = new ImageInstance.LocalImpl(file);
					MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().openScreen(ImageScreen.create(null, CompletableFuture.completedFuture(instance), true)));
				} catch (Exception ignored) {
					io.github.axolotlclient.util.Util.sendChatMessage(new TranslatableText("screenshot.gallery.view.error"));
				}
			}));

		actions.put(() -> API.getInstance().isAuthenticated(), new Action("uploadAction", Formatting.AQUA,
			"upload_image",
			ImageShare.getInstance()::uploadImage));

		return actions;
	});

	private final StringArrayOption autoExec = new StringArrayOption("autoExec", Util.make(() -> {
		List<String> names = new ArrayList<>();
		names.add("off");
		actions.forEach((condition, action) -> names.add(action.getName()));
		return names.toArray(new String[0]);
	}), "off");

	@Override
	public void init() {
		category.add(enabled, autoExec, new GenericOption("imageViewer", "openViewer", () -> {
			MinecraftClient.getInstance().openScreen(new GalleryScreen(MinecraftClient.getInstance().currentScreen));
		}));

		AxolotlClient.CONFIG.general.add(category);
	}

	public MutableText onScreenshotTaken(MutableText text, File shot) {
		if (enabled.get()) {
			MutableText t = getUtilsText(shot.toPath());
			if (t != null) {
				return text.append("\n").append(t);
			}
		}
		return text;
	}

	private @Nullable MutableText getUtilsText(Path file) {
		if (!autoExec.get().equals("off")) {
			actions.forEach((condition, action) -> {
				if (condition.getAsBoolean() && autoExec.get().equals(action.getName())) {
					action.getClickEvent(file).doAction();
				}
			});
			return null;
		}

		MutableText message = LiteralText.EMPTY.copy();
		actions.forEach((condition, action) -> {
			if (condition.getAsBoolean()) {
				message.append(action.getText(file)).append(" ");
			}
		});
		return message;
	}

	public interface OnActionCall {

		void doAction(Path file);
	}

	@AllArgsConstructor
	public static class Action {

		private final String translationKey;
		private final Formatting formatting;
		private final String hoverTextKey;
		private final OnActionCall clickEvent;

		public Text getText(Path file) {
			return new TranslatableText(translationKey).setStyle(Style.EMPTY.withFormatting(formatting)
				.withClickEvent(getClickEvent(file)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText(hoverTextKey))));
		}

		public String getName() {
			return translationKey;
		}

		public CustomClickEvent getClickEvent(Path file) {
			return new CustomClickEvent(clickEvent, file);
		}
	}

	public static class CustomClickEvent extends ClickEvent {

		private final OnActionCall action;
		private final Path file;

		public CustomClickEvent(OnActionCall action, Path file) {
			super(Action.byName(""), "");
			this.action = action;
			this.file = file;
		}

		public void doAction() {
			if (file != null) {
				action.doAction(file);
			} else {
				AxolotlClient.LOGGER.warn("How'd you manage to do this? "
					+ "Now there's a screenshot ClickEvent without a File attached to it!");
			}
		}
	}
}
