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

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

public class ChatListWidget extends EntryListWidget {

	protected final Screen screen;

	private final List<ChatListEntry> entries = new ArrayList<>();

	public ChatListWidget(Screen screen, int screenWidth, int screenHeight, int x, int y, int width, int height, Predicate<Channel> predicate) {
		super(MinecraftClient.getInstance(), screenWidth, screenHeight, y, y + height, 25);
		xStart = x;
		xEnd = x + width;
		this.screen = screen;
		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			list.stream().filter(predicate).forEach(c ->
				entries.add(0, new ChatListEntry(c)))
		);
	}

	public ChatListWidget(Screen screen, int screenWidth, int screenHeight, int x, int y, int width, int height) {
		this(screen, screenWidth, screenHeight, x, y, width, height, c -> true);
	}

	@Override
	protected boolean isEntrySelected(int i) {
		return i == selectedEntry;
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	@Override
	protected int getEntryCount() {
		return entries.size();
	}

	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY) {
		DrawUtil.enableScissor(x, this.yStart, x + this.width, y + height);
		super.renderList(x, y, mouseX, mouseY);
		DrawUtil.disableScissor();
	}

	public class ChatListEntry implements Entry {

		private final Channel channel;
		private final ButtonWidget widget;

		public ChatListEntry(Channel channel) {
			this.channel = channel;
			widget = new ButtonWidget(0, 0, 0, getRowWidth(), 20, channel.getName());
		}

		@Override
		public void updatePosition(int i, int j, int k) {

		}

		@Override
		public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
			widget.x = (x);
			widget.y = (y);
			widget.render(client, mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(int i, int j, int k, int l, int m, int n) {
			if (widget.isHovered()) {
				client.setScreen(new ChatScreen(client.currentScreen, channel));
			}
			return false;
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}
	}
}
