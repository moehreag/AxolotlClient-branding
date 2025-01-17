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

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.chat.ChatWidget;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import io.github.axolotlclient.mixin.ScreenAccessor;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import org.lwjgl.input.Keyboard;

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
	private final String title;

	public ChatsSidebar(Screen parent) {
		super();
		this.title = I18n.translate("api.chats.sidebar");
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (parent != null) {
			parent.render(mouseX, mouseY, delta);
		}
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, 0, 1000);
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.pushScissor(0, 0, Math.max(0, sidebarWidth + sidebarAnimX), height);
		fill(sidebarAnimX, 0, sidebarWidth + sidebarAnimX, height, 0x99000000);

		textRenderer.drawWithShadow(I18n.translate("api.chats"), 10 + sidebarAnimX, 10, -1);

		if (list != null) {
			list.render(mouseX, mouseY, delta);
		}

		super.render(mouseX, mouseY, delta);

		if (input != null) {
			input.render();
		}

		if (hasChat) {
			fill(70 + sidebarAnimX, 0, 70 + sidebarAnimX + 1, height, 0xFF000000);
			textRenderer.drawWithShadow(channel.getName(), sidebarAnimX + 75, 20, -1);
			if (channel.isDM() && ((Channel.DM) channel).getReceiver().getStatus().isOnline()) {
				textRenderer.drawWithShadow(Formatting.ITALIC + ((Channel.DM) channel).getReceiver().getStatus().getTitle() + ": " + ((Channel.DM) channel).getReceiver().getStatus().getDescription(),
					sidebarAnimX + 80, 30, 8421504);
			}
			chatWidget.render(mouseX, mouseY, delta);
		}

		contextMenu.render(minecraft, mouseX, mouseY);
		animate();
		io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil.popScissor();
		GlStateManager.popMatrix();
	}

	@Override
	public void init() {
		removeChat();
		sidebarWidth = 70;
		sidebarAnimX = -sidebarWidth;

		if (parent != null) {
			((ScreenAccessor) parent).getButtons().stream()
				.filter(e -> e.message.equals(I18n.translate("api.chats"))).forEach(e -> e.visible = false);
		}

		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			this.list = new ListWidget(list, 10, 30, 50, height - 70)
		);

		buttons.add(new ButtonWidget(0, 10 - sidebarWidth, height - 30, 50, 20, I18n.translate("gui.back")));
		contextMenu = new ContextMenuContainer();
	}

	public void remove() {
		remove = true;
	}

	@Override
	public void tick() {
		if (input != null) {
			input.tick();
		}
	}

	@Override
	public void removed() {
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public boolean shouldPauseGame() {
		return parent != null && parent.shouldPauseGame();
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
			buttons.forEach(button -> button.x = (button.x + ANIM_STEP));
		} else if (remove) {
			if (sidebarAnimX <= -sidebarWidth) {
				close();
			}
			sidebarAnimX -= ANIM_STEP;
			if (list != null) {
				list.setX(list.getX() - ANIM_STEP);
			}
			buttons.forEach(button -> button.x = (button.x - ANIM_STEP));
			if (chatWidget != null) {
				chatWidget.setX(chatWidget.getX() - ANIM_STEP);
			}
		} else {
			if (list != null && !list.visible) {
				list.visible = true;
			}
		}
	}

	private void close() {
		minecraft.openScreen(parent);
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (contextMenu.getMenu() != null) {
			if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
				return;
			}
			contextMenu.removeMenu();
		}
		if (mouseX > sidebarWidth) {
			remove();
			return;
		}
		if (list != null) {
			list.mouseClicked(mouseX, mouseY, button);
		}

		if (input != null) {
			input.mouseClicked(mouseX, mouseY, button);
		}
		if (chatWidget != null) {
			chatWidget.mouseClicked(mouseX, mouseY, button);
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	private void removeChat() {
		hasChat = false;
		chatWidget = null;
		input = null;
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			remove();
		}
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
			w = Math.max(minecraft.textRenderer.getWidth(chatUser.getStatus().getTitle() + ": " + chatUser.getStatus().getDescription()) + 5,
				minecraft.textRenderer.getWidth(channel.getName()));
		} else {
			w = minecraft.textRenderer.getWidth(channel.getName());
		}
		sidebarWidth = Math.min(Math.max(width * 5 / 12, w + 5), width / 2);
		chatWidget = new ChatWidget(channel, 75, 50, sidebarWidth - 80, height - 100, this);
		input = new TextFieldWidget(2, textRenderer, 75, height - 30, sidebarWidth - 80, 20) {
			@Override
			public boolean keyPressed(char c, int i) {
				if (i == Keyboard.KEY_RETURN) {
					ChatHandler.getInstance().sendMessage(ChatsSidebar.this.channel, input.getText());
					input.setText("");
					return true;
				}
				return super.keyPressed(c, i);
			}

			@Override
			public void render() {
				super.render();
				if (getText().isEmpty()) {
					drawString(textRenderer, I18n.translate("api.friends.chat.input"),
						x + 2, y + 6, -8355712);
				}
			}
		};
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

	@Override
	public void handleMouse() {
		super.handleMouse();
		if (list != null) {
			list.handleMouse();
		}
		if (chatWidget != null) {
			chatWidget.handleMouse();
		}
	}

	@Override
	protected void keyPressed(char c, int i) {
		if (input != null) {
			input.keyPressed(c, i);
		}
		super.keyPressed(c, i);
	}

	public interface Action {
		void onPress(ListWidget.UserButton button);
	}

	private class ListWidget extends EntryListWidget {
		private final List<UserButton> elements;
		private final int entryHeight = 25;
		private boolean visible = true;

		public ListWidget(List<Channel> list, int x, int y, int width, int height) {
			super(Minecraft.getInstance(), width, height, y, ChatsSidebar.this.height - y, 25);
			minX = x;
			maxX = x + width;
			minY = y;
			maxY = y + height;
			AtomicInteger buttonY = new AtomicInteger(y);
			elements = list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
				.map(channel -> new UserButton(x, buttonY.getAndAdd(entryHeight), width, entryHeight - 5,
					channel.getName(), buttonWidget -> {
					addChat(channel);
					buttonWidget.active = false;
				})).collect(Collectors.toList());
		}

		public int getX() {
			return minX;
		}

		public void setX(int x) {
			minX = x;
			maxX = x + width;
			elements.forEach(e -> e.x = x);
		}

		@Override
		protected int size() {
			return elements.size();
		}

		@Override
		public void render(int mouseX, int mouseY, float delta) {
			if (this.visible) {
				this.mouseX = mouseX;
				this.mouseY = mouseY;
				this.capScrolling();
				int m = this.minX + this.width / 2 - this.getRowWidth() / 2 + 2;
				int n = this.minY + 4 - (int) this.scrollAmount;

				this.renderList(m, n, mouseX, mouseY);
			}
		}

		@Override
		protected void renderList(int x, int y, int mouseX, int mouseY) {
			DrawUtil.enableScissor(minX, minY, minX + this.width, maxY);
			super.renderList(x, y, mouseX, mouseY);
			DrawUtil.disableScissor();
		}

		@Override
		public Entry getEntry(int i) {
			return elements.get(i);
		}

		public class UserButton extends ButtonWidget implements Entry {
			private final Action action;

			public UserButton(int x, int y, int width, int height, String string, Action action) {
				super(0, x, y, width, height, string);
				this.action = action;
				visible = true;
			}

			public void mouseClicked(int mouseX, int mouseY) {
				if (isMouseOver(minecraft, mouseX, mouseY)) {
					playClickSound(minecraft.getSoundManager());
					action.onPress(this);
				}
			}

			@Override
			public void renderOutOfBounds(int i, int j, int k) {

			}

			@Override
			public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY,
							   boolean hovered) {
				this.y = y;
				render(minecraft, mouseX, mouseY);
			}

			@Override
			public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
				if (isMouseOver(minecraft, mouseX, mouseY)) {
					playClickSound(minecraft.getSoundManager());
					action.onPress(this);
					return true;
				}
				return false;
			}

			@Override
			public void mouseReleased(int i, int j, int k, int l, int m, int n) {

			}
		}
	}
}
