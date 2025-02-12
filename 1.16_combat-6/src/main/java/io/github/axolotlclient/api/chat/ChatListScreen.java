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
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class ChatListScreen extends Screen implements ContextMenuScreen {

	private final Screen parent;
	private final ContextMenuContainer container;
	private ChatListWidget dms;
	private ChatListWidget groups;

	public ChatListScreen(Screen parent) {
		super(new TranslatableText("api.chats"));
		this.parent = parent;
		container = new ContextMenuContainer();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackgroundTexture(0);
		dms.render(matrices, mouseX, mouseY, delta);
		groups.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		container.render(matrices, mouseX, mouseY, delta);

		drawCenteredString(matrices, client.textRenderer, I18n.translate("api.chats"), width / 2, 20, -1);
		drawCenteredString(matrices, client.textRenderer, I18n.translate("api.chat.dms"), width / 2 + 80, 40, -1);
		drawCenteredString(matrices, client.textRenderer, I18n.translate("api.chat.groups"), width / 2 - 80, 40, -1);
	}

	@Override
	protected void init() {
		groups = addChild(new ChatListWidget(this, width, height, width / 2 - 155, 55, 150, height - 105, c -> !c.isDM()));
		dms = addChild(new ChatListWidget(this, width, height, width / 2 + 5, 55, 150, height - 105, Channel::isDM));

		addButton(new ButtonWidget(this.width / 2 + 5, this.height - 40, 150, 20, ScreenTexts.BACK, buttonWidget ->
			client.openScreen(parent)));
		addButton(new ButtonWidget(this.width / 2 - 155, this.height - 40, 150, 20, new TranslatableText("api.chat.groups.create"), buttonWidget ->
			client.openScreen(new CreateChannelScreen(this))));
		ChannelRequest.getChannelList().whenCompleteAsync((list, t) -> {
			groups.addChannels(list);
			dms.addChannels(list);
		});
	}

	@Override
	public ContextMenuContainer getMenuContainer() {
		return container;
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	@Override
	public Screen getSelf() {
		return this;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (container.getMenu() != null) {
			if (container.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			container.removeMenu();
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
