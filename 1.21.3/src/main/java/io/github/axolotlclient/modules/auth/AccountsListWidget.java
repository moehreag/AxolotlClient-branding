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

package io.github.axolotlclient.modules.auth;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AccountsListWidget extends ObjectSelectionList<AccountsListWidget.Entry> {

	private final AccountsScreen screen;

	public AccountsListWidget(AccountsScreen screen, Minecraft client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, bottom - top, top, entryHeight);
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
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 30;
	}

	@Override
	public boolean isFocused() {
		return this.screen.getFocused() == this;
	}

	@Environment(EnvType.CLIENT)
	public static class Entry extends ObjectSelectionList.Entry<AccountsListWidget.Entry> {

		private static final ResourceLocation checkmark =
			ResourceLocation.fromNamespaceAndPath("axolotlclient", "textures/check.png");
		private static final ResourceLocation warningSign =
			ResourceLocation.fromNamespaceAndPath("axolotlclient", "textures/warning.png");

		private final AccountsScreen screen;
		private final Account account;
		private final Minecraft client;
		private long time;

		public Entry(AccountsScreen screen, Account account) {
			this.screen = screen;
			this.account = account;
			this.client = Minecraft.getInstance();
		}

		@Override
		public Component getNarration() {
			return Component.literal(account.getName());
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			RenderSystem.enableBlend();
			if (Auth.getInstance().getCurrent().equals(account)) {
				graphics.blit(RenderType::guiTextured, checkmark, x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			} else if (account.isExpired()) {
				graphics.blit(RenderType::guiTextured, warningSign, x - 35, y + 1, 0, 0, 32, 32, 32, 32);
			}
			ResourceLocation texture = Auth.getInstance().getSkinTexture(account);
			PlayerFaceRenderer.draw(graphics, texture, x - 1, y - 1, 33, true, false, -1);

			graphics.drawString(client.font, account.getName(), x + 3 + 33, y + 1, -1, false);
			graphics.drawString(client.font, account.getUuid(), x + 3 + 33, y + 12, 8421504, false);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.screen.select(this);
			if (Util.getMillis() - this.time < 250L && client.level == null) {
				Auth.getInstance().login(account);
			}

			this.time = Util.getMillis();
			return false;
		}

		public Account getAccount() {
			return account;
		}
	}
}
