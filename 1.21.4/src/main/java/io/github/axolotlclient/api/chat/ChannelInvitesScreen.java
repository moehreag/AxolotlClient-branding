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

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.ChannelInvite;
import io.github.axolotlclient.api.util.UUIDHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public class ChannelInvitesScreen extends Screen {
	private final Screen parent;
	private Button acceptButton;
	private Button denyButton;
	private InvitesListWidget invites;

	public ChannelInvitesScreen(Screen parent) {
		super(Component.translatable("api.channels.invites"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		HeaderAndFooterLayout hFL = new HeaderAndFooterLayout(this, 33, 55);

		hFL.addTitleHeader(title, font);

		invites = hFL.addToContents(new InvitesListWidget(minecraft, hFL.getHeaderHeight(), width, hFL.getContentHeight(), 25));


		var footer = hFL.addToFooter(LinearLayout.vertical().spacing(4));
		var footerTop = footer.addChild(LinearLayout.horizontal().spacing(4));
		acceptButton = footerTop.addChild(Button.builder(Component.translatable("api.channels.invite.accept"), w -> {
			if (invites.getSelected() != null) {
				ChannelRequest.acceptChannelInvite(invites.getSelected().invite);
				rebuildWidgets();
			}
		}).width(73).build());
		denyButton = footerTop.addChild(Button.builder(Component.translatable("api.channels.invite.ignore"), w -> {
			if (invites.getSelected() != null) {
				ChannelRequest.ignoreChannelInvite(invites.getSelected().invite);
				rebuildWidgets();
			}
		}).width(73).build());
		footer.addChild(Button.builder(CommonComponents.GUI_BACK, w -> onClose()).build());

		hFL.arrangeElements();

		hFL.visitWidgets(this::addRenderableWidget);
		updateButtons();
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	private void updateButtons() {
		denyButton.active = acceptButton.active = invites.getSelected() != null;
	}

	private class InvitesListWidget extends ObjectSelectionList<InvitesListWidget.InvitesListEntry> {

		public InvitesListWidget(Minecraft client, int y, int width, int height, int entryHeight) {
			super(client, width, height, y, entryHeight);
			ChannelRequest.getChannelInvites().thenAccept(list ->
				list.stream().map(InvitesListEntry::new).forEach(this::addEntry));
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean bl = super.mouseClicked(mouseX, mouseY, button);
			updateButtons();
			return bl;
		}

		private class InvitesListEntry extends Entry<InvitesListEntry> {

			private final ChannelInvite invite;

			public InvitesListEntry(ChannelInvite invite) {
				this.invite = invite;
			}

			@Override
			public @NotNull Component getNarration() {
				return Component.translatable("api.channels.invite.desc", invite.fromUuid(), invite.channelName());
			}

			@Override
			public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				graphics.drawString(font, Component.translatable("api.channels.invite.name", invite.channelName()), left + 2, top + 2, -1);
				graphics.drawString(font, Component.translatable("api.channels.invite.from", UUIDHelper.getUsername(invite.fromUuid())).withStyle(Style.EMPTY.withItalic(true)), left + 15, top + height - font.lineHeight - 1, 0x808080);

			}
		}
	}
}
