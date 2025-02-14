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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.mixin.KeyBindAccessor;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.keystrokes.KeystrokePositioningScreen;
import io.github.axolotlclient.modules.hud.gui.keystrokes.KeystrokesScreen;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
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

	private static final Path KEYSTROKE_SAVE_FILE = AxolotlClient.resolveConfigFile("keystrokes.json");
	public static final Identifier ID = Identifier.of("kronhud", "keystrokehud");

	private final ColorOption pressedTextColor = new ColorOption("heldtextcolor", new Color(0xFF000000));
	private final ColorOption pressedBackgroundColor = new ColorOption("heldbackgroundcolor", new Color(0x64FFFFFF));
	private final ColorOption pressedOutlineColor = new ColorOption("heldoutlinecolor", ClientColors.BLACK);

	private final GenericOption keystrokesOption = new GenericOption("keystrokes", "keystrokes.configure", () -> client.setScreen(new KeystrokesScreen(KeystrokeHud.this, client.currentScreen)));
	private final GenericOption configurePositions = new GenericOption("keystrokes.positions", "keystrokes.positions.configure",
		() -> client.setScreen(new KeystrokePositioningScreen(client.currentScreen, this)));
	public ArrayList<Keystroke> keystrokes;


	public KeystrokeHud() {
		super(53, 61, true);
		Events.KEYBIND_CHANGE.register(key -> {
			if (MinecraftClient.getInstance().getWindow() != null) {
				KeyBind.resetAll();
				KeyBind.updatePressedStates();
			}
		});
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

	public void setDefaultKeystrokes() {
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
		keystrokes.add(new CustomRenderKeystroke(SpecialKeystroke.SPACE));
	}

	public void setKeystrokes() {
		if (client.getWindow() == null) {
			keystrokes = null;
			return;
			// Wait until render is called
		}
		keystrokes = new ArrayList<>();
		setDefaultKeystrokes();
		loadKeystrokes();
		KeyBind.resetAll();
		KeyBind.updatePressedStates();
	}

	public Keystroke createFromKey(Rectangle bounds, DrawPosition offset, KeyBind key) {
		String name = getMouseKeyBindName(key).orElse(key.getKeyName().getString().toUpperCase());
		if (name.length() > 4) {
			name = name.substring(0, 2);
		}
		return createFromString(bounds, offset, key, name);
	}

	public Keystroke createFromString(Rectangle bounds, DrawPosition offset, KeyBind key, String word) {
		return new LabelKeystroke(bounds, offset, key, word);
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
		options.add(textColor);
		options.add(pressedTextColor);
		options.add(shadow);
		options.add(background);
		options.add(backgroundColor);
		options.add(pressedBackgroundColor);
		options.add(outline);
		options.add(outlineColor);
		options.add(pressedOutlineColor);
		options.add(keystrokesOption);
		options.add(configurePositions);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public interface KeystrokeRenderer {

		void render(Keystroke stroke, GuiGraphics graphics);
	}

	public abstract class Keystroke {

		@Getter
		@Setter
		protected KeyBind key;
		protected KeystrokeRenderer render;
		@Getter
		protected final Rectangle bounds;
		private final int animTime = 100;
		protected DrawPosition offset;
		private long start = -1;
		private boolean wasPressed = false;

		public Keystroke(Rectangle bounds, DrawPosition offset, KeyBind key, KeystrokeRenderer render) {
			this.bounds = bounds;
			this.offset = offset;
			this.key = key;
			this.render = render;
		}

		public void setX(int x) {
			bounds.x(x - offset.x());
		}

		public void setY(int y) {
			bounds.y(y - offset.y());
		}

		public Rectangle getRenderPosition() {
			return bounds.offset(offset);
		}

		public Color getFGColor() {
			return isKeyDown() ? ClientColors.blend(textColor.get(), pressedTextColor.get(), getPercentPressed())
				: ClientColors.blend(pressedTextColor.get(), textColor.get(), getPercentPressed());
		}

		private float getPercentPressed() {
			return start == -1 ? 1 : MathHelper.clamp((float) (Util.getMeasuringTimeMs() - start) / animTime, 0, 1);
		}

		public void render(GuiGraphics matrices) {
			renderStroke(matrices);
			render.render(this, matrices);
		}

		public void renderStroke(GuiGraphics matrices) {
			if (isKeyDown() != wasPressed) {
				start = Util.getMeasuringTimeMs();
			}
			Rectangle rect = getRenderPosition();
			if (background.get()) {
				fillRect(matrices, rect, getColor());
			}
			if (outline.get()) {
				outlineRect(matrices, rect, getOutlineColor());
			}
			if ((float) (Util.getMeasuringTimeMs() - start) / animTime >= 1) {
				start = -1;
			}
			wasPressed = isKeyDown();
		}

		private boolean isKeyDown() {
			return key != null && key.isPressed();
		}

		public Color getColor() {
			return isKeyDown()
				? ClientColors.blend(backgroundColor.get(), pressedBackgroundColor.get(), getPercentPressed())
				: ClientColors.blend(pressedBackgroundColor.get(), backgroundColor.get(), getPercentPressed());
		}

		public Color getOutlineColor() {
			return isKeyDown() ? ClientColors.blend(outlineColor.get(), pressedOutlineColor.get(), getPercentPressed())
				: ClientColors.blend(pressedOutlineColor.get(), outlineColor.get(), getPercentPressed());
		}

		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>();
			map.put("key", key.getKeyTranslationKey());
			map.put("bounds", Map.of("x", bounds.x(), "y", bounds.y(), "width", bounds.width(), "height", bounds.height()));
			return map;
		}

		public abstract String getLabel();

		public abstract void setLabel(String label);

		public abstract boolean isLabelEditable();
	}

	@SuppressWarnings("unchecked")
	private Keystroke deserializeKey(Map<String, ?> json) {
		if ("option".equals(json.get("type"))) {
			KeyBind key = KeyBindAccessor.getAllKeyBinds().get((String) json.get("option"));
			if (json.containsKey("editable_label")) {
				String label = (String) json.get("label");
				return new LabelKeystroke(getRectangle((Map<String, ?>) json.get("bounds")), getPos(), key,
					label);
			} else {
				return new CustomRenderKeystroke(SpecialKeystroke.valueOf((String) json.get("special_name")), getRectangle((Map<String, ?>) json.get("bounds")), getPos(), key);
			}
		} else {
			var key = KeyBindAccessor.getAllKeyBinds().get((String) json.get("key_name"));
			return new LabelKeystroke(getRectangle((Map<String, ?>) json.get("bounds")), getPos(), key, (String) json.get("label"), (boolean) json.get("synchronize_label"));
		}
	}

	private static Rectangle getRectangle(Map<String, ?> json) {
		return new Rectangle((int) (long) json.get("x"), (int) (long) json.get("y"), (int) (long) json.get("width"), (int) (long) json.get("height"));
	}

	public class CustomRenderKeystroke extends Keystroke {

		private static final Supplier<String> label = () -> Formatting.ITALIC + I18n.translate("keystrokes.stroke.custom_renderer");

		private final SpecialKeystroke parent;

		public CustomRenderKeystroke(SpecialKeystroke stroke, Rectangle bounds, DrawPosition offset, KeyBind key) {
			super(bounds, offset, key, (s, g) -> stroke.getRenderer().render(KeystrokeHud.this, s, g));
			this.parent = stroke;
		}

		public CustomRenderKeystroke(SpecialKeystroke stroke) {
			this(stroke, stroke.getRect().copy(), KeystrokeHud.this.getPos(), stroke.getKey());
		}

		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> json = super.serialize();
			json.put("type", "option");
			json.put("option", key.getTranslationKey());
			json.put("special_name", parent.name());
			return json;
		}

		@Override
		public String getLabel() {
			return label.get();
		}

		@Override
		public void setLabel(String label) {

		}

		@Override
		public boolean isLabelEditable() {
			return false;
		}
	}

	public Keystroke newSpecialStroke(SpecialKeystroke stroke) {
		return new CustomRenderKeystroke(stroke);
	}

	public LabelKeystroke newStroke() {
		return new LabelKeystroke(new Rectangle(0, 0, 17, 17), getPos(), null, "", false);
	}

	@Setter
	public class LabelKeystroke extends Keystroke {

		private String label;
		@Getter
		private boolean synchronizeLabel;

		public LabelKeystroke(Rectangle bounds, DrawPosition offset, KeyBind key, String label) {
			this(bounds, offset, key, label, true);
		}

		public LabelKeystroke(Rectangle bounds, DrawPosition offset, KeyBind key, String label, boolean synchronizeLabel) {
			super(bounds, offset, key, (stroke, matrices) -> {
			});
			this.label = label;
			this.render = (stroke, matrices) -> {
				Rectangle strokeBounds = stroke.bounds;
				float x = (strokeBounds.x() + stroke.offset.x() + ((float) strokeBounds.width() / 2))
					- ((float) client.textRenderer.getWidth(getLabel()) / 2);
				float y = strokeBounds.y() + stroke.offset.y() + ((float) strokeBounds.height() / 2) - 4;

				drawString(matrices, getLabel(), (int) x, (int) y, stroke.getFGColor().toInt(), shadow.get());
			};
			this.synchronizeLabel = synchronizeLabel;
		}

		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> json = super.serialize();
			json.put("type", "custom");
			json.put("key_name", key.getTranslationKey());
			json.put("label", label);
			json.put("synchronize_label", synchronizeLabel);
			return json;
		}

		public void setSynchronizeLabel(boolean synchronizeLabel) {
			if (synchronizeLabel) {
				String name = getMouseKeyBindName(key).orElse(key.getKeyName().getString().toUpperCase());
				if (name.length() > 4) {
					name = name.substring(0, 2);
				}
				this.label = name;
			}
			this.synchronizeLabel = synchronizeLabel;
		}

		@Override
		public void setKey(KeyBind key) {
			if (synchronizeLabel) {
				String name = getMouseKeyBindName(key).orElse(key.getKeyName().getString().toUpperCase());
				if (name.length() > 4) {
					name = name.substring(0, 2);
				}
				this.label = name;
			}
			super.setKey(key);
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public boolean isLabelEditable() {
			return true;
		}
	}

	public void saveKeystrokes() {
		try {
			Files.createDirectories(KEYSTROKE_SAVE_FILE.getParent());
			Files.writeString(KEYSTROKE_SAVE_FILE, GsonHelper.GSON.toJson(keystrokes.stream().map(Keystroke::serialize).toList()));
		} catch (Exception e) {
			AxolotlClient.LOGGER.warn("Failed to save keystroke configuration!", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadKeystrokes() {
		try {
			if (Files.exists(KEYSTROKE_SAVE_FILE)) {
				List<?> entries = (List<?>) GsonHelper.read(Files.readString(KEYSTROKE_SAVE_FILE));
				var loaded = entries.stream().map(e -> (Map<String, Object>) e)
					.map(KeystrokeHud.this::deserializeKey)
					.toList();
				keystrokes.clear();
				keystrokes.addAll(loaded);
			} else {
				saveKeystrokes();
			}
		} catch (Exception e) {
			AxolotlClient.LOGGER.warn("Failed to load keystroke configuration, using defaults!", e);
		}
	}

	@AllArgsConstructor
	@Getter
	public enum SpecialKeystroke {
		SPACE(new Rectangle(0, 54, 53, 7), MinecraftClient.getInstance().options.jumpKey, (hud, stroke, matrices) -> {
			Rectangle bounds = stroke.bounds;
			Rectangle spaceBounds = new Rectangle(bounds.x() + stroke.offset.x() + 4,
				bounds.y() + stroke.offset.y() + bounds.height() / 2 - 1, bounds.width() - 8, 1);
			fillRect(matrices, spaceBounds, stroke.getFGColor());
			if (hud.shadow.get()) {
				fillRect(matrices, spaceBounds.offset(1, 1), new Color(
					(stroke.getFGColor().toInt() & 16579836) >> 2 | stroke.getFGColor().toInt() & -16777216));
			}
		});

		private final Rectangle rect;
		private final KeyBind key;
		private final SpecialKeystrokeRenderer renderer;

		public interface SpecialKeystrokeRenderer {
			void render(KeystrokeHud hud, Keystroke stroke, GuiGraphics graphics);
		}
	}
}
