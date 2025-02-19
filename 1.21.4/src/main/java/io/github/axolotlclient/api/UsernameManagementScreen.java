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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.api.requests.AccountUsernameRequest;
import io.github.axolotlclient.api.types.User;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class UsernameManagementScreen extends Screen {

	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private UsernameListWidget widget;

	public UsernameManagementScreen(Screen parent) {
		super(Component.translatable("api.account.usernames"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.setHeaderHeight(45);
		layout.setFooterHeight(55);
		layout.addTitleHeader(getTitle(), this.font);


		layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).build());
		if (API.getInstance().isAuthenticated()) {
			widget = new UsernameListWidget(API.getInstance().getSelf().getPreviousUsernames());
		} else {
			widget = new UsernameListWidget(Collections.emptyList());
		}
		layout.addToContents(widget);
		layout.arrangeElements();

		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
		widget.updateSize(width, layout);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	private class UsernameListWidget extends ContainerObjectSelectionList<UsernameListWidget.UsernameListEntry> {

		public UsernameListWidget(List<User.OldUsername> names) {
			super(UsernameManagementScreen.this.minecraft, UsernameManagementScreen.this.width,
				UsernameManagementScreen.this.layout.getContentHeight(),
				UsernameManagementScreen.this.layout.getHeaderHeight(), 20
			);

			names.forEach(n -> addEntry(new UsernameListEntry(n)));
		}

		@Override
		public int getRowWidth() {
			return 310;
		}

		private class UsernameListEntry extends Entry<UsernameListEntry> {

			private final Button visibility;
			private final Button delete;
			private final String name;

			public UsernameListEntry(User.OldUsername name) {
				visibility = Button.builder(Component.translatable("api.account.usernames.public", name.isPub()), w -> {
					name.setPub(!name.isPub());
					w.setMessage(Component.translatable("api.account.usernames.public", name.isPub()));
					AccountUsernameRequest.post(name.getName(), name.isPub());
				}).width(100).build();
				delete = Button.builder(Component.translatable("api.account.usernames.delete"),
					w -> minecraft.setScreen(new ConfirmScreen(b -> {
						if (b) {
							AccountUsernameRequest.delete(name.getName())
								.thenRun(() -> UsernameListWidget.this.removeEntry(this));
						}
						minecraft.setScreen(UsernameManagementScreen.this);
					}, Component.translatable("api.account.confirm_deletion"),
						Component.translatable(
							"api.account.usernames.delete.desc")
					))
				).width(100).build();
				this.name = name.getName();
			}

			@Override
			public List<? extends GuiEventListener> children() {
				return List.of(visibility, delete);
			}

			@Override
			public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
				int deleteX = UsernameListWidget.this.scrollBarX() - delete.getWidth() - 10;
				delete.setPosition(deleteX, y - 2);
				visibility.setPosition(deleteX - visibility.getWidth() - 5, y - 2);
				delete.render(graphics, mouseX, mouseY, tickDelta);
				visibility.render(graphics, mouseX, mouseY, tickDelta);
				graphics.drawString(font, name, x, y + entryHeight / 2 - 9 / 2, -1);
			}

			@Override
			public List<? extends NarratableEntry> narratables() {
				return ImmutableList.of(visibility, delete);
			}
		}
	}
}
