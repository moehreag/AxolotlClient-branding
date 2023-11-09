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

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class AccountsListWidget extends AlwaysSelectedEntryListWidget<AccountsListWidget.Entry> {

	private final AccountsScreen screen;

	public AccountsListWidget(AccountsScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		this.screen = screen;
	}

	public void setAccounts(List<Account> accounts) {
		accounts.forEach(account -> addEntry(new Entry(screen, account)));
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 85;
	}

	@Override
	protected int getScrollbarPositionX() {
		return super.getScrollbarPositionX() + 30;
	}

	@Override
	protected boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	@Environment(EnvType.CLIENT)
	public static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {

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
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			GlStateManager.color4f(1, 1, 1, 1);
			if (Auth.getInstance().getCurrent().equals(account)) {
				client.getTextureManager().bindTexture(checkmark);
				drawTexture(matrices, x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			} else if (account.isExpired()) {
				client.getTextureManager().bindTexture(warningSign);
				drawTexture(matrices, x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			}
			client.getTextureManager().bindTexture(Auth.getInstance().getSkinTexture(account));
			RenderSystem.enableBlend();
			drawTexture(matrices, x - 1, y - 1, 33, 33, 8, 8, 8, 8, 64, 64);
			drawTexture(matrices, x - 1, y - 1, 33, 33, 40, 8, 8, 8, 64, 64);
			RenderSystem.disableBlend();

			client.textRenderer.draw(matrices, account.getName(), x + 3 + 33, y + 1, -1);
			client.textRenderer.draw(matrices, account.getUuid(), x + 3 + 33, y + 12, 8421504);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.screen.select(this);
			if (Util.getMeasuringTimeMs() - this.time < 250L && client.world == null) {
				Auth.getInstance().login(account);
			}

			this.time = Util.getMeasuringTimeMs();
			return false;
		}

		public Account getAccount() {
			return account;
		}
	}
}
