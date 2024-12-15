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

package io.github.axolotlclient.api;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.api.chat.ChatWidget;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class ChatsSidebar extends Screen implements ContextMenuScreen {

	private static final int ANIM_STEP = 8;
	private final Screen parent;
	private int sidebarAnimX;
	private int sidebarWidth;
	private boolean remove;
	private boolean hasChat;
	private ListWidget list;
	private TextFieldWidget input;
	private Channel channel;

	private ChatWidget chatWidget;

	private ContextMenuContainer contextMenu;

	public ChatsSidebar(Screen parent) {
		super(Text.translatable("api.chats.sidebar"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (parent != null) {
			parent.render(graphics, mouseX, mouseY, delta);
		}
		graphics.getMatrices().push();
		graphics.getMatrices().translate(0, 0, 1000);
		graphics.fill(sidebarAnimX, 0, sidebarWidth + sidebarAnimX, height, 0x99000000);

		graphics.drawShadowedText(client.textRenderer, Text.translatable("api.chats"), 10 + sidebarAnimX, 10, -1);

		if (hasChat) {
			graphics.fill(70 + sidebarAnimX, 0, 70 + sidebarAnimX + 1, height, 0xFF000000);
			graphics.drawShadowedText(client.textRenderer, channel.getName(), sidebarAnimX + 75, 20, -1);
			if (channel.isDM() && ((Channel.DM) channel).getReceiver().getStatus().isOnline()) {
				graphics.drawShadowedText(client.textRenderer, Formatting.ITALIC + ((Channel.DM) channel).getReceiver().getStatus().getTitle() + ": " + ((Channel.DM) channel).getReceiver().getStatus().getDescription(),
					sidebarAnimX + 80, 30, 8421504);
			}
		}

		super.render(graphics, mouseX, mouseY, delta);

		animate();
		graphics.getMatrices().pop();
	}

	@Override
	protected void init() {
		removeChat();
		sidebarWidth = 70;
		sidebarAnimX = -sidebarWidth;

		if (parent != null) {
			parent.children().stream().filter(element -> element instanceof ClickableWidget)
				.map(e -> (ClickableWidget) e).filter(e -> e.getMessage().equals(Text.translatable("api.chats"))).forEach(e -> e.visible = false);
		}

		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			addDrawableChild(this.list = new ListWidget(list, 10, 30, 50, height - 70))
		);

		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, buttonWidget -> remove()).positionAndSize(10 - sidebarWidth, height - 30, 50, 20).build());
		addDrawableChild(contextMenu = new ContextMenuContainer());
	}

	public void remove() {
		remove = true;

	}

	@Override
	public void removed() {
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return parent != null && parent.isPauseScreen();
	}

	private void animate() {
		if (sidebarAnimX < 0 && !remove) {
			if (sidebarAnimX > -ANIM_STEP) {
				sidebarAnimX = -ANIM_STEP;
			}
			sidebarAnimX += ANIM_STEP;
			if (list != null) {
				list.visible = false;
			}
			getButtons().forEach(button -> button.setX(button.getX() + ANIM_STEP));
		} else if (remove) {
			if (sidebarAnimX < -sidebarWidth) {
				close();
			}
			sidebarAnimX -= ANIM_STEP;
			if (list != null) {
				list.setX(list.getX() - ANIM_STEP);
			}
			getButtons().forEach(button -> button.setX(button.getX() - ANIM_STEP));
			if (chatWidget != null) {
				chatWidget.setLeftPos(chatWidget.getRowLeft() - ANIM_STEP);
			}
		} else {
			if (list != null && !list.visible) {
				list.visible = true;
			}
		}
	}

	public List<ClickableWidget> getButtons() {
		return children().stream().filter(element -> element instanceof ClickableWidget).map(element -> (ClickableWidget) element).collect(Collectors.toList());
	}

	private void close() {
		client.setScreen(parent);
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (contextMenu.getMenu() != null) {
			if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			contextMenu.removeMenu();
		}
		if (mouseX > sidebarWidth) {
			remove();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void removeChat() {
		hasChat = false;
		remove(chatWidget);
		remove(input);
	}

	private void addChat(Channel channel) {
		if (hasChat) {
			removeChat();
		}
		hasChat = true;
		list.elements.forEach(b -> b.active = true);
		this.channel = channel;
		int w;
		if (channel.isDM()) {
			User chatUser = ((Channel.DM) channel).getReceiver();
			w = Math.max(client.textRenderer.getWidth(chatUser.getStatus().getTitle() + ": " + chatUser.getStatus().getDescription()) + 5,
				client.textRenderer.getWidth(channel.getName()));
		} else {
			w = client.textRenderer.getWidth(channel.getName());
		}
		sidebarWidth = Math.min(Math.max(width * 5 / 12, w + 5), width/2);
		chatWidget = new ChatWidget(channel, 75, 50, sidebarWidth - 80, height - 100, this);
		addDrawableChild(chatWidget);
		addDrawableChild(input = new TextFieldWidget(textRenderer, 75, height - 30, sidebarWidth - 80, 20, Text.translatable("api.friends.chat.input")) {
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == InputUtil.KEY_ENTER_CODE) {
					ChatHandler.getInstance().sendMessage(channel, input.getText());
					input.setText("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});
		input.setSuggestion(input.getMessage().getString());
		input.setChangedListener(s -> {
			if (s.isEmpty()) {
				input.setSuggestion(input.getMessage().getString());
			} else {
				input.setSuggestion("");
			}
		});
		input.setMaxLength(1024);
	}

	@Override
	public ContextMenuContainer getMenuContainer() {
		return contextMenu;
	}

	@Override
	public Screen getSelf() {
		return this;
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	private class ListWidget extends AbstractParentElement implements Drawable, Element, Selectable {
		private final List<ClickableWidget> elements;
		private final int y;
		private final int width;
		private final int height;
		private final int entryHeight = 25;
		protected boolean hovered;
		private int x;
		private int scrollAmount;
		private boolean visible;

		public ListWidget(List<Channel> list, int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			AtomicInteger buttonY = new AtomicInteger(y);
			elements = list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
				.map(channel -> ButtonWidget.builder(Text.of(channel.getName()), buttonWidget -> {
						addChat(channel);
						buttonWidget.active = false;
					})
					.positionAndSize(x, buttonY.getAndAdd(entryHeight), width, entryHeight - 5).build()).collect(Collectors.toList());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			if (visible) {
				graphics.getMatrices().push();
				graphics.enableScissor(x, y, x + width, y + height);

				AtomicInteger buttonY = new AtomicInteger(y);
				elements.forEach(e -> {
					e.setY(buttonY.get() - scrollAmount);
					e.render(graphics, mouseX, mouseY, delta);
					buttonY.getAndAdd(entryHeight);
				});

				graphics.disableScissor();
				graphics.getMatrices().pop();
			}
		}

		@Override
		public List<? extends Element> children() {
			return elements;
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amountY) {
			if (this.isMouseOver(mouseX, mouseY)) {
				if (elements.size() * entryHeight > height) {
					int a = scrollAmount;
					a -= (int) (amountY * (entryHeight / 2f));
					scrollAmount = MathHelper.clamp(a, 0, elements.size() * entryHeight - height);
					return true;
				}
			}
			return super.mouseScrolled(mouseX, mouseY, amountY);
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return hovered = visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
			elements.forEach(e -> e.setX(x));
		}

		@Override
		public void appendNarrations(NarrationMessageBuilder builder) {

		}

		@Override
		public SelectionType getType() {
			return this.hovered ? SelectionType.HOVERED : SelectionType.NONE;
		}
	}
}
