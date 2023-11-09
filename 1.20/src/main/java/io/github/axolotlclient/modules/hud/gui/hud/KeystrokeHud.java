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

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.options.GraphicsOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.option.KeyBind;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class KeystrokeHud extends TextHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "keystrokehud");

	private final ColorOption pressedTextColor = new ColorOption("heldtextcolor", new Color(0xFF000000));
	private final ColorOption pressedBackgroundColor = new ColorOption("heldbackgroundcolor", 0x64FFFFFF);
	private final ColorOption pressedOutlineColor = new ColorOption("heldoutlinecolor", Color.BLACK);
	private final BooleanOption mouseMovement = new BooleanOption("mousemovement", this::onMouseMovementOption, false);
	private final GraphicsOption mouseMovementIndicatorInner = new GraphicsOption("mouseMovementIndicator", new int[][]{
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, -1, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0},
		new int[]{0, 0, 0, 0, 0, 0, 0}
	}, true);
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
	}, true);
	private final MinecraftClient client;
	private ArrayList<Keystroke> keystrokes;
	private float mouseX = 0;
	private float mouseY = 0;
	private float lastMouseX = 0;
	private float lastMouseY = 0;

	public KeystrokeHud() {
		super(53, 61, true);
		this.client = MinecraftClient.getInstance();
		Events.KEYBIND_CHANGE.register(key -> setKeystrokes());
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void setKeystrokes() {
		if (client.getWindow() == null) {
			keystrokes = null;
			return;
			// Wait until render is called
		}
		keystrokes = new ArrayList<>();
		DrawPosition pos = getPos();
		// LMB
		keystrokes.add(createFromKey(new Rectangle(0, 36, 26, 17), pos, client.options.attackKey));
		// RMB
		keystrokes.add(createFromKey(new Rectangle(27, 36, 26, 17), pos, client.options.useKey));
		// W
		keystrokes.add(createFromKey(new Rectangle(18, 0, 17, 17), pos, client.options.forwardKey));
		// A
		keystrokes.add(createFromKey(new Rectangle(0, 18, 17, 17), pos, client.options.leftKey));
		// S
		keystrokes.add(createFromKey(new Rectangle(18, 18, 17, 17), pos, client.options.backKey));
		// D
		keystrokes.add(createFromKey(new Rectangle(36, 18, 17, 17), pos, client.options.rightKey));

		// Space
		keystrokes.add(new Keystroke(new Rectangle(0, 54, 53, 7), pos, client.options.jumpKey, (stroke, matrices) -> {
			Rectangle bounds = stroke.bounds;
			Rectangle spaceBounds = new Rectangle(bounds.x() + stroke.offset.x() + 4,
				bounds.y() + stroke.offset.y() + 2, bounds.width() - 8, 1);
			fillRect(matrices, spaceBounds, stroke.getFGColor());
			if (shadow.get()) {
				fillRect(matrices, spaceBounds.offset(1, 1), new Color(
					(stroke.getFGColor().getAsInt() & 16579836) >> 2 | stroke.getFGColor().getAsInt() & -16777216));
			}
		}));
		KeyBind.resetAll();
		KeyBind.updatePressedStates();

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

	public Keystroke createFromKey(Rectangle bounds, DrawPosition offset, KeyBind key) {
		String name = getMouseKeyBindName(key).orElse(key.getKeyName().getString().toUpperCase());
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

	public static Optional<String> getMouseKeyBindName(KeyBind keyBinding) {
		if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(
			InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_1).getTranslationKey())) {
			return Optional.of("LMB");
		} else if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(
			InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_2).getTranslationKey())) {
			return Optional.of("RMB");
		} else if (keyBinding.getKeyTranslationKey().equalsIgnoreCase(
			InputUtil.Type.MOUSE.createFromKeyCode(GLFW.GLFW_MOUSE_BUTTON_3).getTranslationKey())) {
			return Optional.of("MMB");
		}
		return Optional.empty();
	}

	public Keystroke createFromString(Rectangle bounds, DrawPosition offset, KeyBind key, String word) {
		return new Keystroke(bounds, offset, key, (stroke, matrices) -> {
			Rectangle strokeBounds = stroke.bounds;
			float x = (strokeBounds.x() + stroke.offset.x() + ((float) strokeBounds.width() / 2))
				- ((float) client.textRenderer.getWidth(word) / 2);
			float y = strokeBounds.y() + stroke.offset.y() + ((float) strokeBounds.height() / 2) - 4;

			drawString(matrices, word, (int) x, (int) y, stroke.getFGColor().getAsInt(), shadow.get());
		});
	}

	@Override
	public void render(GuiGraphics graphics, float delta) {
		graphics.getMatrices().push();
		scale(graphics);
		renderComponent(graphics, delta);
		graphics.getMatrices().pop();
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		if (keystrokes == null) {
			setKeystrokes();
		}
		for (Keystroke stroke : keystrokes) {
			stroke.render(graphics);
		}
		if (mouseMovement.get()) {
			int spaceY = 62 + getRawY();
			int spaceX = getRawX();
			if (background.get()) {
				DrawUtil.fillRect(graphics, spaceX, spaceY, width, 35, backgroundColor.get().getAsInt());
			}
			if (outline.get()) {
				DrawUtil.outlineRect(graphics, spaceX, spaceY, width, 35, outlineColor.get().getAsInt());
			}

			float calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * delta)) - 5;
			float calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * delta)) - 5;

			graphics.drawTexture(mouseMovementIndicatorInner.getTexture(),
				spaceX + (width / 2) - 7 / 2 - 1, spaceY + 17 - (7 / 2), 0, 0, 7, 7, 7, 7);

			graphics.getMatrices().translate(calculatedMouseX, calculatedMouseY, 0); // Woah KodeToad, good use of translate

			graphics.drawTexture(mouseMovementIndicatorOuter.getTexture(),
				spaceX + (width / 2) - 1, spaceY + 17, 0, 0, 11, 11, 11, 11);
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		renderComponent(graphics, delta);
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

		void render(Keystroke stroke, GuiGraphics graphics);
	}

	public class Keystroke {

		protected final KeyBind key;
		protected final KeystrokeRenderer render;
		protected final Rectangle bounds;
		private final int animTime = 100;
		protected DrawPosition offset;
		private float start = -1;
		private boolean wasPressed = false;

		public Keystroke(Rectangle bounds, DrawPosition offset, KeyBind key, KeystrokeRenderer render) {
			this.bounds = bounds;
			this.offset = offset;
			this.key = key;
			this.render = render;
		}

		public Color getFGColor() {
			return key.isPressed() ? Color.blend(textColor.get(), pressedTextColor.get(), getPercentPressed())
				: Color.blend(pressedTextColor.get(), textColor.get(), getPercentPressed());
		}

		private float getPercentPressed() {
			return start == -1 ? 1 : MathHelper.clamp((Util.getMeasuringTimeMs() - start) / animTime, 0, 1);
		}

		public void render(GuiGraphics matrices) {
			renderStroke(matrices);
			render.render(this, matrices);
		}

		public void renderStroke(GuiGraphics matrices) {
			if (key.isPressed() != wasPressed) {
				start = Util.getMeasuringTimeMs();
			}
			Rectangle rect = bounds.offset(offset);
			if (background.get()) {
				fillRect(matrices, rect, getColor());
			}
			if (outline.get()) {
				outlineRect(matrices, rect, getOutlineColor());
			}
			if ((Util.getMeasuringTimeMs() - start) / animTime >= 1) {
				start = -1;
			}
			wasPressed = key.isPressed();
		}

		public Color getColor() {
			return key.isPressed()
				? Color.blend(backgroundColor.get(), pressedBackgroundColor.get(), getPercentPressed())
				: Color.blend(pressedBackgroundColor.get(), backgroundColor.get(), getPercentPressed());
		}

		public Color getOutlineColor() {
			return key.isPressed() ? Color.blend(outlineColor.get(), pressedOutlineColor.get(), getPercentPressed())
				: Color.blend(pressedOutlineColor.get(), outlineColor.get(), getPercentPressed());
		}
	}
}
