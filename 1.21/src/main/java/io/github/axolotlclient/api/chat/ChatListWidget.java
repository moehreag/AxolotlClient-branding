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
import java.util.function.Predicate;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.list.ElementListWidget;
import net.minecraft.text.Text;

public class ChatListWidget extends ElementListWidget<ChatListWidget.ChatListEntry> {

	protected final ContextMenuScreen screen;
	private final Predicate<Channel> predicate;

	public ChatListWidget(ContextMenuScreen screen, int screenWidth, int screenHeight, int x, int y, int width, int height, Predicate<Channel> filter) {
		super(MinecraftClient.getInstance(), width, height, y, 25);
		setX(x);
		this.screen = screen;
		this.predicate = filter;
	}

	public void addChannels(List<Channel> channels) {
		channels.stream().filter(predicate).forEach(c -> addEntryToTop(new ChatListEntry(c)));
	}

	@Override
	public int getRowWidth() {
		return getWidth() - 8;
	}

	@Override
	protected boolean isZero(int index) {
		return true;
	}

	public ChatListWidget(ContextMenuScreen screen, int screenWidth, int screenHeight, int x, int y, int width, int height) {
		this(screen, screenWidth, screenHeight, x, y, width, height, c -> true);
		ChannelRequest.getChannelList().thenAccept(this::addChannels);
	}

	public class ChatListEntry extends Entry<ChatListEntry> {

		private final Channel channel;
		private final ButtonWidget widget;

		public ChatListEntry(Channel channel) {
			this.channel = channel;
			widget = ButtonWidget.builder(Text.of(channel.getName()),
					buttonWidget -> client.setScreen(new ChatScreen(screen.getParent(), channel)))
				.width(getRowWidth()).build();
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			widget.setX(x);
			widget.setY(y);
			widget.render(graphics, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(widget);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (widget.isMouseOver(mouseX, mouseY)) {
				if (button == 0) {
					return widget.mouseClicked(mouseX, mouseY, button);
				} else if (button == 1) {
					ContextMenu.Builder builder = ContextMenu.builder()
						.entry(Text.of(channel.getName()), w -> {
						})
						.spacer()
						.entry(Text.translatable("api.channel.configure"), w -> client.setScreen(new ChannelSettingsScreen(ChatListWidget.this.screen.getSelf(), channel)))
						.spacer();
					if (channel.getOwner().equals(API.getInstance().getSelf())) {
						builder.entry(Text.translatable("api.channel.delete"), w ->
							ChannelRequest.leaveOrDeleteChannel(channel).whenComplete((o, throwable) -> client.execute(() -> client.setScreen(screen.getSelf()))));
					} else {
						builder.entry(Text.translatable("api.channel.leave"), w ->
							ChannelRequest.leaveOrDeleteChannel(channel).whenComplete((o, throwable) -> client.execute(() -> client.setScreen(screen.getSelf()))));
					}
					ChatListWidget.this.screen.setContextMenu(builder.build());
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(widget);
		}
	}
}
