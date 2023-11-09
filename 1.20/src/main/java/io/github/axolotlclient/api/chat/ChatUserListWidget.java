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

package io.github.axolotlclient.api.chat;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.handlers.FriendHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.modules.auth.Auth;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class ChatUserListWidget extends AlwaysSelectedEntryListWidget<ChatUserListWidget.UserListEntry> {

	private final ChatScreen screen;

	public ChatUserListWidget(ChatScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users) {
		users.forEach(user -> addEntry(new UserListEntry(user)));
	}

	@Override
	public int getRowWidth() {
		return width - 5;
	}

	public int addEntry(UserListEntry entry) {
		return super.addEntry(entry.init(screen));
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft() + width - 8;
	}

	@Override
	public boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	public class UserListEntry extends Entry<UserListEntry> {

		@Getter
		private final User user;
		private final MinecraftClient client;
		private long time;
		private Text note;
		private ChatScreen screen;

		public UserListEntry(User user, MutableText note) {
			this(user);
			this.note = note.formatted(Formatting.ITALIC);
		}

		public UserListEntry(User user) {
			this.client = MinecraftClient.getInstance();
			this.user = user;
		}

		public UserListEntry init(ChatScreen screen) {
			this.screen = screen;
			return this;
		}


		@Override
		public Text getNarration() {
			return Text.of(user.getName());
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (hovered && !screen.hasContextMenu()) {
				graphics.fill(x - 2, y - 1, x + entryWidth - 3, y + entryHeight + 1, 0x55ffffff);
			}
			drawScrollableText(graphics, client.textRenderer, Text.of(user.getName()), x + 3 + entryHeight,
				y + 1, x + entryWidth - 6, y + 1 + client.textRenderer.fontHeight + 2, -1);
			graphics.drawText(client.textRenderer, user.getStatus().getTitle(), x + 3 + entryHeight, y + 12, 8421504, false);
			if (user.getStatus().isOnline()) {
				graphics.drawText(client.textRenderer, user.getStatus().getDescription(), x + 3 + entryHeight + 7, y + 23, 8421504, false);
			}

			if (note != null) {
				graphics.drawText(client.textRenderer, note, x + entryWidth - client.textRenderer.getWidth(note) - 2, y + entryHeight - 10, 8421504, false);
			}

			RenderSystem.enableBlend();
			graphics.drawTexture(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()),
				x, y, entryHeight, entryHeight, 8, 8, 8, 8, 64, 64);
			graphics.drawTexture(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()),
				x, y, entryHeight, entryHeight, 40, 8, 8, 8, 64, 64);
			RenderSystem.disableBlend();
		}

		protected static void drawScrollableText(GuiGraphics graphics, TextRenderer textRenderer, Text text, int left, int top, int right, int bottom, int color) {
			int i = textRenderer.getWidth(text);
			int j = (top + bottom - 9) / 2 + 1;
			int k = right - left;
			if (i > k) {
				int l = i - k;
				double d = (double) Util.getMeasuringTimeMs() / 1000.0;
				double e = Math.max((double) l * 0.5, 3.0);
				double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
				double g = MathHelper.lerp(f, 0.0, l);
				graphics.enableScissor(left, top, right, bottom);
				graphics.drawShadowedText(textRenderer, text, left - (int) g, j, color);
				graphics.disableScissor();
			} else {
				graphics.drawShadowedText(textRenderer, text, left, j, color);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			ChatUserListWidget.this.setSelected(this);
			if (button == 0) { // left click
				if (Util.getMeasuringTimeMs() - this.time < 250L && client.world == null) { // left *double* click

				}
				this.time = Util.getMeasuringTimeMs();
			} else if (button == 1) { // right click


				if (!user.equals(API.getInstance().getSelf())) {
					ContextMenu.Builder menu = ContextMenu.builder()
						.entry(Text.of(user.getName()), buttonWidget -> {
						})
						.spacer()
						.entry(Text.translatable("api.friends.chat"), buttonWidget -> {
							ChannelRequest.getOrCreateDM(user.getUuid())
								.whenCompleteAsync((channel, throwable) -> client.setScreen(new ChatScreen(screen.getParent(), channel)));
						})
						.spacer()
						.entry(Text.translatable("api.chat.report.user"), buttonWidget -> {
							ChatHandler.getInstance().reportUser(user);
						});
					if (FriendHandler.getInstance().isBlocked(user.getUuid())) {
						menu.entry(Text.translatable("api.users.block"), buttonWidget ->
							FriendHandler.getInstance().blockUser(user.getUuid()));
					} else {
						menu.entry(Text.translatable("api.users.unblock"), buttonWidget ->
							FriendHandler.getInstance().unblockUser(user.getUuid()));
					}
					screen.setContextMenu(menu.build());
				}
			}

			return false;
		}
	}
}
