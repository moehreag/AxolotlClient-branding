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

import java.util.function.Predicate;

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ChatListWidget extends AlwaysSelectedEntryListWidget<ChatListWidget.ChatListEntry> {

	protected final Screen screen;

	public ChatListWidget(Screen screen, int screenWidth, int screenHeight, int x, int y, int width, int height, Predicate<Channel> predicate) {
		super(MinecraftClient.getInstance(), screenWidth, screenHeight, y, y + height, 25);
		left = x;
		right = x + width;
		this.screen = screen;
		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			list.stream().filter(predicate).forEach(c ->
				children().add(0, new ChatListEntry(c)))
		);
	}

	public ChatListWidget(Screen screen, int screenWidth, int screenHeight, int x, int y, int width, int height) {
		this(screen, screenWidth, screenHeight, x, y, width, height, c -> true);
	}

	public class ChatListEntry extends EntryListWidget.Entry<ChatListEntry> {

		private final Channel channel;
		private final ButtonWidget widget;

		public ChatListEntry(Channel channel) {
			this.channel = channel;
			widget = new ButtonWidget(0, 0, getRowWidth(), 20, Text.of(channel.getName()),
				buttonWidget -> client.openScreen(new ChatScreen(client.currentScreen, channel)));
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			widget.x = (x);
			widget.y = (y);
			widget.render(matrices, mouseX, mouseY, tickDelta);
		}
	}
}
