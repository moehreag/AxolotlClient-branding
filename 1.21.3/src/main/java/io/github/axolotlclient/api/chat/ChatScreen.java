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

import com.mojang.blaze3d.platform.InputConstants;
import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.ContextMenuContainer;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ChatScreen extends Screen implements ContextMenuScreen {

	private final Channel channel;
	private final Screen parent;
	private final ContextMenuContainer contextMenu = new ContextMenuContainer();
	private ChatWidget widget;
	private ChatUserListWidget users;
	private EditBox input;

	public ChatScreen(Screen parent, Channel channel) {
		super(Component.translatable("api.screen.chat"));
		this.channel = channel;
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredString(this.font, channel.getName(), this.width / 2, 15, 16777215);
	}

	@Override
	protected void init() {

		addRenderableWidget(new ChatListWidget(this, width, height, 0, 30, 55, height - 90));

		addRenderableWidget(widget = new ChatWidget(channel, 65, 30, width - (!channel.isDM() ? 155 : 115), height - 90, this));

		if (!channel.isDM()) {
			users = new ChatUserListWidget(this, minecraft, 80, height - 20, 30, height - 60, 25);
			users.setX(width - 80);
			users.setUsers(channel.getAllUsers());
			addRenderableWidget(users);
		}

		addRenderableWidget(input = new EditBox(font, width / 2 - 150, height - 50,
			300, 20, Component.translatable("api.chat.enterMessage")) {

			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == InputConstants.KEY_RETURN && !getValue().isEmpty()) {
					ChatHandler.getInstance().sendMessage(channel, getValue());
					setValue("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});

		input.setSuggestion(Component.translatable(channel.isDM() ? "api.chat.messageUser" : "api.chat.messageGroup", channel.getName()).getString());
		input.setResponder(s -> {
			if (s.isEmpty()) {
				input.setSuggestion(Component.translatable(channel.isDM() ? "api.chat.messageUser" : "api.chat.messageGroup", channel.getName()).getString());
			} else {
				input.setSuggestion("");
			}
		});
		input.setMaxLength(1024);

		if (channel.getOwner().equals(API.getInstance().getSelf())) {
			addRenderableWidget(Button.builder(Component.translatable("api.channel.configure"), b -> minecraft.setScreen(new ChannelSettingsScreen(this, channel)))
				.bounds(width - 60, 5, 50, 20).build());
		}

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.parent))
			.bounds(this.width / 2 - 75, this.height - 28, 150, 20)
			.build()
		);

		addRenderableOnly(contextMenu);
	}

	@Override
	public void removed() {
		if (widget != null) {
			widget.remove();
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
		return super.mouseClicked(mouseX, mouseY, button);
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
