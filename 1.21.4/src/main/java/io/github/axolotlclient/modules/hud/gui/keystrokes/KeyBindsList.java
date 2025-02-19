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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
	final KeystrokesScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokesScreen keyBindsScreen) {
		super(Minecraft.getInstance(), keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), 24);
		this.keyBindsScreen = keyBindsScreen;

		reload();
	}

	public void reload() {
		clearEntries();
		for (KeystrokeHud.Keystroke keyMapping : keyBindsScreen.hud.keystrokes) {

			Component component = Component.translatable(keyMapping.getKey().getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}

		addEntry(new SpacerEntry());
		addEntry(new NewEntry());
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

	}

	public static class SpacerEntry extends Entry {

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			return null;
		}
	}

	private static final Component CONFIGURE_BUTTON_TITLE = Component.translatable("keystrokes.stroke.configure");

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final Component REMOVE_BUTTON_TITLE = Component.translatable("keystrokes.stroke.remove");
		private final KeystrokeHud.Keystroke key;
		private Component name;
		private final Button configureButton, removeButton;

		KeyEntry(final KeystrokeHud.Keystroke key, final Component name) {
			this.key = key;
			this.name = key.getKey().getTranslatedKeyMessage();
			this.configureButton = Button.builder(CONFIGURE_BUTTON_TITLE, button -> minecraft.setScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, false)))
				.bounds(0, 0, 75, 20)
				.build();
			this.removeButton = Button.builder(REMOVE_BUTTON_TITLE, b -> {
					removeEntry(this);
					keyBindsScreen.removeKey(key);
				}).bounds(0, 0, 50, 20)
				.build();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.scrollBarX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.setPosition(i, j);
			this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.setPosition(k, j);
			this.configureButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.pose().pushPose();
			var rect = key.getRenderPosition();
			float scale = Math.min((float) height / rect.height(), (float) 100 / rect.width());
			guiGraphics.pose().translate(left, top, 0);
			guiGraphics.pose().scale(scale, scale, 1);
			guiGraphics.pose().translate(-rect.x(), -rect.y(), 0);
			key.render(guiGraphics);
			guiGraphics.pose().popPose();
			guiGraphics.drawString(minecraft.font, name, left + width / 2 - minecraft.font.width(name) / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.configureButton, removeButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.configureButton, removeButton);
		}
	}

	public class NewEntry extends Entry {

		private final Button addButton, addSpecialButton;
		private final KeystrokeHud.Keystroke key = keyBindsScreen.hud.newStroke();

		public NewEntry() {
			this.addButton = Button.builder(Component.translatable("keystrokes.stroke.add"), button -> minecraft.setScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, true)))
				.bounds(0, 0, 150, 20)
				.build();
			this.addSpecialButton = Button.builder(Component.translatable("keystrokes.stroke.add.special"),
					button -> minecraft.setScreen(new AddSpecialKeystrokeScreen(keyBindsScreen, keyBindsScreen.hud)))
				.width(150).build();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(addSpecialButton, addButton);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.scrollBarX() - width / 2 - 10 + 4;
			int j = top - 2;
			this.addButton.setPosition(i, j);
			this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - addButton.getWidth() - 8;
			this.addSpecialButton.setPosition(k, j);
			this.addSpecialButton.render(guiGraphics, mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(addSpecialButton, addButton);
		}
	}
}
