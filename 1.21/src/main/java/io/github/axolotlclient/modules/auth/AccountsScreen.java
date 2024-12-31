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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class AccountsScreen extends Screen {
	private final Screen parent;
	protected AccountsListWidget accountsListWidget;
	private ButtonWidget loginButton;
	private ButtonWidget deleteButton;
	private ButtonWidget refreshButton;

	public AccountsScreen(Screen currentScreen) {
		super(Text.translatable("accounts"));
		this.parent = currentScreen;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 20, 16777215);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 294) {
			this.refresh();
			return true;
		} else if (this.accountsListWidget.getSelectedOrNull() != null) {
			if (keyCode != 257 && keyCode != 335) {
				return this.accountsListWidget.keyPressed(keyCode, scanCode, modifiers);
			} else {
				this.login();
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public void init() {

		accountsListWidget = new AccountsListWidget(this, client, width, height, 32, height - 64, 35);
		addDrawableSelectableElement(accountsListWidget);

		accountsListWidget.setAccounts(Auth.getInstance().getAccounts());

		addDrawableSelectableElement(loginButton = new ButtonWidget.Builder(Text.translatable("auth.login"),
			buttonWidget -> login()).positionAndSize(this.width / 2 - 154, this.height - 52, 150, 20).build());

		this.addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("auth.add"),
				button -> {
					if (!Auth.getInstance().allowOfflineAccounts()) {
						initMSAuth();
					} else {
						client.setScreen(new ConfirmScreen(result -> {
							if (!result) {
								client.setScreen(this);
								initMSAuth();
							} else {
								client.setScreen(new AddOfflineScreen(this));
							}
						}, Text.translatable("auth.add.choose"), Text.empty(), Text.translatable("auth.add.offline"), Text.translatable("auth.add.ms")));
					}
				})
			.positionAndSize(this.width / 2 + 4, this.height - 52, 150, 20).build());

		this.deleteButton = this.addDrawableSelectableElement(ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
			AccountsListWidget.Entry entry = this.accountsListWidget.getSelectedOrNull();
			if (entry != null) {
				button.active = false;
				Auth.getInstance().removeAccount(entry.getAccount());
				refresh();
			}
		}).positionAndSize(this.width / 2 - 50, this.height - 28, 100, 20).build());


		this.addDrawableSelectableElement(refreshButton = ButtonWidget.builder(Text.translatable("auth.refresh"), button -> refreshAccount())
			.positionAndSize(this.width / 2 - 154, this.height - 28, 100, 20)
			.build()
		);

		this.addDrawableSelectableElement(
			ButtonWidget.builder(CommonTexts.BACK, button -> this.client.setScreen(this.parent))
				.positionAndSize(this.width / 2 + 4 + 50, this.height - 28, 100, 20)
				.build()
		);
		updateButtonActivationStates();
	}

	private void initMSAuth() {
		Auth.getInstance().getAuth().startDeviceAuth().thenRun(() -> client.execute(this::refresh));
	}

	private void refreshAccount() {
		refreshButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedOrNull();
		if (entry != null) {
			entry.getAccount().refresh(Auth.getInstance().getAuth()).thenRun(() -> client.execute(() -> {
				Auth.getInstance().save();
				refresh();
			}));
		}
	}

	private void updateButtonActivationStates() {
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedOrNull();
		if (client.world == null && entry != null) {
			loginButton.active = entry.getAccount().isExpired() || !entry.getAccount().equals(Auth.getInstance().getCurrent());
			deleteButton.active = refreshButton.active = true;
		} else {
			loginButton.active = deleteButton.active = refreshButton.active = false;
		}
	}

	private void refresh() {
		this.client.setScreen(new AccountsScreen(this.parent));
	}

	private void login() {
		loginButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedOrNull();
		if (entry != null) {
			Auth.getInstance().login(entry.getAccount());
		}
	}

	public void select(AccountsListWidget.Entry entry) {
		this.accountsListWidget.setSelected(entry);
		this.updateButtonActivationStates();
	}
}
