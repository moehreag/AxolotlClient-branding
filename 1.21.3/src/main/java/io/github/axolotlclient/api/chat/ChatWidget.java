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
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.ChatMessage;
import io.github.axolotlclient.modules.auth.Auth;
import io.github.axolotlclient.util.ClientColors;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class ChatWidget extends ObjectSelectionList<ChatWidget.ChatLine> {

	private final List<ChatMessage> messages = new ArrayList<>();
	private final Channel channel;
	private final Minecraft client;
	private final ContextMenuScreen screen;
	@Setter @Getter private int x, y, width, height;

	public ChatWidget(Channel channel, int x, int y, int width, int height, ContextMenuScreen screen) {
		super(Minecraft.getInstance(), width, height, y, 13);
		this.channel = channel;
		this.client = Minecraft.getInstance();
		setX(x + 5);

		setRenderHeader(false, 0);
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		channel.getMessages().forEach(this::addMessage);

		ChatHandler.getInstance().setMessagesConsumer(chatMessages -> chatMessages.forEach(this::addMessage));
		ChatHandler.getInstance().setMessageConsumer(this::addMessage);
		ChatHandler.getInstance().setEnableNotifications(message -> !message.channelId().equals(channel.getId()));

		setScrollAmount(getMaxScroll());
	}

	@Override
	protected int getScrollbarPosition() {
		return x + width - 6;
	}

	@Override
	public int getRowWidth() {
		return width - 60;
	}

	private void addMessage(ChatMessage message) {
		List<FormattedCharSequence> list = client.font.split(Component.literal(message.content()), getRowWidth());

		boolean scrollToBottom = getScrollAmount() == getMaxScroll();

		if (!messages.isEmpty()) {
			ChatMessage prev = messages.getLast();
			if (!(prev.sender().equals(message.sender()) &&
				  prev.senderDisplayName().equals(message.senderDisplayName()))) {
				addEntry(new NameChatLine(message));
			} else {
				if (prev.timestamp().getEpochSecond() - message.timestamp().getEpochSecond() > 150) {
					addEntry(new NameChatLine(message));
				}
			}
		} else {
			addEntry(new NameChatLine(message));
		}

		list.forEach(t -> addEntry(new ChatLine(t, message)));
		messages.add(message);

		children().sort(Comparator.comparingLong(c -> c.getOrigin().timestamp().getEpochSecond()));

		if (scrollToBottom) {
			setScrollAmount(getMaxScroll());
		}
		messages.sort(Comparator.comparingLong(value -> value.timestamp().getEpochSecond()));
	}

	private void loadMessages() {
		long before;
		if (!messages.isEmpty()) {
			before = messages.getFirst().timestamp().getEpochSecond();
		} else {
			before = Instant.now().getEpochSecond();
		}
		ChatHandler.getInstance().getMessagesBefore(channel, before);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
		double scrollAmount = (this.getScrollAmount() - amountY * (double) this.itemHeight / 2.0);
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
	protected void renderSelection(GuiGraphics graphics, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
	}

	@Override
	protected boolean isValidMouseClick(int index) {
		return true;
	}

	public class ChatLine extends Entry<ChatLine> {
		protected final Minecraft client = ChatWidget.this.client;
		@Getter private final FormattedCharSequence content;
		@Getter private final ChatMessage origin;

		public ChatLine(FormattedCharSequence content, ChatMessage origin) {
			super();
			this.content = content;
			this.origin = origin;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				ChatWidget.this.setSelected(this);
				return true;
			}
			if (button == 1) {
				ContextMenu.Builder builder =
					ContextMenu.builder().entry(Component.literal(origin.sender().getName()), buttonWidget -> {
					}).spacer();
				if (!origin.sender().equals(API.getInstance().getSelf())) {
					builder.entry(Component.translatable("api.friends.chat"), buttonWidget -> {
						ChannelRequest.getOrCreateDM(origin.sender()).whenCompleteAsync(
							(channel, throwable) -> client.execute(
								() -> client.setScreen(new ChatScreen(screen.getParent(), channel))));
					}).spacer();
				}
				builder.entry(Component.translatable("api.chat.report.message"), buttonWidget -> {
					Screen previous = client.screen;
					client.setScreen(new ConfirmScreen(b -> {
						if (b) {
							ChatHandler.getInstance().reportMessage(origin);
						}
						client.setScreen(previous);
					}, Component.translatable("api.channels.confirm_report"), Component.translatable("api.channels.confirm_report.desc", origin.content())));
				}).spacer().entry(Component.translatable("action.copy"), buttonWidget -> {
					client.keyboardHandler.setClipboard(origin.content());
				});
				screen.setContextMenu(builder.build());
				return true;
			}
			return false;
		}

		protected void renderExtras(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			for (ChatLine l : children()) {
				if (l.getOrigin().equals(origin)) {
					if (Objects.equals(getHovered(), l)) {
						hovered = true;
						break;
					}
				}
			}
			if (hovered && !screen.hasContextMenu()) {
				graphics.fill(x - 2 - 22, y - 2, x + entryWidth + 20, y + entryHeight - 1, 0x33FFFFFF);
				if (index < children().size() - 1 && children().get(index + 1).getOrigin().equals(origin)) {
					graphics.fill(x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight + 2,
								  0x33FFFFFF
								 );
				}
				if ((index < children().size() - 1 && !children().get(index + 1).getOrigin().equals(origin)) ||
					index == children().size() - 1) {
					graphics.fill(x - 2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight, 0x33FFFFFF);
				}
			}
			renderExtras(graphics, x, y, mouseX, mouseY);
			graphics.drawString(client.font, content, x, y, -1, false);
		}

		@Override
		public Component getNarration() {
			return Component.literal(origin.content());
		}
	}

	public class NameChatLine extends ChatLine {

		private final String formattedTime;

		public NameChatLine(ChatMessage message) {
			super(Component.literal(message.senderDisplayName()).setStyle(Style.EMPTY.withBold(true))
					  .getVisualOrderText(), message);

			DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy H:mm");
			formattedTime = DATE_FORMAT.format(message.timestamp().atZone(ZoneId.systemDefault()));
		}

		@Override
		protected void renderExtras(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
			RenderSystem.disableBlend();
			ResourceLocation texture =
				Auth.getInstance().getSkinTexture(getOrigin().sender().getUuid(), getOrigin().sender().getName());
			PlayerFaceRenderer.draw(graphics, texture, x-22, y, 18, true, false, -1);
			RenderSystem.enableBlend();
			graphics.drawString(client.font, formattedTime, client.font.width(getContent()) + x + 5, y,
								ClientColors.GRAY.toInt(), false
							   );
		}
	}
}
