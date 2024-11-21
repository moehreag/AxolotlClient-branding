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

import io.github.axolotlclient.api.chat.ChatScreen;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FriendsScreen extends Screen {

	private final Screen parent;

	private UserListWidget widget;

	private Button chatButton, removeButton, onlineTab, allTab, pendingTab, blockedTab;
	private Button denyButton, acceptButton;

	private Tab current = Tab.ONLINE;

	protected FriendsScreen(Screen parent, Tab tab) {
		this(parent);
		current = tab;
	}

	protected FriendsScreen(Screen parent) {
		super(Component.translatable("api.screen.friends"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 294) {
			this.refresh();
			return true;
		} else if (this.widget.getSelected() != null) {
			if (keyCode != 257 && keyCode != 335) {
				return this.widget.keyPressed(keyCode, scanCode, modifiers);
			} else {
				this.openChat();
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	protected void init() {
		addRenderableWidget(widget = new UserListWidget(this, minecraft, width, height, 32, height - 64, 35));

		widget.children().clear();

		if (current == Tab.ALL || current == Tab.ONLINE) {
			FriendRequest.getInstance().getFriends().whenCompleteAsync((list, t) -> widget.setUsers(
				list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
					.filter(user -> {
						if (current == Tab.ONLINE) {
							return user.getStatus().isOnline();
						}
						return true;
					}).toList()));
		} else if (current == Tab.PENDING) {
			FriendRequest.getInstance().getFriendRequests().whenCompleteAsync((con, th) -> {

				con.getLeft().stream()
					.sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName())).forEach(
						user -> widget.addEntry(new UserListWidget.UserListEntry(user, Component.translatable(
							"api.friends.pending.incoming"))));
				con.getRight().stream()
					.sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName())).forEach(
						user -> widget.addEntry(new UserListWidget.UserListEntry(user, Component.translatable(
							"api.friends.pending.outgoing"))));
			});
		} else if (current == Tab.BLOCKED) {
			FriendRequest.getInstance().getBlocked().whenCompleteAsync((list, th) -> widget.setUsers(
				list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
					.toList()));
		}

		this.addRenderableWidget(blockedTab = Button.builder(Component.translatable("api.friends.tab.blocked"),
															 button -> minecraft.setScreen(
																 new FriendsScreen(parent, Tab.BLOCKED))
															).bounds(this.width / 2 + 24, this.height - 52, 57, 20)
			.build());

		this.addRenderableWidget(pendingTab = Button.builder(Component.translatable("api.friends.tab.pending"),
															 button -> minecraft.setScreen(
																 new FriendsScreen(parent, Tab.PENDING))
															).bounds(this.width / 2 - 34, this.height - 52, 57, 20)
			.build());

		this.addRenderableWidget(allTab = Button.builder(Component.translatable("api.friends.tab.all"),
														 button -> minecraft.setScreen(
															 new FriendsScreen(parent, Tab.ALL))
														).bounds(this.width / 2 - 94, this.height - 52, 57, 20)
			.build());

		this.addRenderableWidget(onlineTab = Button.builder(Component.translatable("api.friends.tab.online"),
															button -> minecraft.setScreen(
																new FriendsScreen(parent, Tab.ONLINE))
														   ).bounds(this.width / 2 - 154, this.height - 52, 57, 20)
			.build());

		this.addRenderableWidget(Button.builder(Component.translatable("api.friends.add"),
												button -> minecraft.setScreen(new AddFriendScreen(this))
											   ).bounds(this.width / 2 + 88, this.height - 52, 66, 20).build());

		this.removeButton =
			this.addRenderableWidget(Button.builder(Component.translatable("api.friends.remove"), button -> {
				UserListWidget.UserListEntry entry = this.widget.getSelected();
				if (entry != null) {
					FriendRequest.getInstance().removeFriend(entry.getUser());
					refresh();
				}
			}).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());

		addRenderableWidget(denyButton = Button.builder(Component.translatable("api.friends.request.deny"),
														button -> denyRequest()
													   ).bounds(this.width / 2 - 50, this.height - 28, 48, 20).build());

		addRenderableWidget(acceptButton = Button.builder(Component.translatable("api.friends.request.accept"),
														  button -> acceptRequest()
														 ).bounds(this.width / 2 + 2, this.height - 28, 48, 20)
			.build());

		this.addRenderableWidget(chatButton =
									 Button.builder(Component.translatable("api.friends.chat"), button -> openChat())
										 .bounds(this.width / 2 - 154, this.height - 28, 100, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.parent))
									 .bounds(this.width / 2 + 4 + 50, this.height - 28, 100, 20).build());
		updateButtonActivationStates();
	}

	private void refresh() {
		minecraft.setScreen(new FriendsScreen(parent));
	}

	private void denyRequest() {
		UserListWidget.UserListEntry entry = widget.getSelected();
		if (entry != null) {
			FriendRequest.getInstance().denyFriendRequest(entry.getUser());
		}
		refresh();
	}

	private void acceptRequest() {
		UserListWidget.UserListEntry entry = widget.getSelected();
		if (entry != null) {
			FriendRequest.getInstance().acceptFriendRequest(entry.getUser());
		}
		refresh();
	}

	private void updateButtonActivationStates() {
		UserListWidget.UserListEntry entry = widget.getSelected();
		if (entry != null) {
			chatButton.active = removeButton.active = true;
		} else {
			chatButton.active = removeButton.active = false;
		}

		removeButton.visible = true;
		denyButton.visible = false;
		acceptButton.visible = false;
		if (current == Tab.ONLINE) {
			onlineTab.active = false;
			allTab.active = pendingTab.active = blockedTab.active = true;
		} else if (current == Tab.ALL) {
			allTab.active = false;
			onlineTab.active = pendingTab.active = blockedTab.active = true;
		} else if (current == Tab.PENDING) {
			pendingTab.active = false;
			onlineTab.active = allTab.active = blockedTab.active = true;
			removeButton.visible = false;
			denyButton.visible = true;
			acceptButton.visible = true;
			denyButton.active = acceptButton.active = entry != null;
		} else if (current == Tab.BLOCKED) {
			blockedTab.active = false;
			onlineTab.active = allTab.active = pendingTab.active = true;
		}
	}

	public void openChat() {
		UserListWidget.UserListEntry entry = widget.getSelected();
		if (entry != null) {
			ChannelRequest.getOrCreateDM(entry.getUser())
				.whenCompleteAsync((c, t) -> minecraft.execute(() -> minecraft.setScreen(new ChatScreen(this, c))));
		}
	}

	public void select(UserListWidget.UserListEntry entry) {
		this.widget.setSelected(entry);
		this.updateButtonActivationStates();
	}

	public enum Tab {
		ONLINE, ALL, PENDING, BLOCKED
	}
}
