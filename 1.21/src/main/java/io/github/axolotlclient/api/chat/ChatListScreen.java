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

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class ChatListScreen extends Screen {

	private final Screen parent;

	public ChatListScreen(Screen parent) {
		super(Text.translatable("api.chats"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredShadowedText(client.textRenderer, Text.translatable("api.chats"), width / 2, 20, -1);
		graphics.drawCenteredShadowedText(client.textRenderer, Text.translatable("api.chat.dms"), width / 2 + 80, 40, -1);
		graphics.drawCenteredShadowedText(client.textRenderer, Text.translatable("api.chat.groups"), width / 2 - 80, 40, -1);
	}

	@Override
	protected void init() {
		ChatListWidget groups = addDrawableSelectableElement(new ChatListWidget(this, width, height, width / 2 - 155, 55, 150, height - 105, c -> !c.isDM()));
		ChatListWidget dms = addDrawableSelectableElement(new ChatListWidget(this, width, height, width / 2 + 5, 55, 150, height - 105, Channel::isDM));

		addDrawableSelectableElement(ButtonWidget.builder(CommonTexts.BACK, buttonWidget ->
			client.setScreen(parent)).positionAndSize(this.width / 2 + 5, this.height - 40, 150, 20).build());
		addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("api.chat.groups.create"), buttonWidget ->
				client.setScreen(new CreateChannelScreen(this)))
			.positionAndSize(this.width / 2 - 155, this.height - 40, 150, 20).build());
		ChannelRequest.getChannelList().whenCompleteAsync((list, t) -> {
			groups.addChannels(list);
			dms.addChannels(list);
		});
	}
}
