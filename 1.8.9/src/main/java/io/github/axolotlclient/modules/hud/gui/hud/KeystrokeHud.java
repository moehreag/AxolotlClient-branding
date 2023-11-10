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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class KeystrokeHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "keystrokehud");
	private static final Minecraft client = Minecraft.getInstance();
	private final ColorOption pressedTextColor = new ColorOption("heldtextcolor", new Color(0xFF000000));
	private final ColorOption pressedBackgroundColor = new ColorOption("heldbackgroundcolor", new Color(0x64FFFFFF));
	private final ColorOption pressedOutlineColor = new ColorOption("heldoutlinecolor", ClientColors.BLACK);
	private final BooleanOption mouseMovement = new BooleanOption("mousemovement", false, this::onMouseMovementOption);
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
	private ArrayList<Keystroke> keystrokes;
	private float mouseX = 0;
	private float mouseY = 0;
	private float lastMouseX = 0;
	private float lastMouseY = 0;

	public KeystrokeHud() {
		super(53, 61, true);
		Events.KEYBIND_CHANGE.register(key -> setKeystrokes());
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void setKeystrokes() {
		if (Util.getWindow() == null) {
			keystrokes = null;
			return;
			// Wait until render is called
		}
		keystrokes = new ArrayList<>();
		DrawPosition pos = getPos();
		// LMB
		keystrokes.add(createFromKey(new Rectangle(0, 36, 26, 17), pos, client.options.attackKey));
		// RMB
		keystrokes.add(createFromKey(new Rectangle(27, 36, 26, 17), pos, client.options.usekey));
		// W
		keystrokes.add(createFromKey(new Rectangle(18, 0, 17, 17), pos, client.options.forwardKey));
		// A
		keystrokes.add(createFromKey(new Rectangle(0, 18, 17, 17), pos, client.options.leftKey));
		// S
		keystrokes.add(createFromKey(new Rectangle(18, 18, 17, 17), pos, client.options.backKey));
		// D
		keystrokes.add(createFromKey(new Rectangle(36, 18, 17, 17), pos, client.options.rightKey));

		// Space
		keystrokes.add(new Keystroke(new Rectangle(0, 54, 53, 7), pos, client.options.jumpKey, (stroke) -> {
			Rectangle bounds = stroke.bounds;
			Rectangle spaceBounds = new Rectangle(bounds.x() + stroke.offset.x() + 4,
				bounds.y() + stroke.offset.y() + 2, bounds.width() - 8, 1);
			fillRect(spaceBounds, stroke.getFGColor());
			if (shadow.get()) {
				fillRect(spaceBounds.offset(1, 1), new Color(
					(stroke.getFGColor().toInt() & 16579836) >> 2 | stroke.getFGColor().toInt() & -16777216));
			}
		}));
		KeyBinding.releaseAll();
		KeyBinding.resetMapping();

		onMouseMovementOption(mouseMovement.get());
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

	public Keystroke createFromKey(Rectangle bounds, DrawPosition offset, KeyBinding key) {
		String name = getMouseKeyBindName(key)
			.orElse(GameOptions.getKeyName(key.getKeyCode()).toUpperCase());
		if (name.length() > 4) {
			name = name.substring(0, 2);
		}
		return createFromString(bounds, offset, key, name);
	}

	public void onMouseMovementOption(boolean value) {
		int baseHeight = 61;
		if (value) {
			baseHeight += 36;
		}
		height = baseHeight;
		onBoundsUpdate();
	}

	public static Optional<String> getMouseKeyBindName(KeyBinding keyBinding) {
		if (keyBinding.getName().equalsIgnoreCase(client.options.attackKey.getName())) {
			return Optional.of("LMB");
		} else if (keyBinding.getName().equalsIgnoreCase(client.options.usekey.getName())) {
			return Optional.of("RMB");
		} else if (keyBinding.getName().equalsIgnoreCase(client.options.pickItemKey.getName())) {
			return Optional.of("MMB");
		}
		return Optional.empty();
	}

	public Keystroke createFromString(Rectangle bounds, DrawPosition offset, KeyBinding key, String word) {
		return new Keystroke(bounds, offset, key, (stroke) -> {
			Rectangle strokeBounds = stroke.bounds;
			float x = (strokeBounds.x() + stroke.offset.x() + ((float) strokeBounds.width() / 2))
				- ((float) client.textRenderer.getWidth(word) / 2);
			float y = strokeBounds.y() + stroke.offset.y() + ((float) strokeBounds.height() / 2) - 4;

			drawString(word, (int) x, (int) y, stroke.getFGColor().toInt(), shadow.get());
		});
	}

	@Override
	public void render(float delta) {
		GlStateManager.pushMatrix();
		scale();
		renderComponent(delta);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderComponent(float delta) {
		if (keystrokes == null) {
			setKeystrokes();
		}
		for (Keystroke stroke : keystrokes) {
			stroke.render();
		}
		if (mouseMovement.get()) {
			int spaceY = 62 + getRawY();
			int spaceX = getRawX();
			if (background.get()) {
				DrawUtil.fillRect(spaceX, spaceY, width, 35, backgroundColor.get().toInt());
			}
			if (outline.get()) {
				DrawUtil.outlineRect(spaceX, spaceY, width, 35, outlineColor.get().toInt());
			}

			float calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * delta)) - 5;
			float calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * delta)) - 5;

			GlStateManager.color3f(1, 1, 1);
			Util.bindTexture(mouseMovementIndicatorInner);
			drawTexture(spaceX + (width / 2) - 7 / 2 - 1, spaceY + 17 - (7 / 2), 0, 0, 7, 7, 7, 7);

			GlStateManager.translatef(calculatedMouseX, calculatedMouseY, 0); // Woah KodeToad, good use of translate

			Util.bindTexture(mouseMovementIndicatorOuter);
			drawTexture(spaceX + (width / 2) - 1, spaceY + 17, 0, 0, 11, 11, 11, 11);
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		renderComponent(delta);
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		DrawPosition pos = getPos();
		if (keystrokes == null) {
			setKeystrokes();
		}
		for (Keystroke stroke : keystrokes) {
			stroke.offset = pos;
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX *= .75f;
		mouseY *= .75f;
	}

	@Override
	protected boolean getShadowDefault() {
		return false;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		// We want a specific order since this is a more complicated entry
		List<Option<?>> options = new ArrayList<>();
		options.add(enabled);
		options.add(scale);
		options.add(mouseMovement);
		options.add(mouseMovementIndicatorInner);
		options.add(mouseMovementIndicatorOuter);
		options.add(textColor);
		options.add(pressedTextColor);
		options.add(shadow);
		options.add(background);
		options.add(backgroundColor);
		options.add(pressedBackgroundColor);
		options.add(outline);
		options.add(outlineColor);
		options.add(pressedOutlineColor);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public interface KeystrokeRenderer {

		void render(Keystroke stroke);
	}

	public class Keystroke {

		protected final KeyBinding key;
		protected final KeystrokeRenderer render;
		protected final Rectangle bounds;
		private final int animTime = 100;
		protected DrawPosition offset;
		private float start = -1;
		private boolean wasPressed = false;

		public Keystroke(Rectangle bounds, DrawPosition offset, KeyBinding key, KeystrokeRenderer render) {
			this.bounds = bounds;
			this.offset = offset;
			this.key = key;
			this.render = render;
		}

		public Color getFGColor() {
			return !key.isPressed() ? ClientColors.blend(textColor.get(), pressedTextColor.get(), getPercentPressed())
				: ClientColors.blend(pressedTextColor.get(), textColor.get(), getPercentPressed());
		}

		private float getPercentPressed() {
			return start == -1 ? 1 : MathHelper.clamp((Minecraft.getTime() - start) / animTime, 0, 1);
		}

		public void render() {
			renderStroke();
			render.render(this);
		}

		public void renderStroke() {
			if (key.isPressed() == wasPressed) {
				start = Minecraft.getTime();
			}
			Rectangle rect = bounds.offset(offset);
			if (background.get()) {
				fillRect(rect, getColor());
			}
			if (outline.get()) {
				outlineRect(rect, getOutlineColor());
			}
			if ((Minecraft.getTime() - start) / animTime >= 1) {
				start = -1;
			}
			wasPressed = key.isPressed();
		}

		public Color getColor() {
			return !key.isPressed()
				? ClientColors.blend(backgroundColor.get(), pressedBackgroundColor.get(), getPercentPressed())
				: ClientColors.blend(pressedBackgroundColor.get(), backgroundColor.get(), getPercentPressed());
		}

		public Color getOutlineColor() {
			return !key.isPressed() ? ClientColors.blend(outlineColor.get(), pressedOutlineColor.get(), getPercentPressed())
				: ClientColors.blend(pressedOutlineColor.get(), outlineColor.get(), getPercentPressed());
		}
	}
}
