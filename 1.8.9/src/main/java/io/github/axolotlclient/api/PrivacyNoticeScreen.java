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

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;

public class PrivacyNoticeScreen extends Screen {

	private static final URI PRIVACY_POLICY_URL = URI.create("https://axolotlclient.xyz/privacy");

	private final Screen parent;
	private List<String> message;
	private final Consumer<Boolean> accepted;

	protected PrivacyNoticeScreen(Screen parent, Consumer<Boolean> accepted) {
		super();
		this.parent = parent;
		this.accepted = accepted;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		drawCenteredString(this.textRenderer, I18n.translate("api.privacyNotice"), this.width / 2, getTitleY(), -1);
		int k = 90;

		for (String string : this.message) {
			this.drawCenteredString(this.textRenderer, string, this.width / 2, k, 16777215);
			k += this.textRenderer.fontHeight;
		}
		super.render(mouseX, mouseY, delta);
	}

	@Override
	public void init() {

		message = client.textRenderer.wrapLines(
			I18n.translate("api.privacyNotice.description"), width - 50);
		int y = MathHelper.clamp(this.getMessageY() + this.getMessagesHeight() + 20, this.height / 6 + 96, this.height - 24);
		this.addButtons(y);
	}

	private void addButtons(int y) {
		buttons.add(new ButtonWidget(1, width / 2 - 50, y, 100, 20,
			I18n.translate("api.privacyNotice.accept")));
		buttons.add(new ButtonWidget(0, width / 2 + 55, y, 100, 20,
			I18n.translate("api.privacyNotice.deny")));
		buttons.add(new ButtonWidget(2, width / 2 - 155, y, 100, 20,
			I18n.translate("api.privacyNotice.openPolicy")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			client.setScreen(parent);
			APIOptions.getInstance().enabled.set(false);
			APIOptions.getInstance().privacyAccepted.set("denied");
			accepted.accept(false);
		} else if (buttonWidget.id == 1) {
			client.setScreen(parent);
			APIOptions.getInstance().privacyAccepted.set("accepted");
			accepted.accept(true);
		} else if (buttonWidget.id == 2) {
			OSUtil.getOS().open(PRIVACY_POLICY_URL, AxolotlClient.LOGGER);
		}
	}

	private int getTitleY() {
		int i = (this.height - this.getMessagesHeight()) / 2;
		return MathHelper.clamp(i - 20 - 9, 10, 80);
	}

	private int getMessageY() {
		return this.getTitleY() + 20;
	}

	private int getMessagesHeight() {
		return this.message.size() * 9;
	}
}
