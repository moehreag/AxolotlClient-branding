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

import io.github.axolotlclient.util.OSUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.net.URI;
import java.util.List;

public class DeviceCodeDisplayScreen extends Screen {
	private final Screen parent;
	private final String verificationUri, userCode;
	private final List<FormattedCharSequence> message;
	private int ticksLeft;
	private Component status;
	private boolean working;

	public DeviceCodeDisplayScreen(Screen parent, DeviceFlowData data) {
		super(Component.translatable("auth.add"));
		this.parent = parent;
		this.message = Minecraft.getInstance().font.split(Component.literal(data.getMessage()), 400);
		this.verificationUri = data.getVerificationUri();
		this.userCode = data.getUserCode();
		this.ticksLeft = data.getExpiresIn() * 20;
		this.status = Component.translatable("auth.time_left",
			((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s");
		data.setStatusConsumer(s -> {
			if (s.equals("auth.finished")) {
				minecraft.execute(() -> minecraft.setScreen(parent));
			}
			working = true;
			clearWidgets();
			status = Component.translatable(s);
		});
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(Component.translatable("auth.copy_and_open"),
			buttonWidget -> {
				minecraft.keyboardHandler.setClipboard(userCode);
				OSUtil.getOS().open(URI.create(verificationUri));
			}).bounds(width / 2 - 100, height / 2, 200, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredString(font, title, width / 2, 25, -1);

		int y = height / 4;
		for (FormattedCharSequence orderedText : message) {
			graphics.drawCenteredString(font, orderedText, width / 2, y, -1);
			y += 10;
		}
		graphics.drawCenteredString(font, working ? status : Component.translatable("auth.time_left",
				((ticksLeft / 20) / 60) + "m" + ((ticksLeft / 20) % 60) + "s"),
			width / 2, y + 10, -1);
	}

	@Override
	public void tick() {
		ticksLeft--;
	}
}
