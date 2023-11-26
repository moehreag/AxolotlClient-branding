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

import java.net.URI;
import java.util.List;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.util.OSUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class DeviceCodeDisplayScreen extends Screen {
	private final Screen parent;
	private final String verificationUri, userCode;
	private final List<String> message;
	private final String title;
	private int ticksLeft;
	private String status;
	private boolean working;

	public DeviceCodeDisplayScreen(Screen parent, DeviceFlowData data) {
		super();
		this.title = I18n.translate("auth.add");
		this.parent = parent;
		this.message = Minecraft.getInstance().textRenderer.split(data.getMessage(), 400);
		this.verificationUri = data.getVerificationUri();
		this.userCode = data.getUserCode();
		this.ticksLeft = data.getExpiresIn() * 20;
		this.status = I18n.translate("auth.time_left",
			((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s");
		data.setStatusConsumer(s -> {
			if (s.equals("auth.finished")) {
				Minecraft.getInstance().submit(() -> Minecraft.getInstance().openScreen(parent));
			}
			working = true;
			buttons.clear();
			status = I18n.translate(s);
		});
	}

	@Override
	public void init() {
		buttons.add(new ButtonWidget(1, width / 2 - 100, height / 2,
			200, 20, I18n.translate("auth.copy_and_open")));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();
		super.render(mouseX, mouseY, delta);

		drawCenteredString(minecraft.textRenderer, title, width / 2, 25, -1);

		int y = height / 4;
		for (String text : message) {
			minecraft.textRenderer.drawWithShadow(text, width / 2f - minecraft.textRenderer.getWidth(text) / 2f, y, -1);
			y += 10;
		}
		drawCenteredString(minecraft.textRenderer, working ? status : I18n.translate("auth.time_left",
				((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s"),
			width / 2, y + 10, -1);
	}

	@Override
	public void tick() {
		ticksLeft--;
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 1) {
			setClipboard(userCode);
			OSUtil.getOS().open(URI.create(verificationUri), AxolotlClient.LOGGER);
		}
	}
}
