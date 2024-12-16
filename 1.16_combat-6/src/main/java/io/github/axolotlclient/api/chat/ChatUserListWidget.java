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

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class ChatUserListWidget extends AlwaysSelectedEntryListWidget<ChatUserListWidget.UserListEntry> {

	private final ChatScreen screen;

	public ChatUserListWidget(ChatScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, bottom - top, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users, Channel channel) {
		users.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName())).forEach(user -> addEntry(new UserListEntry(user, channel)));
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
		private final Channel channel;
		private long time;
		private ChatScreen screen;

		public UserListEntry(User user, Channel channel) {
			this.client = MinecraftClient.getInstance();
			this.user = user;
			this.channel = channel;
		}

		public UserListEntry init(ChatScreen screen) {
			this.screen = screen;
			return this;
		}


		@Override
		public void render(MatrixStack graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (hovered && !screen.hasContextMenu()) {
				fill(graphics, x - 2, y - 1, x + entryWidth - 3, y + entryHeight + 1, 0x55ffffff);
			}
			DrawUtil.drawScrollableText(graphics, client.textRenderer, Text.of(user.getName()), x + 3 + entryHeight,
				y + 1, x + entryWidth - 6, y + 1 + client.textRenderer.fontHeight + 2, -1);
			DrawUtil.drawScrollableText(graphics, client.textRenderer, Text.of(user.getStatus().getTitle()), x + 3 + entryHeight,
				y + 12, x + entryWidth - 6, y + 12 + client.textRenderer.fontHeight + 2, 8421504);

			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()));
			RenderSystem.enableBlend();
			drawTexture(graphics, x, y, entryHeight, entryHeight, 8, 8, 8, 8, 64, 64);
			drawTexture(graphics, x, y, entryHeight, entryHeight, 40, 8, 8, 8, 64, 64);
			RenderSystem.disableBlend();
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
					ContextMenu.Builder menu = ContextMenu.builder().entry(Text.of(user.getName()), buttonWidget -> {
					}).spacer();
					if (!channel.isDM()) {
						menu.entry(new TranslatableText("api.friends.chat"), buttonWidget -> {
							ChannelRequest.getOrCreateDM(user).whenCompleteAsync(((channel, throwable) ->
								client.execute(() -> client.openScreen(new ChatScreen(screen.getParent(), channel)))));
						}).spacer();
					}
					if (!FriendRequest.getInstance().isBlocked(user.getUuid())) {
						menu.entry(new TranslatableText("api.users.block"), buttonWidget ->
							FriendRequest.getInstance().blockUser(user));
					} else {
						menu.entry(new TranslatableText("api.users.unblock"), buttonWidget ->
							FriendRequest.getInstance().unblockUser(user));
					}
					if (channel.getOwner().equals(API.getInstance().getSelf())) {
						menu.spacer().entry(new TranslatableText("api.channel.remove_user"), b -> ChannelRequest.removeUserFromChannel(channel, user));
					}
					screen.setContextMenu(menu.build());
					return true;
				}
			}

			return false;
		}
	}
}
