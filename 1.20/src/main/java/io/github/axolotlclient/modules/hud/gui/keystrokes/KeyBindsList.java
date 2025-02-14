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
import net.minecraft.client.gui.ElementPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationEvent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ElementListWidget<KeyBindsList.Entry> {
	final KeystrokesScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokesScreen keyBindsScreen) {
		super(MinecraftClient.getInstance(), keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height-33, 24);
		this.keyBindsScreen = keyBindsScreen;

		reload();
	}

	public void reload() {
		clearEntries();
		for (KeystrokeHud.Keystroke keyMapping : keyBindsScreen.hud.keystrokes) {

			Text component = Text.translatable(keyMapping.getKey().getTranslationKey());
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
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public @Nullable ElementPath nextFocusPath(GuiNavigationEvent event) {
			return null;
		}
	}

	private static final Text CONFIGURE_BUTTON_TITLE = Text.translatable("keystrokes.stroke.configure");

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final Text REMOVE_BUTTON_TITLE = Text.translatable("keystrokes.stroke.remove");
		private final KeystrokeHud.Keystroke key;
		private Text name;
		private final ButtonWidget configureButton, removeButton;

		KeyEntry(final KeystrokeHud.Keystroke key, final Text name) {
			this.key = key;
			this.name = key.getKey().getKeyName();
			this.configureButton = ButtonWidget.builder(CONFIGURE_BUTTON_TITLE, button -> client.setScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, false)))
				.positionAndSize(0, 0, 75, 20)
				.build();
			this.removeButton = ButtonWidget.builder(REMOVE_BUTTON_TITLE, b -> {
					removeEntry(this);
					keyBindsScreen.removeKey(key);
				}).positionAndSize(0, 0, 50, 20)
				.build();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.setPosition(i, j);
			this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.setPosition(k, j);
			this.configureButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.getMatrices().push();
			var rect = key.getRenderPosition();
			float scale = Math.min((float) height / rect.height(), (float) 100 / rect.width());
			guiGraphics.getMatrices().translate(left, top, 0);
			guiGraphics.getMatrices().scale(scale, scale, 1);
			guiGraphics.getMatrices().translate(-rect.x(), -rect.y(), 0);
			key.render(guiGraphics);
			guiGraphics.getMatrices().pop();
			guiGraphics.drawShadowedText(client.textRenderer, name, left + width / 2 - client.textRenderer.getWidth(name) / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.configureButton, removeButton);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(this.configureButton, removeButton);
		}
	}

	public class NewEntry extends Entry {

		private final ButtonWidget addButton, addSpecialButton;
		private final KeystrokeHud.Keystroke key = keyBindsScreen.hud.newStroke();

		public NewEntry() {
			this.addButton = ButtonWidget.builder(Text.translatable("keystrokes.stroke.add"), button -> client.setScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, true)))
				.positionAndSize(0, 0, 150, 20)
				.build();
			this.addSpecialButton = ButtonWidget.builder(Text.translatable("keystrokes.stroke.add.special"),
					button -> client.setScreen(new AddSpecialKeystrokeScreen(keyBindsScreen, keyBindsScreen.hud)))
				.width(150).build();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(addSpecialButton, addButton);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - width / 2 - 10 + 4;
			int j = top - 2;
			this.addButton.setPosition(i, j);
			this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - addButton.getWidth() - 8;
			this.addSpecialButton.setPosition(k, j);
			this.addSpecialButton.render(guiGraphics, mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(addSpecialButton, addButton);
		}
	}
}
