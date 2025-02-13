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

package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ElementListWidget<KeyBindsList.Entry> {
	final KeystrokesScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokesScreen keyBindsScreen, List<KeystrokeHud.Keystroke> keys) {
		super(MinecraftClient.getInstance(), keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height-33, 24);
		this.keyBindsScreen = keyBindsScreen;

		reload(keys);
	}

	public void reload(List<KeystrokeHud.Keystroke> keys) {
		clearEntries();
		for (KeystrokeHud.Keystroke keyMapping : keys) {

			Text component = new TranslatableText(keyMapping.getKey().getTranslationKey());
			int i = client.textRenderer.getWidth(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}

		addEntry(new SpacerEntry());
		addEntry(new NewEntry());
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft()+getRowWidth()+10;
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	public static class SpacerEntry extends Entry {
		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}

	private static final Text CONFIGURE_BUTTON_TITLE = new TranslatableText("keystrokes.stroke.configure");

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final Text REMOVE_BUTTON_TITLE = new TranslatableText("keystrokes.stroke.remove");
		private final KeystrokeHud.Keystroke key;
		private Text name;
		private final ButtonWidget configureButton, removeButton;

		KeyEntry(final KeystrokeHud.Keystroke key, final Text name) {
			this.key = key;
			this.name = key.getKey().getBoundKeyLocalizedText();
			this.configureButton = new ButtonWidget(0, 0, 75, 20, CONFIGURE_BUTTON_TITLE, button -> client.openScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, false)));
			this.removeButton = new ButtonWidget(0, 0, 50, 20, REMOVE_BUTTON_TITLE, b -> {
					removeEntry(this);
					keyBindsScreen.removeKey(key);
				});
		}

		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.x = i;
			this.removeButton.y = j;
			this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.x = k;
			this.configureButton.y = j;
			this.configureButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.push();
			var rect = key.getRenderPosition();
			float scale = Math.min((float) height / rect.height(), (float) 100 / rect.width());
			guiGraphics.translate(left, top, 0);
			guiGraphics.scale(scale, scale, 1);
			guiGraphics.translate(-rect.x(), -rect.y(), 0);
			key.render(guiGraphics);
			guiGraphics.pop();
			drawTextWithShadow(guiGraphics, client.textRenderer, name, left + width / 2 - client.textRenderer.getWidth(name) / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.configureButton, removeButton);
		}
	}

	public class NewEntry extends Entry {

		private final ButtonWidget addButton, addSpecialButton;
		private final KeystrokeHud.Keystroke key = keyBindsScreen.hud.newStroke();

		public NewEntry() {
			this.addButton = new ButtonWidget(0, 0, 150, 20, new TranslatableText("keystrokes.stroke.add"), button -> client.openScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, true)));
			this.addSpecialButton = new ButtonWidget(0, 0, 150, 20, new TranslatableText("keystrokes.stroke.add.special"),
					button -> client.openScreen(new AddSpecialKeystrokeScreen(keyBindsScreen, keyBindsScreen.hud)));
		}

		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - width / 2 - 10 + 4;
			int j = top - 2;
			this.addButton.x = i;
			this.addButton.y = j;
			this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - addButton.getWidth() - 8;
			this.addSpecialButton.x = k;
			this.addSpecialButton.y = j;
			this.addSpecialButton.render(guiGraphics, mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(addSpecialButton, addButton);
		}
	}
}
