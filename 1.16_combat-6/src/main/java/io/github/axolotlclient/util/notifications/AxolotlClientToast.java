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

package io.github.axolotlclient.util.notifications;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AxolotlClientToast extends DrawUtil implements Toast {
	private static final Identifier BACKGROUND_SPRITE = new Identifier("axolotlclient", "textures/gui/sprites/toast/axolotlclient.png");
	private static final int DISPLAY_TIME_MILLIS = 5000;
	private static final int MAX_LINE_SIZE = 200;
	private static final int LINE_SPACING = 12;
	private static final int MARGIN = 10;
	private static final int DEFAULT_WIDTH = 160;
	private final Text title;
	private final List<OrderedText> messageLines;
	private final int width;

	public AxolotlClientToast(Text title, @Nullable Text message) {
		this(
			title,
			nullToEmpty(message),
			Math.max(DEFAULT_WIDTH, 15 + (2 * MARGIN) + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), message == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(message)))
		);
	}

	public static AxolotlClientToast multiline(MinecraftClient minecraft, Text title, Text message) {
		TextRenderer font = minecraft.textRenderer;
		List<OrderedText> list = font.wrapLines(message, MAX_LINE_SIZE);
		int i = Math.min(MAX_LINE_SIZE, list.stream().mapToInt(font::getWidth).max().orElse(MAX_LINE_SIZE));
		return new AxolotlClientToast(title, list, i + (2 * MARGIN) + 15);
	}

	private AxolotlClientToast(Text title, List<OrderedText> messageLines, int width) {
		this.title = title;
		this.messageLines = messageLines;
		this.width = width;
	}

	private static ImmutableList<OrderedText> nullToEmpty(@Nullable Text message) {
		return message == null ? ImmutableList.of() : ImmutableList.of(message.asOrderedText());
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return (2 * MARGIN) + Math.max(this.messageLines.size(), 1) * LINE_SPACING;
	}

	@Override
	public Visibility draw(MatrixStack stack, ToastManager manager, long startTime) {
		blitSprite(BACKGROUND_SPRITE, 0, 0, getWidth(), getHeight(), new NineSlice(160, 64, new Border(17, 30, 4, 4), false));
		TextRenderer font = MinecraftClient.getInstance().textRenderer;
		MinecraftClient.getInstance().getTextureManager().bindTexture(AxolotlClient.badgeIcon);
		drawTexture(stack, 4, 4, 0, 0, 15, 15, 15, 15);
		int textOffset = 22;
		if (this.messageLines.isEmpty()) {
			drawText(stack, title, textOffset, LINE_SPACING, -256, false);
		} else {
			drawText(stack, title, textOffset, 7, -256, false);

			for (int i = 0; i < this.messageLines.size(); i++) {
				font.draw(stack, this.messageLines.get(i), textOffset, 18 + i * LINE_SPACING, -1);
			}
		}

		return (double) startTime < (double) DISPLAY_TIME_MILLIS ? Visibility.SHOW : Visibility.HIDE;
	}
}
