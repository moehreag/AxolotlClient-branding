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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.axolotlclient.api.requests.AccountUsernameRequest;
import io.github.axolotlclient.api.types.User;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;

public class UsernameManagementScreen extends Screen {

	private final Screen parent;
	private UsernameListWidget widget;
	private final String title = I18n.translate("api.account.usernames");

	public UsernameManagementScreen(Screen parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void init() {
		if (API.getInstance().isAuthenticated()) {
			widget = new UsernameListWidget(API.getInstance().getSelf().getPreviousUsernames());
		} else {
			widget = new UsernameListWidget(Collections.emptyList());
		}

		buttons.add(new ButtonWidget(0, width / 2 - 75, height - 25 - 20, 150, 20, I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			minecraft.openScreen(parent);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		widget.render(mouseX, mouseY, delta);
		super.render(mouseX, mouseY, delta);
		textRenderer.drawWithShadow(title, width / 2f - textRenderer.getWidth(title) / 2f, 25, -1);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		widget.handleMouse();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		if (!widget.mouseClicked(i, j, k)) {
			super.mouseClicked(i, j, k);
		}
	}

	@Override
	protected void mouseReleased(int i, int j, int k) {
		if (!widget.mouseReleased(i, j, k)) {
			super.mouseReleased(i, j, k);
		}
	}

	private class UsernameListWidget extends EntryListWidget {

		private final List<UsernameListEntry> entries = new ArrayList<>();

		public UsernameListWidget(List<User.OldUsername> names) {
			super(UsernameManagementScreen.this.minecraft, UsernameManagementScreen.this.width, UsernameManagementScreen.this.height, 45, UsernameManagementScreen.this.height - 55, 20);

			names.forEach(n -> entries.add(new UsernameListEntry(n)));
		}

		@Override
		protected int getScrollbarPosition() {
			return (minX + this.width / 2 - this.getRowWidth() / 2) + getRowWidth() + 10;
		}

		@Override
		public int getRowWidth() {
			return 310;
		}

		@Override
		public Entry getEntry(int i) {
			return entries.get(i);
		}

		@Override
		protected int size() {
			return entries.size();
		}

		private class UsernameListEntry implements Entry {

			private final ButtonWidget visibility;
			private final ButtonWidget delete;
			private final User.OldUsername name;

			public UsernameListEntry(User.OldUsername name) {
				visibility = new ButtonWidget(1, 0, 0, 100, 20, I18n.translate("api.account.usernames.public", name.isPub()));
				delete = new ButtonWidget(1, 0, 0, 100, 20, I18n.translate("api.account.usernames.delete"));
				this.name = name;
			}

			@Override
			public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
				int deleteX = UsernameListWidget.this.getScrollbarPosition() - delete.getWidth() - 10;
				delete.x = deleteX;
				delete.y = y - 2;
				visibility.x = deleteX - visibility.getWidth() - 5;
				visibility.y = y - 2;
				delete.render(minecraft, mouseX, mouseY);
				visibility.render(minecraft, mouseX, mouseY);
				textRenderer.drawWithShadow(name.getName(), x, y + entryHeight / 2f - 9 / 2f, -1);
			}

			@Override
			public void renderOutOfBounds(int i, int j, int k) {

			}

			@Override
			public boolean mouseClicked(int i, int mouseX, int mouseY, int l, int m, int n) {
				if (delete.isMouseOver(minecraft, mouseX, mouseY)) {
					minecraft.openScreen(new ConfirmScreen((b, un) -> {
						if (b) {
							AccountUsernameRequest.delete(name.getName()).thenRun(() ->
								UsernameListWidget.this.entries.remove(this));
						}
						minecraft.openScreen(UsernameManagementScreen.this);
					}, I18n.translate("api.account.confirm_deletion"),
						I18n.translate("api.account.usernames.delete.desc"), 0));
					return true;
				} else if (visibility.isMouseOver(minecraft, mouseX, mouseY)) {
					name.setPub(!name.isPub());
					visibility.message = I18n.translate("api.account.usernames.public", name.isPub());
					AccountUsernameRequest.post(name.getName(), name.isPub());
					return true;
				}
				return false;
			}

			@Override
			public void mouseReleased(int i, int j, int k, int l, int m, int n) {

			}
		}
	}
}
