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
import java.util.function.Predicate;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;

public class ChatListWidget extends EntryListWidget {

	protected final ContextMenuScreen screen;
	private final Predicate<Channel> predicate;

	private final List<ChatListEntry> entries = new ArrayList<>();

	public ChatListWidget(ContextMenuScreen screen, int screenWidth, int screenHeight, int x, int y, int width, int height, Predicate<Channel> filter) {
		super(Minecraft.getInstance(), width, height, y, y + height, 25);
		minX = x;
		maxX = x+width;
		this.screen = screen;
		this.predicate = filter;
	}

	public void addChannels(List<Channel> channels) {
		channels.stream().filter(predicate).forEach(c -> entries.add(0, new ChatListEntry(c)));
	}

	@Override
	public int getRowWidth() {
		return width - 8;
	}

	public ChatListWidget(ContextMenuScreen screen, int screenWidth, int screenHeight, int x, int y, int width, int height) {
		this(screen, screenWidth, screenHeight, x, y, width, height, c -> true);
		ChannelRequest.getChannelList().thenAccept(this::addChannels);
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	@Override
	protected int size() {
		return entries.size();
	}

	public class ChatListEntry implements Entry {

		private final Channel channel;
		private final ButtonWidget widget;

		public ChatListEntry(Channel channel) {
			this.channel = channel;
			widget = new ButtonWidget(-1, 0, 0, getRowWidth(), 20, channel.getName());
		}

		@Override
		public void renderOutOfBounds(int i, int j, int k) {

		}

		@Override
		public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
			widget.x = (x);
			widget.y = (y);
			widget.render(minecraft, mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int m, int n) {
			if (widget.isMouseOver(minecraft, mouseX, mouseY)) {
				if (button == 0) {
					minecraft.openScreen(new ChatScreen(ChatListWidget.this.screen.getSelf(), channel));
					return true;
				} else if (button == 1) {
					ContextMenu.Builder builder = ContextMenu.builder()
							.entry(channel.getName(), w -> {
							})
							.spacer()
							.entry(I18n.translate("api.channel.configure"), w -> minecraft.openScreen(new ChannelSettingsScreen(ChatListWidget.this.screen.getSelf(), channel)))
							.spacer();
					if (channel.getOwner().equals(API.getInstance().getSelf())) {
						builder.entry(I18n.translate("api.channel.delete"), w -> ChannelRequest.leaveOrDeleteChannel(channel));
					} else {
						builder.entry(I18n.translate("api.channel.leave"), w -> ChannelRequest.leaveOrDeleteChannel(channel));
					}
					ChatListWidget.this.screen.setContextMenu(builder.build());
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
