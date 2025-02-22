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

import io.github.axolotlclient.api.ContextMenuContainer;
import io.github.axolotlclient.api.ContextMenuScreen;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class ChatListScreen extends Screen implements ContextMenuScreen {

	private final Screen parent;
	private final ContextMenuContainer container;
	private ChatListWidget dms;
	private ChatListWidget groups;

	public ChatListScreen(Screen parent) {
		super();
		this.parent = parent;
		container = new ContextMenuContainer();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		drawBackgroundTexture(0);
		dms.render(mouseX, mouseY, delta);
		groups.render(mouseX, mouseY, delta);
		super.render(mouseX, mouseY, delta);
		container.render(minecraft, mouseX, mouseY);

		drawCenteredString(minecraft.textRenderer, I18n.translate("api.chats"), width / 2, 20, -1);
		drawCenteredString(minecraft.textRenderer, I18n.translate("api.chat.dms"), width / 2 + 80, 40, -1);
		drawCenteredString(minecraft.textRenderer, I18n.translate("api.chat.groups"), width / 2 - 80, 40, -1);
	}

	@Override
	public void init() {
		groups = new ChatListWidget(this, width, height, width / 2 - 155, 55, 150, height - 105, c -> !c.isDM());
		dms = new ChatListWidget(this, width, height, width / 2 + 5, 55, 150, height - 105, Channel::isDM);

		buttons.add(new ButtonWidget(0, this.width / 2 + 5, this.height - 40, 150, 20, I18n.translate("gui.back")));
		buttons.add(new ButtonWidget(1, this.width / 2 - 155, this.height - 40, 150, 20,
			I18n.translate("api.chat.groups.create")));
		ChannelRequest.getChannelList().thenAccept(list -> {
			groups.addChannels(list);
			dms.addChannels(list);
		});
	}

	@Override
	public ContextMenuContainer getMenuContainer() {
		return container;
	}

	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			minecraft.openScreen(parent);
		} else if (buttonWidget.id == 1) {
			minecraft.openScreen(new CreateChannelScreen(this));
		}
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	protected void mouseClicked(int i, int j, int k) {
		if (container.mouseClicked(i, j, k)) {
			return;
		}
		super.mouseClicked(i, j, k);
		dms.mouseClicked(i, j, k);
		groups.mouseClicked(i, j, k);
	}

	@Override
	public Screen getSelf() {
		return this;
	}

	public void handleMouse() {
		super.handleMouse();
		dms.handleMouse();
		groups.handleMouse();
	}
}
