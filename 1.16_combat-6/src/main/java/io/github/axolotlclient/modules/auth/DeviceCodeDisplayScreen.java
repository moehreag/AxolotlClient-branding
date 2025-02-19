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

import java.net.URI;
import java.util.List;

import io.github.axolotlclient.util.OSUtil;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class DeviceCodeDisplayScreen extends Screen {
	private final Screen parent;
	private final String verificationUri, userCode;
	private final List<OrderedText> message;
	private int ticksLeft;
	private Text status;
	private boolean working;
	private final Identifier qrCode;

	public DeviceCodeDisplayScreen(Screen parent, DeviceFlowData data) {
		super(new TranslatableText("auth.add"));
		this.parent = parent;
		this.message = MinecraftClient.getInstance().textRenderer.wrapLines(Text.of(data.getMessage()), 400);
		this.verificationUri = data.getVerificationUri();
		this.userCode = data.getUserCode();
		this.ticksLeft = data.getExpiresIn() * 20;
		this.qrCode = Util.getTexture(data.getQrCode(), "device_auth_" + data.getUserCode());
		this.status = new TranslatableText("auth.time_left",
			((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s");
		data.setStatusConsumer(s -> {
			if (s.equals("auth.finished")) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().openScreen(parent));
			}
			working = true;
			buttons.clear();
			status = new TranslatableText(s);
		});
	}

	@Override
	protected void init() {
		addButton(new ButtonWidget(width / 2 - 100, height / 2,
			200, 20, new TranslatableText("auth.copy_and_open"),
			buttonWidget -> {
				client.keyboard.setClipboard(userCode);
				OSUtil.getOS().open(URI.create(verificationUri));
			}));
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);

		drawCenteredText(graphics, client.textRenderer, title, width / 2, 25, -1);

		int y = height / 4;
		for (OrderedText orderedText : message) {
			client.textRenderer.drawWithShadow(graphics, orderedText, width / 2f - client.textRenderer.getWidth(orderedText) / 2f, y, -1);
			y += 10;
		}
		drawCenteredText(graphics, client.textRenderer, working ? status : new TranslatableText("auth.time_left",
				((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s"),
			width / 2, y + 10, -1);

		y = height / 2 + 30;
		if (height - y > 40) {
			int qrImageSize = height - y - 20;
			client.getTextureManager().bindTexture(qrCode);
			drawTexture(graphics, width / 2 - qrImageSize / 2, y, 0, 0, qrImageSize, qrImageSize, qrImageSize, qrImageSize);
		}
	}

	@Override
	public void tick() {
		ticksLeft--;
	}
}
