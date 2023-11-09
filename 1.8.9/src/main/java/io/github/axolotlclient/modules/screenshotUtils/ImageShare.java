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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class ImageShare extends ImageNetworking {

	@Getter
	private static final ImageShare Instance = new ImageShare();

	private ImageShare() {
	}

	public void uploadImage(File file) {
		Util.sendChatMessage(new TranslatableText("imageUploadStarted"));
		upload(file).whenCompleteAsync((downloadUrl, throwable) -> {
			if (downloadUrl.isEmpty()) {
				Util.sendChatMessage(new TranslatableText("imageUploadFailure"));
			} else {
				Util.sendChatMessage(new LiteralText(I18n.translate("imageUploadSuccess") + " ")
					.append(new LiteralText(downloadUrl)
						.setStyle(new Style()
							.setUnderline(true)
							.setFormatting(Formatting.DARK_PURPLE)
							.setClickEvent(new ScreenshotUtils.CustomClickEvent(null) {
											   @Override
											   public void doAction() {
												   Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(downloadUrl), null);
											   }
										   }
							)
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(I18n.translate("clickToCopy")))))));
			}
		});
	}

	public ImageInstance downloadImage(String url){
		ImageData data = download(url);
		if(!data.getName().isEmpty()) {
			try {
				return new ImageInstance(ImageIO.read(new ByteArrayInputStream(data.getData())), data.getName());
			} catch (IOException ignored) {
			}
		}
		return null;
	}
}
