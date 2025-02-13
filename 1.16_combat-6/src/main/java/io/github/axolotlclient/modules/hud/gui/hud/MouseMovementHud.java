/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class MouseMovementHud extends BoxHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "mousemovementhud");

	private final GraphicsOption mouseMovementIndicatorInner = new GraphicsOption("mouseMovementIndicator", new int[][]{
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, -1, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0}
	});
	private final GraphicsOption mouseMovementIndicatorOuter = new GraphicsOption("mouseMovementIndicatorOuter", new int[][]{
		new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
		new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
	});

	private float mouseX = 0;
	private float mouseY = 0;
	private float lastMouseX = 0;
	private float lastMouseY = 0;

	public MouseMovementHud() {
		super(53, 35, true);
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void onPlayerDirectionChange(PlayerDirectionChangeEvent event) {
		// Implementation credit goes to TheKodeToad
		// This project has the author's approval to use this
		// https://github.com/Sol-Client/Client/blob/main/game/src/main/java/io/github/solclient/client/mod/impl/hud/keystrokes/KeystrokesMod.java
		mouseX += (event.getYaw() - event.getPrevYaw()) / 7F;
		mouseY += (event.getPitch() - event.getPrevPitch()) / 7F;
		// 0, 0 will be the center of the HUD element
		float halfWidth = getWidth() / 2f;
		mouseX = MathHelper.clamp(mouseX, -halfWidth + 4, halfWidth - 4);
		mouseY = MathHelper.clamp(mouseY, -13, 13);
	}

	@Override
	public void renderComponent(MatrixStack graphics, float delta) {
		int spaceY = getRawY();
		int spaceX = getRawX();

		float calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * delta)) - 5;
		float calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * delta)) - 5;

		io.github.axolotlclient.util.Util.bindTexture(mouseMovementIndicatorInner);
		drawTexture(graphics,
			spaceX + (width / 2) - 7 / 2 - 1, spaceY + 17 - (7 / 2), 0, 0, 7, 7, 7, 7);

		graphics.translate(calculatedMouseX, calculatedMouseY, 0); // Woah KodeToad, good use of translate

		io.github.axolotlclient.util.Util.bindTexture(mouseMovementIndicatorOuter);
		drawTexture(graphics,
			spaceX + (width / 2) - 1, spaceY + 17, 0, 0, 11, 11, 11, 11);
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack graphics, float delta) {
		renderComponent(graphics, delta);
	}

	@Override
	public void tick() {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX *= .75f;
		mouseY *= .75f;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(mouseMovementIndicatorInner);
		options.add(mouseMovementIndicatorOuter);
		return options;
	}
}
