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

package io.github.axolotlclient.api.chat;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.EntryListWidget;

public class ChatUserListWidget extends EntryListWidget {

	private final List<UserListEntry> entries = new ArrayList<>();
	private final ChatScreen screen;
	private int selectedEntry = -1;

	public ChatUserListWidget(ChatScreen screen, Minecraft client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users, Channel channel) {
		users.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName())).forEach(user -> addEntry(new UserListEntry(user, channel)));
	}

	@Override
	protected int getScrollbarPosition() {
		return this.minX + this.width / 2 - this.getRowWidth() / 2 + 2 + width - 8;
	}

	@Override
	protected int size() {
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
		minecraft.getTextureManager().bind(GuiElement.BACKGROUND_LOCATION);

	}

	public class UserListEntry extends GuiElement implements EntryListWidget.Entry {

		@Getter
		private final User user;
		private final Minecraft client;
		private final Channel channel;
		private long time;
		private ChatScreen screen;

		public UserListEntry(User user, Channel channel) {
			this.client = Minecraft.getInstance();
			this.user = user;
			this.channel = channel;
		}

		public UserListEntry init(ChatScreen screen) {
			this.screen = screen;
			return this;
		}

		@Override
		public void renderOutOfBounds(int i, int j, int k) {

		}

		@Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			if (hovered) {
				fill(x - 2, y - 1, x + entryWidth - 3, y + entryHeight + 1, 0x55ffffff);
			}
			DrawUtil.drawScrollableText(client.textRenderer, user.getName(), x + 3 + entryHeight, y + 1, x + entryWidth - 6, y + 1 + client.textRenderer.fontHeight + 2, -1);
			DrawUtil.drawScrollableText(client.textRenderer, user.getStatus().getTitle(), x + 3 + entryHeight,
				y + 12, x + entryWidth - 6, y + 12 + client.textRenderer.fontHeight + 2, 8421504);

			client.getTextureManager().bind(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()));
			GlStateManager.enableBlend();
			GlStateManager.color3f(1, 1, 1);
			drawTexture(x, y, 8, 8, 8, 8, entryHeight, entryHeight, 64, 64);
			drawTexture(x, y, 40, 8, 8, 8, entryHeight, entryHeight, 64, 64);
			GlStateManager.disableBlend();
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			ChatUserListWidget.this.selectedEntry = index;
			if (button == 0) { // left click
				if (Minecraft.getTime() - this.time < 250L && client.world == null) { // left *double* click

				}
				this.time = Minecraft.getTime();
			} else if (button == 1) { // right click

				if (!user.equals(API.getInstance().getSelf())) {
					ContextMenu.Builder menu = ContextMenu.builder().entry(user.getName(), buttonWidget -> {})
						.spacer();
					if (!channel.isDM()) {
						menu.entry("api.friends.chat", buttonWidget -> {
							ChannelRequest.getOrCreateDM(user)
								.whenCompleteAsync((channel, throwable) -> client.submit(() -> client.openScreen(new ChatScreen(screen.getParent(), channel))));
						}).spacer();
					}
					if (!FriendRequest.getInstance().isBlocked(user.getUuid())) {
						menu.entry("api.users.block", buttonWidget ->
							FriendRequest.getInstance().blockUser(user.getUuid()));
					} else {
						menu.entry("api.users.unblock", buttonWidget ->
							FriendRequest.getInstance().unblockUser(user.getUuid()));
					}
					if (channel.getOwner().equals(API.getInstance().getSelf())) {
						menu.spacer().entry("api.channel.remove_user", b -> ChannelRequest.removeUserFromChannel(channel, user));
					}
					screen.setContextMenu(menu.build());
					return true;
				}
			}

			return false;
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}
	}
}
