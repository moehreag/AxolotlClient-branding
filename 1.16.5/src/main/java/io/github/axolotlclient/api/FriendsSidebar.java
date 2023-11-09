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

package io.github.axolotlclient.api;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.github.axolotlclient.api.chat.ChatWidget;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class FriendsSidebar extends Screen implements ContextMenuScreen {

	private static final int ANIM_STEP = 5;
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

	public FriendsSidebar(Screen parent) {
		super(new TranslatableText("api.friends.sidebar"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (parent != null) {
			parent.render(matrices, mouseX, mouseY, delta);
		}
		fill(matrices, sidebarAnimX, 0, sidebarWidth + sidebarAnimX, height, 0x99000000);

		if (list != null) {
			list.render(matrices, mouseX, mouseY, delta);
		}

		client.textRenderer.drawWithShadow(matrices, new TranslatableText("api.friends"), 10 + sidebarAnimX, 10, -1);

		super.render(matrices, mouseX, mouseY, delta);

		if (hasChat) {
			drawVerticalLine(matrices, 70 + sidebarAnimX, 0, height, 0xFF000000);
			client.textRenderer.drawWithShadow(matrices, channel.getName(), sidebarAnimX + 75, 20, -1);
			if (channel.isDM()) {
				client.textRenderer.drawWithShadow(matrices, Formatting.ITALIC + ((Channel.DM) channel).getReceiver().getStatus().getTitle() + ":" + ((Channel.DM) channel).getReceiver().getStatus().getDescription(),
					sidebarAnimX + 80, 30, 8421504);
			}

			chatWidget.render(matrices, mouseX, mouseY, delta);
		}

		contextMenu.render(matrices, mouseX, mouseY, delta);

		animate();
	}

	@Override
	protected void init() {
		sidebarWidth = 70;
		sidebarAnimX = -sidebarWidth;

		if (parent != null) {
			parent.children().stream().filter(element -> element instanceof ClickableWidget)
				.map(e -> (ClickableWidget) e).filter(e -> e.getMessage().equals(new TranslatableText("api.friends"))).forEach(e -> e.visible = false);
		}

		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			addChild(this.list = new ListWidget(list, 10, 30, 50, height - 60))
		);

		addButton(new ButtonWidget(10 - sidebarWidth, height - 30, 50, 20, ScreenTexts.BACK, buttonWidget -> remove()));
		addChild(contextMenu = new ContextMenuContainer());
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
			getButtons().forEach(button -> button.x = (button.x + ANIM_STEP));
		} else if (remove) {
			if (sidebarAnimX < -sidebarWidth) {
				close();
			}
			sidebarAnimX -= ANIM_STEP;
			if (list != null) {
				list.setX(list.getX() - ANIM_STEP);
			}
			getButtons().forEach(button -> button.x = (button.x - ANIM_STEP));
			if (chatWidget != null) {
				chatWidget.setX(chatWidget.getX() - ANIM_STEP);
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
		client.openScreen(parent);
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

	private void addChat(Channel channel) {
		hasChat = true;
		this.channel = channel;
		int w;
		if (channel.isDM()) {
			User chatUser = ((Channel.DM) channel).getReceiver();
			w = Math.max(client.textRenderer.getWidth(chatUser.getStatus().getTitle() + ":" + chatUser.getStatus().getDescription()),
				client.textRenderer.getWidth(channel.getName()));
		} else {
			w = client.textRenderer.getWidth(channel.getName());
		}
		sidebarWidth = Math.max(width * 5 / 12, w + 5);
		chatWidget = new ChatWidget(channel, 75, 50, sidebarWidth - 80, height - 60, this);
		addButton(input = new TextFieldWidget(textRenderer, 75, height - 30, sidebarWidth - 80, 20, new TranslatableText("api.friends.chat.input")) {
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == GLFW.GLFW_KEY_ENTER) {
					ChatHandler.getInstance().sendMessage(channel, input.getText());
					input.setText("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});
	}

	@Override
	public void setContextMenu(ContextMenu menu) {
		contextMenu.setMenu(menu);
	}

	@Override
	public boolean hasContextMenu() {
		return contextMenu.hasMenu();
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	public static class ButtonEntry extends EntryListWidget.Entry<ButtonEntry> {

		private final ButtonWidget widget;

		public ButtonEntry(ButtonWidget widget) {
			this.widget = widget;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			widget.y = y;
			widget.x = x;
			System.out.println(x);
			//MinecraftClient.getInstance().textRenderer.draw(matrices, widget.getMessage(), x, y, -1);
			widget.render(matrices, mouseX, mouseY, tickDelta);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return widget.mouseClicked(mouseX, mouseY, button);
		}
	}

	public class ListWidget extends EntryListWidget<ButtonEntry> {
		private final int entryHeight = 25;
		private boolean visible;

		public ListWidget(List<Channel> list, int x, int y, int width, int height) {
			super(FriendsSidebar.this.client, width, height, y, y + height, 25);
			left = x;
			right = x + width;
			top = y;
			bottom = y + height;
			this.setRenderHeader(false, 0);

			setRenderSelection(false);
			AtomicInteger buttonY = new AtomicInteger(y);
			list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
				.map(user -> new ButtonWidget(x, buttonY.getAndAdd(entryHeight), width, entryHeight - 5,
					Text.of(user.getName()), buttonWidget -> addChat(user))).forEach(b -> addEntry(new ButtonEntry(b)));
		}

		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (visible) {
				this.renderBackground(matrices);
				int k = this.getRowLeft();
				int l = this.top + 4 - (int) this.getScrollAmount();
				this.renderList(matrices, k, l, mouseX, mouseY, delta);
			}
		}

		@Override
		protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
			Util.applyScissor(left, top, right, height);
			super.renderList(matrices, x, y, mouseX, mouseY, delta);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		@Override
		public int getRowLeft() {
			return left;
		}

		public int getX() {
			return left;
		}

		public void setX(int x) {
			left = x;
			right = x + width;
		}
	}
}
