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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.API;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ChatWidget extends AlwaysSelectedEntryListWidget<ChatWidget.ChatLine> {

	private final List<ChatMessage> messages = new ArrayList<>();
	private final Channel channel;
	private final MinecraftClient client;
	private final ContextMenuScreen screen;

	public ChatWidget(Channel channel, int x, int y, int width, int height, ContextMenuScreen screen) {
		super(MinecraftClient.getInstance(), width, height, y, y + height, 13);
		this.channel = channel;
		this.client = MinecraftClient.getInstance();
		setLeftPos(x + 5);

		setRenderHeader(false, 0);
		this.screen = screen;
		this.width = width;
		this.height = height;
		channel.getMessages().forEach(this::addMessage);

		ChatHandler.getInstance().setMessagesConsumer(chatMessages -> chatMessages.forEach(this::addMessage));
		ChatHandler.getInstance().setMessageConsumer(this::addMessage);
		ChatHandler.getInstance().setEnableNotifications(message -> !message.channelId().equals(channel.getId()));

		setScrollAmount(getMaxScroll());
		setRenderSelection(false);
	}

	public int getX() {
		return left;
	}

	protected int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
	}

	@Override
	protected int getScrollbarPositionX() {
		return left + width - 6;
	}

	@Override
	public int getRowWidth() {
		return width - 60;
	}

	private void addMessage(ChatMessage message) {
		List<OrderedText> list = client.textRenderer.wrapLines(Text.of(message.content()), getRowWidth());

		boolean scrollToBottom = getScrollAmount() == getMaxScroll();

		if (!messages.isEmpty()) {
			ChatMessage prev = messages.get(messages.size()-1);
			if (!(prev.sender().equals(message.sender()) && prev.senderDisplayName().equals(message.senderDisplayName()))) {
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
			before = messages.get(0).timestamp().getEpochSecond();
		} else {
			before = Instant.now().getEpochSecond();
		}
		ChatHandler.getInstance().getMessagesBefore(channel, before);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountY) {
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
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		DrawUtil.enableScissor(this.left, this.top, this.left + width, this.top + height);
		super.renderList(matrices, x, y, mouseX, mouseY, delta);
		DrawUtil.disableScissor();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int i = this.getScrollbarPositionX();
		int j = i + 6;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (client.world == null) {
			bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(this.left, this.bottom, 0.0)
				.texture((float) this.left / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.next();
			bufferBuilder.vertex(this.right, this.bottom, 0.0)
				.texture((float) this.right / 32.0F, (float) (this.bottom + (int) this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.next();
			bufferBuilder.vertex(this.right, this.top, 0.0)
				.texture((float) this.right / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.next();
			bufferBuilder.vertex(this.left, this.top, 0.0)
				.texture((float) this.left / 32.0F, (float) (this.top + (int) this.getScrollAmount()) / 32.0F)
				.color(32, 32, 32, 255)
				.next();
			tessellator.draw();
		}
		int k = this.getRowLeft();
		int l = this.top + 4 - (int)this.getScrollAmount();

		this.renderList(matrices, k, l, mouseX, mouseY, delta);
		this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(this.left, this.top, -100.0).texture(0.0F, (float)this.top / 32.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left + this.width, this.top, -100.0)
			.texture((float)this.width / 32.0F, (float)this.top / 32.0F)
			.color(64, 64, 64, 255)
			.next();
		bufferBuilder.vertex(this.left + this.width, 0.0, -100.0).texture((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left, 0.0, -100.0).texture(0.0F, 0.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left, this.height, -100.0).texture(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.left + this.width, this.height, -100.0)
			.texture((float)this.width / 32.0F, (float)this.height / 32.0F)
			.color(64, 64, 64, 255)
			.next();
		bufferBuilder.vertex(this.left + this.width, this.bottom, -100.0)
			.texture((float)this.width / 32.0F, (float)this.bottom / 32.0F)
			.color(64, 64, 64, 255)
			.next();
		bufferBuilder.vertex(this.left, this.bottom, -100.0).texture(0.0F, (float)this.bottom / 32.0F).color(64, 64, 64, 255).next();
		tessellator.draw();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE
		);
		RenderSystem.disableAlphaTest();
		RenderSystem.shadeModel(7425);
		RenderSystem.disableTexture();
		int n = 4;
		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(this.left, this.top + 4, 0.0).texture(0.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, this.top + 4, 0.0).texture(1.0F, 1.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.right, this.top, 0.0).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.top, 0.0).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.left, this.bottom, 0.0).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, this.bottom, 0.0).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
		bufferBuilder.vertex(this.right, this.bottom - 4, 0.0).texture(1.0F, 0.0F).color(0, 0, 0, 0).next();
		bufferBuilder.vertex(this.left, this.bottom - 4, 0.0).texture(0.0F, 0.0F).color(0, 0, 0, 0).next();
		tessellator.draw();
		int o = this.getMaxScroll();
		if (o > 0) {
			int p = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
			p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
			int q = (int)this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
			if (q < this.top) {
				q = this.top;
			}

			bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
			bufferBuilder.vertex(i, this.bottom, 0.0).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(j, this.bottom, 0.0).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(j, this.top, 0.0).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(i, this.top, 0.0).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
			bufferBuilder.vertex(i, q + p, 0.0).texture(0.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(j, q + p, 0.0).texture(1.0F, 1.0F).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(j, q, 0.0).texture(1.0F, 0.0F).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(i, q, 0.0).texture(0.0F, 0.0F).color(128, 128, 128, 255).next();
			bufferBuilder.vertex(i, q + p - 1, 0.0).texture(0.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(j - 1, q + p - 1, 0.0).texture(1.0F, 1.0F).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(j - 1, q, 0.0).texture(1.0F, 0.0F).color(192, 192, 192, 255).next();
			bufferBuilder.vertex(i, q, 0.0).texture(0.0F, 0.0F).color(192, 192, 192, 255).next();
			tessellator.draw();
		}

		RenderSystem.enableTexture();
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
	}

	public class ChatLine extends Entry<ChatLine> {
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
				return true;
			}
			if (button == 1) {
				ContextMenu.Builder builder = ContextMenu.builder()
					.title(Text.of(origin.sender().getName()))
					.spacer();
				if (!origin.sender().equals(API.getInstance().getSelf())) {
					builder.entry(new TranslatableText("api.friends.chat"), buttonWidget -> {
							ChannelRequest.getOrCreateDM(origin.sender())
								.whenCompleteAsync((channel, throwable) -> client.execute(() -> client.openScreen(new ChatScreen(screen.getParent(), channel))));
						})
						.spacer();
				}
				builder.entry(new TranslatableText("api.chat.report.message"), buttonWidget -> {
						Screen previous = client.currentScreen;
						client.openScreen(new ConfirmScreen(b -> {
							if (b) {
								ChatHandler.getInstance().reportMessage(origin);
							}
							client.openScreen(previous);
						}, new TranslatableText("api.channels.confirm_report"), new TranslatableText("api.channels.confirm_report.desc", origin.content())));
					})
					.spacer()
					.entry(new TranslatableText("action.copy"), buttonWidget -> {
						client.keyboard.setClipboard(origin.content());
					});
				screen.setContextMenu(builder.build());
				return true;
			}
			return false;
		}

		protected void renderExtras(MatrixStack graphics, int x, int y, int mouseX, int mouseY) {
		}

		@Override
		public void render(MatrixStack graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			for (ChatLine l : children()) {
				if (l.getOrigin().equals(origin)) {
					if (l.isMouseOver(mouseX, mouseY) && Objects.equals(getEntryAtPosition(mouseX, mouseY), l)) {
						hovered = true;
						break;
					}
				}
			}
			if (hovered && !screen.hasContextMenu()) {
				fill(graphics, x - 2 - 22, y - 2, x + entryWidth + 20, y + entryHeight - 1, 0x33FFFFFF);
				if (index < children().size() - 1 && children().get(index + 1).getOrigin().equals(origin)) {
					fill(graphics, -2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight + 2, 0x33FFFFFF);
				}
				if ((index < children().size() - 1 && !children().get(index + 1).getOrigin().equals(origin)) || index == children().size() - 1) {
					fill(graphics, -2 - 22, y + entryHeight - 1, x + entryWidth + 20, y + entryHeight, 0x33FFFFFF);
				}
			}
			renderExtras(graphics, x, y, mouseX, mouseY);
			client.textRenderer.draw(graphics, content, x, y, -1);
		}
	}

	public class NameChatLine extends ChatLine {

		private final String formattedTime;

		public NameChatLine(ChatMessage message) {
			super(new LiteralText(message.senderDisplayName())
				.setStyle(Style.EMPTY.withBold(true)).asOrderedText(), message);

			DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy H:mm");
			formattedTime = DATE_FORMAT.format(message.timestamp().atZone(ZoneId.systemDefault()));
		}

		@Override
		protected void renderExtras(MatrixStack graphics, int x, int y, int mouseX, int mouseY) {
			RenderSystem.disableBlend();
			Identifier texture = Auth.getInstance().getSkinTexture(getOrigin().sender().getUuid(),
				getOrigin().sender().getName());
			client.getTextureManager().bindTexture(texture);
			drawTexture(graphics, x - 22, y, 18, 18, 8, 8, 8, 8, 64, 64);
			drawTexture(graphics, x - 22, y, 18, 18, 40, 8, 8, 8, 64, 64);
			RenderSystem.enableBlend();
			client.textRenderer.draw(graphics, formattedTime, client.textRenderer.getWidth(getContent()) + x + 5, y, ClientColors.GRAY.toInt());
		}
	}
}
