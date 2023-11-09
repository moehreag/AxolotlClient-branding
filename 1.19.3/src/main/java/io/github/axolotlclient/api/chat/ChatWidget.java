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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ChatWidget extends AlwaysSelectedEntryListWidget<ChatWidget.ChatLine> {

	private final List<ChatMessage> messages = new ArrayList<>();
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
		setLeftPos(x + 5);

		setRenderHeader(false, 0);
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		Arrays.stream(channel.getMessages()).forEach(this::addMessage);

		ChatHandler.getInstance().setMessagesConsumer(chatMessages -> chatMessages.forEach(this::addMessage));
		ChatHandler.getInstance().setMessageConsumer(this::addMessage);
		ChatHandler.getInstance().setEnableNotifications(message -> !Arrays.stream(channel.getUsers()).collect(Collectors.toUnmodifiableSet()).contains(message.getSender()));

		setScrollAmount(getMaxScroll());
	}

	@Override
	protected int getScrollbarPositionX() {
		return x + width - 5;
	}

	private void addMessage(ChatMessage message) {
		List<OrderedText> list = client.textRenderer.wrapLines(Text.of(message.getContent()), getRowWidth());

		boolean scrollToBottom = getScrollAmount() == getMaxScroll();

		if (messages.size() > 0) {
			ChatMessage prev = messages.get(messages.size() - 1);
			if (!prev.getSender().equals(message.getSender())) {
				addEntry(new NameChatLine(message));
			} else {
				if (prev.getTimestamp() - message.getTimestamp() > 150) {
					addEntry(new NameChatLine(message));
				}
			}
		} else {
			addEntry(new NameChatLine(message));
		}

		list.forEach(t -> addEntry(new ChatLine(t, message)));
		messages.add(message);

		children().sort(Comparator.comparingLong(c -> c.getOrigin().getTimestamp()));

		if (scrollToBottom) {
			setScrollAmount(getMaxScroll());
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
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		double scrollAmount = (this.getScrollAmount() - amount * (double) this.itemHeight / 2.0);
		if (scrollAmount < 0) {
			loadMessages();
		}
		setScrollAmount(scrollAmount);
		return true;
	}

	public void remove() {
		ChatHandler.getInstance().setMessagesConsumer(ChatHandler.DEFAULT_MESSAGES_CONSUMER);
		ChatHandler.getInstance().setMessageConsumer(ChatHandler.DEFAULT_MESSAGE_CONSUMER);
		ChatHandler.getInstance().setEnableNotifications(ChatHandler.DEFAULT);
	}

	@Override
	protected void drawEntrySelectionHighlight(MatrixStack matrices, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
		int i = this.left + (this.width - entryWidth) / 2;
		int j = this.left + (this.width + entryWidth) / 2;
		fill(matrices, i, y - 2, j, y + entryHeight, borderColor);
		fill(matrices, i + 1, y - 1, j - 1, y + entryHeight - 1, fillColor);
	}

	@Override
	protected void renderList(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		DrawUtil.enableScissor(this.x, this.y, this.x + width, this.y + height);
		super.renderList(matrices, mouseX, mouseY, delta);
		DrawUtil.disableScissor();
	}

	public class ChatLine extends AlwaysSelectedEntryListWidget.Entry<ChatLine> {
		protected final MinecraftClient client = MinecraftClient.getInstance();
		@Getter
		private final OrderedText content;
		@Getter
		private final ChatMessage origin;

		public ChatLine(OrderedText content, ChatMessage origin) {
			super();
			this.content = content;
			this.origin = origin;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				ChatWidget.this.setSelected(this);
			}
			if (button == 1) {
				ContextMenu.Builder builder = ContextMenu.builder()
					.entry(Text.of(origin.getSender().getName()), buttonWidget -> {
					})
					.spacer()
					.entry(Text.translatable("api.friends.chat"), buttonWidget -> {
						ChannelRequest.getOrCreateDM(origin.getSender().getUuid())
							.whenComplete((channel, throwable) -> client.setScreen(new ChatScreen(screen.getParent(), channel)));
					})
					.spacer()
					.entry(Text.translatable("api.chat.report.message"), buttonWidget -> {
						ChatHandler.getInstance().reportMessage(origin);
					})
					.spacer()
					.entry(Text.translatable("action.copy"), buttonWidget -> {
						client.keyboard.setClipboard(origin.getContent());
					});
				screen.setContextMenu(builder.build());
			}
			return false;
		}

		protected void renderExtras(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			for (ChatLine l : children()) {
				if (l.getOrigin().equals(origin)) {
					if (Objects.equals(getHoveredEntry(), l)) {
						hovered = true;
						break;
					}
				}
			}
			if (hovered && !screen.hasContextMenu()) {
				fill(matrices, x - 2 - 22, y - 2, x + entryWidth + 20, y + entryHeight - 1, 0x33FFFFFF);
				if (index < children().size() - 1 && children().get(index + 1).getOrigin().equals(origin)) {
					fill(matrices, x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight + 2, 0x33FFFFFF);
				}
				if ((index < children().size() - 1 && !children().get(index + 1).getOrigin().equals(origin)) || index == children().size() - 1) {
					fill(matrices, x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight, 0x33FFFFFF);
				}
			}
			renderExtras(matrices, x, y, mouseX, mouseY);
			MinecraftClient.getInstance().textRenderer.draw(matrices, content, x, y, -1);
		}

		@Override
		public Text getNarration() {
			return Text.of(origin.getContent());
		}
	}

	public class NameChatLine extends ChatLine {

		private final String formattedTime;

		public NameChatLine(ChatMessage message) {
			super(Text.literal(message.getSender().getName()).setStyle(Style.EMPTY.withBold(true)).asOrderedText(), message);

			SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d/M/yyyy H:mm");
			formattedTime = DATE_FORMAT.format(new Date(message.getTimestamp()*1000));
		}

		@Override
		protected void renderExtras(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
			RenderSystem.disableBlend();
			RenderSystem.setShaderTexture(0, Auth.getInstance().getSkinTexture(getOrigin().getSender().getUuid(),
				getOrigin().getSender().getName()));
			drawTexture(matrices, x - 22, y, 18, 18, 8, 8, 8, 8, 64, 64);
			drawTexture(matrices, x - 22, y, 18, 18, 40, 8, 8, 8, 64, 64);
			RenderSystem.enableBlend();
			client.textRenderer.draw(matrices, formattedTime, client.textRenderer.getWidth(getContent()) + x + 5, y, Color.GRAY.getAsInt());
		}
	}
}
