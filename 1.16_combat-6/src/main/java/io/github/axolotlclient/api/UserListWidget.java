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

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.api.types.PkSystem;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.modules.auth.Auth;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class UserListWidget extends AlwaysSelectedEntryListWidget<UserListWidget.UserListEntry> {

	private final FriendsScreen screen;

	public UserListWidget(FriendsScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setUsers(List<User> users) {
		users.forEach(user -> addEntry(new UserListEntry(user)));
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 85;
	}

	public int addEntry(UserListEntry entry) {
		return super.addEntry(entry.init(screen));
	}

	@Override
	protected int getScrollbarPositionX() {
		return super.getScrollbarPositionX() + 30;
	}

	@Override
	public boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	public static class UserListEntry extends AlwaysSelectedEntryListWidget.Entry<UserListEntry> {

		@Getter
		private final User user;
		private final MinecraftClient client;
		private long time;
		private Text note;
		private FriendsScreen screen;

		@Getter
		private boolean outgoingRequest;

		public UserListEntry(User user, MutableText note) {
			this(user);
			this.note = note.formatted(Formatting.ITALIC);
		}

		public UserListEntry(User user) {
			this.client = MinecraftClient.getInstance();
			this.user = user;
		}

		public UserListEntry init(FriendsScreen screen) {
			this.screen = screen;
			return this;
		}

		public UserListEntry outgoing() {
			outgoingRequest = true;
			return this;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (user.isSystem()) {
				String fronters = user.getSystem().getFronters().stream()
					.map(PkSystem.Member::getDisplayName).collect(Collectors.joining("/"));
				String tag = Formatting.ITALIC + Formatting.GRAY.toString() + "(" + user.getSystem().getName() + "/" + user.getName() + ")";
				client.textRenderer.draw(matrices, fronters + " " + tag, x + 3, y + 1, -1);
			} else {
				client.textRenderer.draw(matrices, user.getName(), x + 3 + 33, y + 1, -1);
			}

			if (user.getStatus().isOnline() && user.getStatus().getActivity() != null) {
				client.textRenderer.draw(matrices, user.getStatus().getTitle(), x + 3 + 33, y + 12, 8421504);
				client.textRenderer.draw(matrices, user.getStatus().getDescription(), x + 3 + 40, y + 23, 8421504);
			} else if (user.getStatus().getLastOnline() != null) {
				client.textRenderer.draw(matrices, user.getStatus().getLastOnline(), x + 3 + 33, y + 12, 8421504);
			}

			if (note != null) {
				client.textRenderer.draw(matrices, note, x + entryWidth - client.textRenderer.getWidth(note) - 4, y + entryHeight - 10, 8421504);
			}

			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(user.getUuid(), user.getName()));
			RenderSystem.enableBlend();
			drawTexture(matrices, x - 1, y - 1, 33, 33, 8, 8, 8, 8, 64, 64);
			drawTexture(matrices, x - 1, y - 1, 33, 33, 40, 8, 8, 8, 64, 64);
			RenderSystem.disableBlend();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.screen.select(this);
			if (Util.getMeasuringTimeMs() - this.time < 250L && client.world == null) {
				screen.openChat();
			}

			this.time = Util.getMeasuringTimeMs();
			return false;
		}
	}
}
