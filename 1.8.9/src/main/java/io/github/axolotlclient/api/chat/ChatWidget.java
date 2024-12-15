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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ChatsSidebar;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.util.ClientColors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;

public class ChatWidget extends EntryListWidget {

	private final List<ChatMessage> messages = new ArrayList<>();
	private final List<ChatLine> entries = new ArrayList<>();
	private final Channel channel;
	private final Minecraft client;
	private final ContextMenuScreen screen;
	private int selectedEntry = -1;

	public ChatWidget(Channel channel, int x, int y, int width, int height, ContextMenuScreen screen) {
		super(Minecraft.getInstance(), width, height, y, y + height, 13);
		this.channel = channel;
		this.client = Minecraft.getInstance();
		super.setX(x + 5);

		setHeader(false, 0);
		this.screen = screen;
		channel.getMessages().forEach(this::addMessage);

		ChatHandler.getInstance().setMessagesConsumer(chatMessages -> chatMessages.forEach(this::addMessage));
		ChatHandler.getInstance().setMessageConsumer(this::addMessage);
		ChatHandler.getInstance().setEnableNotifications(message -> !message.channelId().equals(channel.getId()));

		scroll(getMaxScroll());
		renderSelectionHighlight = false;
	}

	@Override
	protected int getScrollbarPosition() {
		return minX + width - 6;
	}

	@Override
	public int getRowWidth() {
		return width - 60;
	}

	public int getX() {
		return minX;
	}

	protected boolean isEntrySelected(int i) {
		return i == selectedEntry;
	}

	private void addMessage(ChatMessage message) {
		List<String> list = client.textRenderer.split(message.content(), getRowWidth());

		boolean scrollToBottom = getScrollAmount() == getMaxScroll();

		if (!messages.isEmpty()) {
			ChatMessage prev = messages.get(messages.size()-1);
			if (!(prev.sender().equals(message.sender()) && prev.senderDisplayName().equals(message.senderDisplayName()))) {
				entries.add(new NameChatLine(message));
			} else {
				if (prev.timestamp().getEpochSecond() - message.timestamp().getEpochSecond() > 150) {
					entries.add(new NameChatLine(message));
				}
			}
		} else {
			entries.add(new NameChatLine(message));
		}

		list.forEach(t -> entries.add(new ChatLine(t, message)));
		messages.add(message);

		entries.sort(Comparator.comparingLong(c -> c.getOrigin().timestamp().getEpochSecond()));

		if (scrollToBottom) {
			scroll(getMaxScroll());
		}
		messages.sort(Comparator.comparingLong(value -> value.timestamp().getEpochSecond()));
	}

	private void loadMessages() {
		long before;
		if (!messages.isEmpty()) {
			before = messages.get(0).timestamp().getEpochSecond();
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
		this.capScrolling();
		this.mouseYStart = -2;
	}

	public void remove() {
		ChatHandler.getInstance().setMessagesConsumer(ChatHandler.DEFAULT_MESSAGES_CONSUMER);
		ChatHandler.getInstance().setMessageConsumer(ChatHandler.DEFAULT_MESSAGE_CONSUMER);
		ChatHandler.getInstance().setEnableNotifications(ChatHandler.DEFAULT);
	}

	@Override
	protected void renderList(int i, int j, int k, int l) {
		DrawUtil.enableScissor(minX, minY, maxX, maxY);
		super.renderList(i, j, k, l);
		DrawUtil.disableScissor();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (screen.getSelf() instanceof ChatsSidebar) {
			if (this.visible) {
				this.mouseX = mouseX;
				this.mouseY = mouseY;
				this.capScrolling();
				int m = this.minX + this.width / 2 - this.getRowWidth() / 2 + 2;
				int n = this.minY + 4 - (int) this.scrollAmount;

				this.renderList(m, n, mouseX, mouseY);
			}
		} else {
			super.render(mouseX, mouseY, delta);
		}
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	@Override
	protected int size() {
		return entries.size();
	}

	public class ChatLine extends DrawUtil implements Entry {
		protected final Minecraft client = Minecraft.getInstance();
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
		public void renderOutOfBounds(int i, int j, int k) {

		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			if (button == 0) {
				ChatWidget.this.selectedEntry = index;
				return true;
			}
			if (button == 1) {
				ContextMenu.Builder builder = ContextMenu.builder()
					.entry(origin.sender().getName(), buttonWidget -> {
					})
					.spacer();
				if (!origin.sender().equals(API.getInstance().getSelf())) {
					builder.entry("api.friends.chat", buttonWidget -> {
							ChannelRequest.getOrCreateDM(origin.sender())
								.whenCompleteAsync((channel, throwable) -> client.submit(() -> client.openScreen(new ChatScreen(screen.getParent(), channel))));
						})
						.spacer();
				}
				builder.entry("api.chat.report.message", buttonWidget -> {
						Screen previous = client.screen;
						client.openScreen(new ConfirmScreen((b, i) -> {
							if (b) {
								ChatHandler.getInstance().reportMessage(origin);
							}
							client.openScreen(previous);
						}, I18n.translate("api.channels.confirm_report"),
							I18n.translate("api.channels.confirm_report.desc", origin.content()), 0));
					})
					.spacer()
					.entry("action.copy", buttonWidget -> {
						Screen.setClipboard(origin.content());
					});
				screen.setContextMenu(builder.build());
				return true;
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
			client.textRenderer.draw(content, x, y, -1);
		}
	}

	public class NameChatLine extends ChatLine {

		private final String formattedTime;

		public NameChatLine(ChatMessage message) {
			super(new LiteralText(message.senderDisplayName())
				.setStyle(new Style().setBold(true)).getFormattedString(), message);

			DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy H:mm");
			formattedTime = DATE_FORMAT.format(message.timestamp().atZone(ZoneId.systemDefault()));
		}

		@Override
		protected void renderExtras(int x, int y, int mouseX, int mouseY) {
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
			client.getTextureManager().bind(Auth.getInstance().getSkinTexture(getOrigin().sender().getUuid(),
				getOrigin().sender().getName()));
			drawTexture(x - 22, y, 8, 8, 8, 8, 18, 18, 64, 64);
			drawTexture(x - 22, y, 40, 8, 8, 8, 18, 18, 64, 64);
			GlStateManager.enableBlend();
			client.textRenderer.draw(formattedTime, client.textRenderer.getWidth(getContent()) + x + 5, y, ClientColors.GRAY.toInt());
		}
	}
}
