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

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenuContainer;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.input.Keyboard;

public class ChatScreen extends Screen implements ContextMenuScreen {

	private final Channel channel;
	private final Screen parent;
	private final ContextMenuContainer contextMenu = new ContextMenuContainer();
	private ChatWidget widget;
	private ChatListWidget chatListWidget;
	private ChatUserListWidget users;
	private TextFieldWidget input;
	private final String title = I18n.translate("api.screen.chat");

	public ChatScreen(Screen parent, Channel channel) {
		super();
		this.channel = channel;
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		drawBackgroundTexture(0);

		if (users != null) {
			users.render(mouseX, mouseY, delta);
		}

		chatListWidget.render(mouseX, mouseY, delta);
		widget.render(mouseX, mouseY, delta);
		input.render();

		super.render(mouseX, mouseY, delta);

		drawCenteredString(this.textRenderer, channel.getName(), this.width / 2, 20, 16777215);

		contextMenu.render(minecraft, mouseX, mouseY);
	}

	@Override
	public void init() {
		chatListWidget = new ChatListWidget(this, width, height, 0, 30, 55, height - 90);
		ChannelRequest.getChannelList().thenAccept(chatListWidget::addChannels).thenRun(() -> chatListWidget.setActiveChannel(channel));

		widget = new ChatWidget(channel, 65, 30, width - 155, height - 90, this);

		users = new ChatUserListWidget(this, minecraft, 80, height - 20, 30, height - 60, 25);
		users.setX(width - 80);
		users.setUsers(channel.getAllUsers(), channel);

		input = new TextFieldWidget(5, minecraft.textRenderer, width / 2 - 150, height - 50,
			300, 20) {

			@Override
			public boolean keyPressed(char c, int i) {
				if (i == Keyboard.KEY_RETURN && !getText().isEmpty()) {
					ChatHandler.getInstance().sendMessage(channel, getText());
					setText("");
					return true;
				}
				return super.keyPressed(c, i);
			}

			@Override
			public void render() {
				super.render();
				if (getText().isEmpty()) {
					drawString(textRenderer, I18n.translate(channel.isDM() ? "api.chat.messageUser" : "api.chat.messageGroup", channel.getName()),
						x + 2, y + 6, -8355712);
				}
			}
		};
		input.setMaxLength(1024);

		if (channel.getOwner().equals(API.getInstance().getSelf())) {
			buttons.add(new ButtonWidget(2, width - 60, 5, 50, 20, I18n.translate("api.channel.configure")){
				@Override
				public void drawCenteredString(TextRenderer textRenderer, String string, int i, int j, int k) {
					DrawUtil.drawScrollableText(textRenderer, string, x+2, y, x+width-2, y+height, k);
				}
			});
		}

		this.buttons.add(new ButtonWidget(1, this.width / 2 - 75, this.height - 28, 150, 20,
			I18n.translate("gui.back")));
		Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void tick() {
		input.tick();
	}

	@Override
	public void removed() {
		if (widget != null) {
			widget.remove();
		}
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 1) {
			this.minecraft.openScreen(this.parent);
		} else if (buttonWidget.id == 2) {
			minecraft.openScreen(new ChannelSettingsScreen(this, channel));
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
		super.mouseClicked(mouseX, mouseY, button);
		widget.mouseClicked(mouseX, mouseY, button);
		input.mouseClicked(mouseX, mouseY, button);
		users.mouseClicked(mouseX, mouseY, button);
		chatListWidget.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();

		widget.handleMouse();
		users.handleMouse();
		chatListWidget.handleMouse();
	}

	@Override
	protected void keyPressed(char c, int i) {
		super.keyPressed(c, i);
		input.keyPressed(c, i);
	}

	@Override
	public ContextMenuContainer getMenuContainer() {
		return contextMenu;
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	@Override
	public Screen getSelf() {
		return this;
	}
}
