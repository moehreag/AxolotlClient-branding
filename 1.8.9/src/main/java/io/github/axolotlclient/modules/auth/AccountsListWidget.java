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

package io.github.axolotlclient.modules.auth;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.Identifier;

public class AccountsListWidget extends EntryListWidget {

	private final AccountsScreen screen;
	private final List<Entry> entries = new ArrayList<>();
	private int selectedEntry = -1;

	public AccountsListWidget(AccountsScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setAccounts(List<Account> accounts) {
		accounts.forEach(account -> entries.add(new Entry(screen, account)));
	}

	@Override
	protected int getEntryCount() {
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
	public EntryListWidget.Entry getEntry(int i) {
		return entries.get(i);
	}

	public Entry getSelectedEntry() {
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

	@Environment(EnvType.CLIENT)
	public static class Entry extends DrawUtil implements EntryListWidget.Entry {

		private static final Identifier checkmark = new Identifier("axolotlclient", "textures/check.png");
		private static final Identifier warningSign = new Identifier("axolotlclient", "textures/warning.png");

		private final AccountsScreen screen;
		private final Account account;
		private final MinecraftClient client;
		private long time;

		public Entry(AccountsScreen screen, Account account) {
			this.screen = screen;
			this.account = account;
			this.client = MinecraftClient.getInstance();
		}

		@Override
		public void updatePosition(int i, int j, int k) {

		}

		@Override
		public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
			client.textRenderer.draw(account.getName(), x + 3 + 33, y + 1, -1);
			client.textRenderer.draw(account.getUuid(), x + 3 + 33, y + 12, 8421504);
			GlStateManager.color(1, 1, 1, 1);
			if (Auth.getInstance().getCurrent().equals(account)) {
				client.getTextureManager().bindTexture(checkmark);
				drawTexture(x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			} else if (account.isExpired()) {
				client.getTextureManager().bindTexture(warningSign);
				drawTexture(x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			}
			GlStateManager.color(1, 1, 1, 1);
			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(account));
			GlStateManager.enableBlend();
			drawTexture(x - 1, y - 1, 8, 8, 8, 8, 33, 33, 64, 64);
			drawTexture(x - 1, y - 1, 40, 8, 8, 8, 33, 33, 64, 64);
			GlStateManager.disableBlend();

		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			this.screen.select(index);
			if (MinecraftClient.getTime() - this.time < 250L && client.world == null) {
				Auth.getInstance().login(account);
			}

			this.time = MinecraftClient.getTime();
			return false;
		}

		@Override
		public void mouseReleased(int i, int j, int k, int l, int m, int n) {

		}

		public Account getAccount() {
			return account;
		}
	}
}
