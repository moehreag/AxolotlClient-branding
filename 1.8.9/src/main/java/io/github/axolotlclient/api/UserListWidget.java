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

package io.github.axolotlclient.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.types.PkSystem;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.modules.auth.Auth;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Formatting;

public class UserListWidget extends EntryListWidget {

	private final FriendsScreen screen;
	private final List<UserListEntry> entries = new ArrayList<>();
	private int selectedEntry = -1;

	public UserListWidget(FriendsScreen screen, Minecraft client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users) {
		users.forEach(user -> addEntry(new UserListEntry(user)));
	}

	public void addEntry(UserListEntry entry) {
		entries.add(entry.init(screen));
	}

	@Override
	protected int size() {
		return entries.size();
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 85;
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 30;
	}

	@Override
	protected boolean isEntrySelected(int i) {
		return i == this.selectedEntry;
	}

	@Override
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	public UserListEntry getSelectedEntry() {
		if (getSelected() < 0) {
			return null;
		}
		return entries.get(getSelected());
	}

	public int getSelected() {
		return this.selectedEntry;
	}

	public void setSelected(int i) {
		this.selectedEntry = i;
	}

	public static class UserListEntry extends GuiElement implements EntryListWidget.Entry {

		@Getter
		private final User user;
		private final Minecraft client;
		private long time;
		private String note;
		private FriendsScreen screen;

		public UserListEntry(User user, String note) {
			this(user);
			this.note = Formatting.ITALIC + note;
		}

		public UserListEntry(User user) {
			this.client = Minecraft.getInstance();
			this.user = user;
		}

		public UserListEntry init(FriendsScreen screen) {
			this.screen = screen;
			return this;
		}

		@Override
		public void renderOutOfBounds(int i, int j, int k) {

		}

		@Override
		public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
			if (user.isSystem()){
				String fronters = user.getSystem().getFronters().stream()
					.map(PkSystem.Member::getDisplayName).collect(Collectors.joining("/"));
				String tag = Formatting.ITALIC + Formatting.GRAY.toString() + "("+user.getSystem().getName()+"/"+user.getName()+")";
				client.textRenderer.draw(fronters+" "+tag, x+3, y+1, -1);
			} else {
				client.textRenderer.draw(user.getName(), x + 3 + 33, y + 1, -1);
			}
			client.textRenderer.draw(user.getStatus().getTitle(), x + 3 + 33, y + 12, 8421504);
			if (user.getStatus().isOnline()) {
				client.textRenderer.draw(user.getStatus().getDescription(), x + 3 + 40, y + 23, 8421504);
			}

			if (note != null) {
				client.textRenderer.draw(note, x + entryWidth - client.textRenderer.getWidth(note) - 2, y + entryHeight - 10, 8421504);
			}

			GlStateManager.color4f(1, 1, 1, 1);
			client.getTextureManager().bind(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()));
			GlStateManager.enableBlend();
			drawTexture(x - 1, y - 1, 8, 8, 8, 8, 33, 33, 64, 64);
			drawTexture(x - 1, y - 1, 40, 8, 8, 8, 33, 33, 64, 64);
			GlStateManager.disableBlend();
		}

		@Override
		public boolean mouseClicked(int i, int j, int k, int l, int m, int n) {
			this.screen.select(i);
			if (Minecraft.getTime() - this.time < 250L && client.world == null) {
				screen.openChat();
			}

			this.time = Minecraft.getTime();
			return false;
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}
	}
}
