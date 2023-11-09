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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;

public class ChatWidget extends EntryListWidget {

	private final List<ChatMessage> messages = new ArrayList<>();
	private final List<ChatLine> entries = new ArrayList<>();
	private final Channel channel;
	private final MinecraftClient client;
	@Setter
	@Getter
	private int x, y, width, height;
	private final ContextMenuScreen screen;

	public ChatWidget(Channel channel, int x, int y, int width, int height, ContextMenuScreen screen) {
		super(MinecraftClient.getInstance(), width, height, y, y + height, 13);
		this.channel = channel;
		this.client = MinecraftClient.getInstance();
		setXPos(x + 5);

		setHeader(false, 0);
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		ThreadExecuter.scheduleTask(() ->
			Arrays.stream(channel.getMessages()).forEach(this::addMessage));
		ChatHandler.getInstance().setMessagesConsumer(chatMessages -> chatMessages.forEach(this::addMessage));
		ChatHandler.getInstance().setMessageConsumer(this::addMessage);
		ChatHandler.getInstance().setEnableNotifications(message -> !Arrays.stream(channel.getUsers()).collect(Collectors.toSet()).contains(message.getSender()));

		scroll(getMaxScroll());
	}


	@Override
	protected int getScrollbarPosition() {
		return x + width - 5;
	}

	@Override
	protected boolean isEntrySelected(int i) {
		return i == selectedEntry;
	}

	private void addMessage(ChatMessage message) {
		List<String> list = client.textRenderer.wrapLines(message.getContent(), getRowWidth());

		boolean scrollToBottom = getScrollAmount() == getMaxScroll();

		if (messages.size() > 0) {
			ChatMessage prev = messages.get(messages.size() - 1);
			if (!prev.getSender().equals(message.getSender())) {
				entries.add(new NameChatLine(message));
			} else {
				if (prev.getTimestamp() - message.getTimestamp() > 150) {
					entries.add(new NameChatLine(message));
				}
			}
		} else {
			entries.add(new NameChatLine(message));
		}

		list.forEach(t -> entries.add(new ChatLine(t, message)));
		messages.add(message);

		entries.sort(Comparator.comparingLong(c -> c.getOrigin().getTimestamp()));

		if (scrollToBottom) {
			scroll(getMaxScroll());
		}
		messages.sort(Comparator.comparingLong(ChatMessage::getTimestamp));
	}

	private void loadMessages() {
		long before;
		if (messages.size() > 0) {
			before = messages.get(0).getTimestamp();
		} else {
			before = Instant.now().getEpochSecond();
		}
		ChatHandler.getInstance().getMessagesBefore(channel, before);
	}

	@Override
	public void scroll(int i) {
		this.scrollAmount += (float) i;
		if (scrollAmount < 0) {
			loadMessages();
		}
		this.capYPosition();
		this.yDrag = -2;
	}

	public void remove() {
		ChatHandler.getInstance().setMessagesConsumer(ChatHandler.DEFAULT_MESSAGES_CONSUMER);
		ChatHandler.getInstance().setMessageConsumer(ChatHandler.DEFAULT_MESSAGE_CONSUMER);
		ChatHandler.getInstance().setEnableNotifications(ChatHandler.DEFAULT);
	}

	@Override
	protected void renderList(int i, int j, int k, int l) {
		DrawUtil.enableScissor(x, y, x + width, y + height);
		super.renderList(i, j, k, l);
		DrawUtil.disableScissor();
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	@Override
	protected int getEntryCount() {
		return entries.size();
	}

	public class ChatLine extends DrawUtil implements Entry {
		protected final MinecraftClient client = MinecraftClient.getInstance();
		@Getter
		private final String content;
		@Getter
		private final ChatMessage origin;

		public ChatLine(String content, ChatMessage origin) {
			super();
			this.content = content;
			this.origin = origin;
		}

		@Override
		public void updatePosition(int i, int j, int k) {

		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			if (button == 0) {
				ChatWidget.this.selectedEntry = index;
			}
			if (button == 1) {
				ContextMenu.Builder builder = ContextMenu.builder()
					.entry(origin.getSender().getName(), buttonWidget -> {
					})
					.spacer()
					.entry("api.friends.chat", buttonWidget -> {
						ChannelRequest.getOrCreateDM(origin.getSender().getUuid())
							.whenCompleteAsync((channel, throwable) -> client.setScreen(new ChatScreen(screen.getParent(), channel)));
					})
					.spacer()
					.entry("api.chat.report.message", buttonWidget -> {
						ChatHandler.getInstance().reportMessage(origin);
					})
					.spacer()
					.entry("action.copy", buttonWidget -> {
						Screen.setClipboard(origin.getContent());
					});
				screen.setContextMenu(builder.build());
			}
			return false;
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}

		protected void renderExtras(int x, int y, int mouseX, int mouseY) {
		}

		@Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			for (int i = 0; i < entries.size(); i++) {
				ChatLine l = entries.get(i);
				if (l.getOrigin().equals(origin)) {
					if (getEntryAt(mouseX, mouseY) == i) {
						hovered = true;
						break;
					}
				}
			}
			if (hovered && !screen.hasContextMenu()) {
				fill(x - 2 - 22, y - 2, x + entryWidth + 20, y + entryHeight - 1, 0x33FFFFFF);
				if (index < entries.size() - 1 && entries.get(index + 1).getOrigin().equals(origin)) {
					fill(x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight + 2, 0x33FFFFFF);
				}
				if ((index < entries.size() - 1 && !entries.get(index + 1).getOrigin().equals(origin)) || index == entries.size() - 1) {
					fill(x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight, 0x33FFFFFF);
				}
			}
			renderExtras(x, y, mouseX, mouseY);
			MinecraftClient.getInstance().textRenderer.draw(content, x, y, -1);
		}
	}

	public class NameChatLine extends ChatLine {

		private final String formattedTime;

		public NameChatLine(ChatMessage message) {
			super(new LiteralText(message.getSender().getName()).setStyle(new Style().setBold(true)).asFormattedString(), message);

			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy H:mm");
			formattedTime = DATE_FORMAT.format(new Date(message.getTimestamp()*1000));
		}

		@Override
		protected void renderExtras(int x, int y, int mouseX, int mouseY) {
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(getOrigin().getSender().getUuid(),
				getOrigin().getSender().getName()));
			drawTexture(x - 22, y, 8, 8, 8, 8, 18, 18, 64, 64);
			drawTexture(x - 22, y, 40, 8, 8, 8, 18, 18, 64, 64);
			GlStateManager.enableBlend();
			client.textRenderer.draw(formattedTime, client.textRenderer.getStringWidth(getContent()) + x + 5, y, Color.GRAY.getAsInt());
		}
	}
}
