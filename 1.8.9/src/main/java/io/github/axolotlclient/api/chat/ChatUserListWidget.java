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

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.handlers.FriendHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;

public class ChatUserListWidget extends EntryListWidget {

	private final List<UserListEntry> entries = new ArrayList<>();
	private final ChatScreen screen;

	public ChatUserListWidget(ChatScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users) {
		users.forEach(user -> addEntry(new UserListEntry(user)));
	}

	@Override
	protected int getScrollbarPosition() {
		return this.xStart + this.width / 2 - this.getRowWidth() / 2 + 2 + width - 8;
	}

	@Override
	protected int getEntryCount() {
		return entries.size();
	}

	@Override
	public int getRowWidth() {
		return width - 5;
	}

	public void addEntry(UserListEntry entry) {
		entries.add(entry.init(screen));
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	@Override
	protected boolean isEntrySelected(int i) {
		return i == selectedEntry;
	}

	@Override
	protected void renderDecorations(int i, int j) {
		super.renderDecorations(i, j);
		GlStateManager.enableTexture();
		client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);

	}

	public class UserListEntry extends DrawableHelper implements Entry {

		@Getter
		private final User user;
		private final MinecraftClient client;
		private long time;
		private String note;
		private ChatScreen screen;

		public UserListEntry(User user, String note) {
			this(user);
			this.note = Formatting.ITALIC + note + Formatting.RESET;
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
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			if (hovered && !screen.hasContextMenu()) {
				DrawableHelper.fill(x - 2, y - 1, x + entryWidth - 3, y + entryHeight + 1, 0x55ffffff);
			}
			DrawUtil.drawScrollableText(client.textRenderer, user.getName(), x + 3 + entryHeight,
				y + 1, x + entryWidth - 6, y + 1 + client.textRenderer.fontHeight + 2, -1);
			client.textRenderer.draw(user.getStatus().getTitle(), x + 3 + entryHeight, y + 12, 8421504);
			if (user.getStatus().isOnline()) {
				client.textRenderer.draw(user.getStatus().getDescription(), x + 3 + entryHeight + 7, y + 23, 8421504);
			}

			if (note != null) {
				client.textRenderer.draw(note, x + entryWidth - client.textRenderer.getStringWidth(note) - 2, y + entryHeight - 10, 8421504);
			}

			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()));
			GlStateManager.enableBlend();
			GlStateManager.color(1, 1, 1);
			drawTexture(x, y, 8, 8, 8, 8, entryHeight, entryHeight, 64, 64);
			drawTexture(x, y, 40, 8, 8, 8, entryHeight, entryHeight, 64, 64);
			GlStateManager.disableBlend();
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			ChatUserListWidget.this.selectedEntry = index;
			if (button == 0) { // left click
				if (MinecraftClient.getTime() - this.time < 250L && client.world == null) { // left *double* click

				}
				this.time = MinecraftClient.getTime();
			} else if (button == 1) { // right click


				if (!user.equals(API.getInstance().getSelf())) {
					ContextMenu.Builder menu = ContextMenu.builder()
						.entry(user.getName(), buttonWidget -> {
						})
						.spacer()
						.entry("api.friends.chat", buttonWidget -> {
							ChannelRequest.getOrCreateDM(user.getUuid())
								.whenCompleteAsync((channel, throwable) -> client.setScreen(new ChatScreen(screen.getParent(), channel)));
						})
						.spacer()
						.entry("api.chat.report.user", buttonWidget -> {
							ChatHandler.getInstance().reportUser(user);
						});
					if (!FriendHandler.getInstance().isBlocked(user.getUuid())) {
						menu.entry(I18n.translate("api.users.block"), buttonWidget ->
							FriendHandler.getInstance().blockUser(user.getUuid()));
					} else {
						menu.entry(I18n.translate("api.users.unblock"), buttonWidget ->
							FriendHandler.getInstance().unblockUser(user.getUuid()));
					}
					screen.setContextMenu(menu.build());
				}
			}

			return false;
		}

		@Override
		public void updatePosition(int i, int j, int k) {

		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}
	}
}
