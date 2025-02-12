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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;

public class AccountsScreen extends Screen {
	private final Screen parent;
	private final String title;
	protected AccountsListWidget accountsListWidget;
	private ButtonWidget loginButton;
	private ButtonWidget deleteButton;
	private ButtonWidget refreshButton;

	public AccountsScreen(Screen currentScreen) {
		title = I18n.translate("accounts");
		this.parent = currentScreen;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.renderBackground();
		this.accountsListWidget.render(mouseX, mouseY, delta);
		drawCenteredString(this.textRenderer, this.title, this.width / 2, 20, 16777215);
		super.render(mouseX, mouseY, delta);
	}

	@Override
	protected void keyPressed(char c, int i) {
		int j = this.accountsListWidget.getSelected();
		EntryListWidget.Entry entry = j < 0 ? null : this.accountsListWidget.getEntry(j);
		if (i == 63) {
			this.refresh();
		} else {
			if (j >= 0) {
				if (i == 200) {
					if (isShiftDown()) {
						if (j > 0 && entry instanceof AccountsListWidget.Entry) {
							this.select(this.accountsListWidget.getSelected() - 1);
							this.accountsListWidget.scroll(-this.accountsListWidget.getEntryHeight());
						}
					} else if (j > 0) {
						this.select(this.accountsListWidget.getSelected() - 1);
						this.accountsListWidget.scroll(-this.accountsListWidget.getEntryHeight());
					} else {
						this.select(-1);
					}
				} else if (i == 208) {
					if (isShiftDown()) {
						this.select(j + 1);
						this.accountsListWidget.scroll(this.accountsListWidget.getEntryHeight());
					} else if (j < this.accountsListWidget.size()) {
						this.select(this.accountsListWidget.getSelected() + 1);
						this.accountsListWidget.scroll(this.accountsListWidget.getEntryHeight());
					} else {
						this.select(-1);
					}
				} else if (i != 28 && i != 156) {
					super.keyPressed(c, i);
				} else {
					this.buttonClicked(this.buttons.get(2));
				}
			} else {
				super.keyPressed(c, i);
			}
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		this.accountsListWidget.mouseClicked(i, j, k);
	}

	@Override
	protected void mouseReleased(int i, int j, int k) {
		super.mouseReleased(i, j, k);
		this.accountsListWidget.mouseReleased(i, j, k);
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		switch (buttonWidget.id) {
			case 0:
				this.minecraft.openScreen(this.parent);
				break;
			case 1:
				login();
				break;
			case 2:
				if (Auth.getInstance().allowOfflineAccounts()) {
					minecraft.openScreen(new ConfirmScreen(this, I18n.translate("auth.add.choose"), "", I18n.translate("auth.add.offline"), I18n.translate("auth.add.ms"), 234));
				} else {
					initMSAuth();
				}
				break;
			case 3:
				AccountsListWidget.Entry entry = this.accountsListWidget.getSelectedEntry();
				if (entry != null) {
					buttonWidget.active = false;
					Auth.getInstance().removeAccount(entry.getAccount());
					refresh();
				}
				break;
			case 4:
				refreshAccount();
				break;
		}
	}

	@Override
	public void init() {

		accountsListWidget = new AccountsListWidget(this, minecraft, width, height, 32, height - 64, 35);

		accountsListWidget.setAccounts(Auth.getInstance().getAccounts());

		buttons.add(loginButton = new ButtonWidget(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.translate("auth.login")));

		this.buttons.add(new ButtonWidget(2, this.width / 2 + 4, this.height - 52, 150, 20, I18n.translate("auth.add")));

		this.buttons.add(this.deleteButton = new ButtonWidget(3, this.width / 2 - 50, this.height - 28, 100, 20, I18n.translate("selectServer.delete")));


		this.buttons.add(refreshButton = new ButtonWidget(4, this.width / 2 - 154, this.height - 28, 100, 20,
			I18n.translate("auth.refresh")));

		this.buttons.add(new ButtonWidget(0, this.width / 2 + 4 + 50, this.height - 28, 100, 20,
			I18n.translate("gui.back")));
		updateButtonActivationStates();
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		this.accountsListWidget.handleMouse();
	}

	@Override
	public void removed() {
		Auth.getInstance().save();
	}

	@Override
	public void confirmResult(boolean bl, int i) {
		if (i == 234) {
			if (!bl) {
				minecraft.openScreen(this);
				initMSAuth();
			} else {
				minecraft.openScreen(new AddOfflineScreen(this));
			}
		}
	}

	private void refresh() {
		this.minecraft.openScreen(new AccountsScreen(this.parent));
	}

	public void select(int index) {
		this.accountsListWidget.setSelected(index);
		this.updateButtonActivationStates();
	}

	private void updateButtonActivationStates() {
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedEntry();
		if (minecraft.world == null && entry != null) {
			loginButton.active = entry.getAccount().isExpired() || !entry.getAccount().equals(Auth.getInstance().getCurrent());
			deleteButton.active = refreshButton.active = true;
		} else {
			loginButton.active = deleteButton.active = refreshButton.active = false;
		}
	}

	private void login() {
		loginButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedEntry();
		if (entry != null) {
			Auth.getInstance().login(entry.getAccount());
		}
	}

	private void initMSAuth() {
		Auth.getInstance().getAuth().startDeviceAuth().thenRun(() -> minecraft.submit(this::refresh));
	}

	private void refreshAccount() {
		refreshButton.active = false;
		AccountsListWidget.Entry entry = accountsListWidget.getSelectedEntry();
		if (entry != null) {
			entry.getAccount().refresh(Auth.getInstance().getAuth()).thenRun(() -> minecraft.submit(() -> {
				Auth.getInstance().save();
				refresh();
			}));
		}
	}
}
