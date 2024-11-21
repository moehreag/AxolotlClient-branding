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

import com.mojang.blaze3d.platform.InputConstants;
import io.github.axolotlclient.api.chat.ChatWidget;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FriendsSidebar extends Screen implements ContextMenuScreen {

	private static final int ANIM_STEP = 8;
	private final Screen parent;
	private int sidebarAnimX;
	private int sidebarWidth;
	private boolean remove;
	private boolean hasChat;
	private ListWidget list;
	private EditBox input;
	private Channel channel;

	private ChatWidget chatWidget;

	private ContextMenuContainer contextMenu;

	public FriendsSidebar(Screen parent) {
		super(Component.translatable("api.chats.sidebar"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (parent != null) {
			parent.render(graphics, mouseX, mouseY, delta);
		}
		graphics.fill(sidebarAnimX, 0, sidebarWidth + sidebarAnimX, height, 0x99000000);

		graphics.drawString(font, Component.translatable("api.chats"), 10 + sidebarAnimX, 10, -1);

		if (hasChat) {
			graphics.fill(70 + sidebarAnimX, 0, 70 + sidebarAnimX + 1, height, 0xFF000000);
			graphics.drawString(font, channel.getName(), sidebarAnimX + 75, 20, -1);
			if (channel.isDM()) {
				graphics.drawString(font, ChatFormatting.ITALIC + ((Channel.DM) channel).getReceiver().getStatus().getTitle() + ":" + ((Channel.DM) channel).getReceiver().getStatus().getDescription(),
					sidebarAnimX + 80, 30, 0x808080);
			}
		}

		super.render(graphics, mouseX, mouseY, delta);

		animate();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

	}

	@Override
	protected void init() {
		removeChat();
		sidebarWidth = 70;
		sidebarAnimX = -sidebarWidth;

		if (parent != null) {
			parent.children().stream().filter(element -> element instanceof AbstractButton)
				.map(e -> (AbstractButton) e).filter(e -> e.getMessage().equals(Component.translatable("api.chats"))).forEach(e -> e.visible = false);
		}

		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			addRenderableWidget(this.list = new ListWidget(list, 10, 30, 50, height - 60))
		);

		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, buttonWidget -> remove()).bounds(10 - sidebarWidth, height - 30, 50, 20).build());
		addRenderableWidget(contextMenu = new ContextMenuContainer());
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
				chatWidget.setX(chatWidget.getX() - ANIM_STEP);
			}
		} else {
			if (list != null && !list.visible) {
				list.visible = true;
			}
		}
	}

	public List<AbstractButton> getButtons() {
		return children().stream().filter(element -> element instanceof AbstractButton).map(element -> (AbstractButton) element).toList();
	}

	private void close() {
		minecraft.setScreen(parent);
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
		removeWidget(chatWidget);
		removeWidget(input);
	}

	private void addChat(Channel channel) {
		hasChat = true;
		this.channel = channel;
		int w;
		if (channel.isDM()) {
			User chatUser = ((Channel.DM) channel).getReceiver();
			w = Math.max(font.width(chatUser.getStatus().getTitle() + ":" + chatUser.getStatus().getDescription()),
				font.width(channel.getName()));
		} else {
			w = font.width(channel.getName());
		}
		sidebarWidth = Math.max(width * 5 / 12, w + 5);
		chatWidget = new ChatWidget(channel, 75, 50, sidebarWidth - 80, height - 100, this);
		addRenderableWidget(chatWidget);
		addRenderableWidget(input = new EditBox(font, 75, height - 30, sidebarWidth - 80, 20, Component.translatable("api.friends.chat.input")) {
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == InputConstants.KEY_RETURN) {
					ChatHandler.getInstance().sendMessage(channel, input.getValue());
					input.setValue("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});
		input.setSuggestion(input.getMessage().getString());
		input.setResponder(s -> {
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

	private class ListWidget extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {
		private final List<AbstractButton> elements;
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
				.map(channel -> Button.builder(Component.literal(channel.getName()), buttonWidget -> addChat(channel))
					.bounds(x, buttonY.getAndAdd(entryHeight), width, entryHeight - 5).build()).collect(Collectors.toList());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			if (visible) {
				graphics.pose().pushPose();
				graphics.enableScissor(x, y, x + width, y + height);

				graphics.pose().translate(0, -scrollAmount, 0);

				elements.forEach(e -> e.render(graphics, mouseX, mouseY, delta));

				graphics.disableScissor();
				graphics.pose().popPose();
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return elements;
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
			if (this.isMouseOver(mouseX, mouseY)) {
				if (elements.size() * entryHeight > height) {
					int a = scrollAmount;
					a += (int) (amountY * (entryHeight / 2));
					scrollAmount = Mth.clamp(a, 0, -elements.size() * entryHeight);
					return true;
				}
			}
			return super.mouseScrolled(mouseX, mouseY, amountX, amountY);
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
		public NarratableEntry.NarrationPriority narrationPriority() {
			return this.hovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}

		@Override
		public void updateNarration(NarrationElementOutput builder) {

		}
	}
}
