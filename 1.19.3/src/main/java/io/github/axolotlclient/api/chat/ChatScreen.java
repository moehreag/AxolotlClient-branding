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

import java.util.Arrays;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.api.ContextMenu;
import io.github.axolotlclient.api.ContextMenuContainer;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;

public class ChatScreen extends Screen implements ContextMenuScreen {

	private ContextMenuContainer contextMenu = new ContextMenuContainer();
	private final Channel channel;
	private final Screen parent;

	private ChatWidget widget;
	private ChatUserListWidget users;
	private TextFieldWidget input;

	public ChatScreen(Screen parent, Channel channel) {
		super(Text.translatable("api.screen.chat"));
		this.channel = channel;
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);

		if (users != null) {
			users.render(matrices, mouseX, mouseY, delta);
		}

		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, channel.getName(), this.width / 2, 20, 16777215);
	}

	@Override
	protected void init() {

		addDrawableChild(new ChatListWidget(this, width, height, 0, 30, 50, height - 90));

		addDrawableChild(widget = new ChatWidget(channel, 50, 30, width - (!channel.isDM() ? 140 : 100), height - 90, this));

		if (!channel.isDM()) {
			users = new ChatUserListWidget(this, client, 80, height - 20, 30, height - 60, 25);
			users.setLeftPos(width - 80);
			users.setUsers(Arrays.asList(channel.getUsers()));
			addSelectableChild(users);
		}

		addDrawableChild(input = new TextFieldWidget(client.textRenderer, width / 2 - 150, height - 50,
			300, 20, Text.translatable("api.chat.enterMessage")) {

			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == InputUtil.KEY_ENTER_CODE && !getText().isEmpty()) {
					ChatHandler.getInstance().sendMessage(channel, getText());
					setText("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});

		input.setSuggestion(Text.translatable(channel.isDM() ? "api.chat.messageUser" : "api.chat.messageGroup", channel.getName()).getString());
		input.setChangedListener(s -> {
			if (s.isEmpty()) {
				input.setSuggestion(Text.translatable(channel.isDM() ? "api.chat.messageUser" : "api.chat.messageGroup", channel.getName()).getString());
			} else {
				input.setSuggestion("");
			}
		});
		input.setMaxLength(1024);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.client.setScreen(this.parent))
			.positionAndSize(this.width / 2 - 75, this.height - 28, 150, 20)
			.build()
		);

		addDrawableChild(contextMenu);
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
	public void setContextMenu(ContextMenu menu) {
		this.contextMenu.setMenu(menu);
	}

	@Override
	public boolean hasContextMenu() {
		return contextMenu.hasMenu();
	}

	@Override
	public Screen getParent() {
		return parent;
	}
}
