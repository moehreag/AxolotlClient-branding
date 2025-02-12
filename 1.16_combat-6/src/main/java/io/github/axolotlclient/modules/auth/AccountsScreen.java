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

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class AccountsScreen extends Screen {
	private final Screen parent;
	protected AccountsListWidget accountsListWidget;
	private ButtonWidget loginButton;
	private ButtonWidget deleteButton;
	private ButtonWidget refreshButton;

	public AccountsScreen(Screen currentScreen) {
		super(new TranslatableText("accounts"));
		this.parent = currentScreen;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.accountsListWidget.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 294) {
			this.refresh();
			return true;
		} else if (this.accountsListWidget.getSelected() != null) {
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
		addChild(accountsListWidget);

		accountsListWidget.setAccounts(Auth.getInstance().getAccounts());

		addButton(loginButton = new ButtonWidget(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableText("auth.login"),
			buttonWidget -> login()));

		this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 52, 150, 20, new TranslatableText("auth.add"),
			button -> {
				if (!Auth.getInstance().allowOfflineAccounts()) {
					initMSAuth();
				} else {
					client.openScreen(new ConfirmScreen(result -> {
						if (!result) {
							client.openScreen(this);
							initMSAuth();
						} else {
							client.openScreen(new AddOfflineScreen(this));
						}
					}, new TranslatableText("auth.add.choose"), LiteralText.EMPTY, new TranslatableText("auth.add.offline"), new TranslatableText("auth.add.ms")));
				}
			}));

		this.deleteButton = this.addButton(new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, new TranslatableText("selectServer.delete"), button -> {
			AccountsListWidget.Entry entry = this.accountsListWidget.getSelected();
			if (entry != null) {
				button.active = false;
				Auth.getInstance().removeAccount(entry.getAccount());
				refresh();
			}
		}));


		this.addButton(refreshButton = new ButtonWidget(this.width / 2 - 154, this.height - 28, 100, 20,
			new TranslatableText("auth.refresh"), button -> refreshAccount()));

		this.addButton(new ButtonWidget(this.width / 2 + 4 + 50, this.height - 28, 100, 20,
			ScreenTexts.BACK, button -> this.client.openScreen(this.parent)));
		updateButtonActivationStates();
	}

	@Override
	public void removed() {
		Auth.getInstance().save();
	}

	private void initMSAuth() {
		Auth.getInstance().getAuth().startDeviceAuth().thenRun(() -> client.execute(this::refresh));
	}

	private void refreshAccount() {
		refreshButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelected();
		if (entry != null) {
			entry.getAccount().refresh(Auth.getInstance().getAuth()).thenRun(() -> client.execute(() -> {
				Auth.getInstance().save();
				refresh();
			}));
		}
	}

	private void updateButtonActivationStates() {
		AccountsListWidget.Entry entry = accountsListWidget.getSelected();
		if (client.world == null && entry != null) {
			loginButton.active = entry.getAccount().isExpired() || !entry.getAccount().equals(Auth.getInstance().getCurrent());
			deleteButton.active = refreshButton.active = true;
		} else {
			loginButton.active = deleteButton.active = refreshButton.active = false;
		}
	}

	private void refresh() {
		this.client.openScreen(new AccountsScreen(this.parent));
	}

	private void login() {
		loginButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelected();
		if (entry != null) {
			Auth.getInstance().login(entry.getAccount());
		}
	}

	public void select(AccountsListWidget.Entry entry) {
		this.accountsListWidget.setSelected(entry);
		this.updateButtonActivationStates();
	}
}
