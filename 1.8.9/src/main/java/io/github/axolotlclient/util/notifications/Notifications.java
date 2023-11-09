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

package io.github.axolotlclient.util.notifications;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;

public class Notifications implements NotificationProvider {

	// ---------------------- Basic Notification System because 1.8.9 has none by itself (except Achievements which only work in worlds) --------------------------

	@Getter
	private static final Notifications Instance = new Notifications();
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final List<Status> statusQueue = new ArrayList<>();
	private Status currentStatus;
	private long statusCreationTime;
	private int lastX;
	private boolean fading;

	public void addStatus(String title, String description, Object... args) {
		if (statusQueue.isEmpty() && currentStatus == null) {
			setStatus(new Status(I18n.translate(title, args), I18n.translate(description, args)));
		} else {
			statusQueue.add(new Status(I18n.translate(title, args), I18n.translate(description, args)));
		}
	}

	private void setStatus(Status status) {
		currentStatus = status;
		statusCreationTime = MinecraftClient.getTime();
		lastX = Util.getWindow().getWidth();
		fading = false;
		client.getSoundManager().play(new PositionedSoundInstance(new Identifier("random.bow"), 0.5F, 0.4F / (0.5F + 0.8F), 1, 0, 0));
	}

	public void renderStatus() {
		if (currentStatus != null) {
			int x = lastX;
			x -= currentStatus.getWidth() + 5;
			if (MinecraftClient.getTime() - statusCreationTime < 100) {
				lastX -= lastX / 45;
			} else if (MinecraftClient.getTime() - statusCreationTime > 2000) {
				if (!fading) {
					client.getSoundManager().play(new PositionedSoundInstance(new Identifier("random.bow"), 0.5F, 0.4F / (0.5F + 0.8F), 1, 0, 0));
				}
				fading = true;
				lastX += lastX / 45;
			}
			DrawableHelper.fill(x - 5 - 1, 0, Util.getWindow().getWidth(), 20 + currentStatus.getDescription().size() * 12 + 1, 0xFF0055FF);
			DrawableHelper.fill(x - 5, 0, Util.getWindow().getWidth(), 20 + currentStatus.getDescription().size() * 12, 0xFF00CFFF);
			client.textRenderer.draw(currentStatus.getTitle(), x, 7, -256, true);
			for (int i = 0; i < currentStatus.getDescription().size(); i++) {
				client.textRenderer.draw(currentStatus.getDescription().get(i), x, 18 + i * 12, -1, true);
			}
			if (x > Util.getWindow().getWidth() + 20) {
				currentStatus = null;
				if (!statusQueue.isEmpty()) {
					Status s = statusQueue.get(0);
					statusQueue.remove(0);
					setStatus(s);
				}
			}
		}
	}


	private static class Status {
		@Getter
		private final String title;
		@Getter
		private final List<String> description;
		@Getter
		private final int width;

		public Status(String title, String description) {
			this.title = title;
			width = Math.max(
				160,
				Math.max(MinecraftClient.getInstance().textRenderer.getStringWidth(title),
					description == null ? 0 : MinecraftClient.getInstance().textRenderer.getStringWidth(description)));
			//this.width = description.stream().mapToInt(MinecraftClient.getInstance().textRenderer::getStringWidth).max().orElse(200);
			this.description = MinecraftClient.getInstance().textRenderer.wrapLines(description, width);
		}
	}
}

