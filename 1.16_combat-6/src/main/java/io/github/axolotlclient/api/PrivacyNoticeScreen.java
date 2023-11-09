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
import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.minecraft.class_5489;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class PrivacyNoticeScreen extends Screen {

	private static final URI PRIVACY_POLICY_URL = URI.create("https://axolotlclient.xyz/privacy");

	private final Screen parent;
	private class_5489 message;
	private final Consumer<Boolean> accepted;

	protected PrivacyNoticeScreen(Screen parent, Consumer<Boolean> accepted) {
		super(new TranslatableText("api.privacyNotice"));
		this.parent = parent;
		this.accepted = accepted;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, getTitleY(), -1);
		message.method_30888(matrices, width / 2, getMessageY());
	}

	@Override
	public String getNarrationMessage() {
		return super.getNarrationMessage() + ". " + new TranslatableText("api.privacyNotice.description").getString();
	}

	@Override
	protected void init() {

		message = class_5489.method_30890(client.textRenderer,
			new TranslatableText("api.privacyNotice.description"), width - 50);
		int y = MathHelper.clamp(this.getMessageY() + this.getMessagesHeight() + 20, this.height / 6 + 96, this.height - 24);
		this.addButtons(y);
	}

	private void addButtons(int y) {
		addButton(new ButtonWidget(width / 2 - 50, y, 100, 20,
			new TranslatableText("api.privacyNotice.accept"), buttonWidget -> {
			client.openScreen(parent);
			APIOptions.getInstance().privacyAccepted.set("accepted");
			accepted.accept(true);
		}));
		addButton(new ButtonWidget(width / 2 + 55, y, 100, 20,
			new TranslatableText("api.privacyNotice.deny"), buttonWidget -> {
			client.openScreen(parent);
			APIOptions.getInstance().enabled.set(false);
			APIOptions.getInstance().privacyAccepted.set("denied");
			accepted.accept(false);
		}));
		addButton(new ButtonWidget(width / 2 - 155, y, 100, 20,
			new TranslatableText("api.privacyNotice.openPolicy"), buttonWidget -> {
			OSUtil.getOS().open(PRIVACY_POLICY_URL, AxolotlClient.LOGGER);
		}));
	}

	private int getTitleY() {
		int i = (this.height - this.getMessagesHeight()) / 2;
		return MathHelper.clamp(i - 20 - 9, 10, 80);
	}

	private int getMessageY() {
		return this.getTitleY() + 20;
	}

	private int getMessagesHeight() {
		return this.message.method_30887() * 9;
	}
}
