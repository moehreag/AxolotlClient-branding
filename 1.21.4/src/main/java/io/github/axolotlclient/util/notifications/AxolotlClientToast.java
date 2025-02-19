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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxolotlClientToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.fromNamespaceAndPath("axolotlclient", "toast/axolotlclient");
	private static final int DISPLAY_TIME_MILLIS = 5000;
	private static final int MAX_LINE_SIZE = 200;
	private static final int LINE_SPACING = 12;
	private static final int MARGIN = 10;
	private final Component title;
	private final List<FormattedCharSequence> messageLines;
	private long lastChanged;
	private boolean changed;
	private final int width;
	private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

	public AxolotlClientToast(Component title, @Nullable Component message) {
		this(
			title,
			nullToEmpty(message),
			Math.max(DEFAULT_WIDTH, 2 * MARGIN + 15 + Math.max(Minecraft.getInstance().font.width(title), message == null ? 0 : Minecraft.getInstance().font.width(message)))
		);
	}

	public static AxolotlClientToast multiline(Minecraft minecraft, Component title, Component message) {
		Font font = minecraft.font;
		List<FormattedCharSequence> list = font.split(message, MAX_LINE_SIZE);
		int i = Math.min(MAX_LINE_SIZE, Math.max(font.width(title), list.stream().mapToInt(font::width).max().orElse(MAX_LINE_SIZE)));
		return new AxolotlClientToast(title, list, i + 2 * MARGIN + 15);
	}

	private AxolotlClientToast(Component title, List<FormattedCharSequence> messageLines, int width) {
		this.title = title;
		this.messageLines = messageLines;
		this.width = width;
	}

	private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component message) {
		return message == null ? ImmutableList.of() : ImmutableList.of(message.getVisualOrderText());
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return 2 * MARGIN + Math.max(this.messageLines.size(), 1) * LINE_SPACING;
	}

	@Override
	public Toast.@NotNull Visibility getWantedVisibility() {
		return this.wantedVisibility;
	}

	@Override
	public void update(ToastManager toastManager, long visibilityTime) {
		if (this.changed) {
			this.lastChanged = visibilityTime;
			this.changed = false;
		}

		double d = (double) DISPLAY_TIME_MILLIS * toastManager.getNotificationDisplayTimeMultiplier();
		long l = visibilityTime - this.lastChanged;
		this.wantedVisibility = (double) l < d ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, long visibilityTime) {
		guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		guiGraphics.blit(RenderType::guiTextured, AxolotlClient.badgeIcon, 4, 4, 0, 0, 15, 15, 15, 15);
		int textOffset = 22;
		if (this.messageLines.isEmpty()) {
			guiGraphics.drawString(font, this.title, textOffset, LINE_SPACING, -256, false);
		} else {
			guiGraphics.drawString(font, this.title, textOffset, 7, -256, false);

			for (int i = 0; i < this.messageLines.size(); i++) {
				guiGraphics.drawString(font, this.messageLines.get(i), textOffset, 18 + i * LINE_SPACING, -1, false);
			}
		}
	}
}
