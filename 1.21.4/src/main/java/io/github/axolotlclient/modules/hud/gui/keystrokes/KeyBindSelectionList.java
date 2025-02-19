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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class KeyBindSelectionList extends ContainerObjectSelectionList<KeyBindSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeyBindSelectionScreen keyBindsScreen;
	private final Consumer<KeyMapping> selectionConsumer;
	private int maxNameWidth;

	public KeyBindSelectionList(KeyBindSelectionScreen keyBindsScreen, Minecraft minecraft, Consumer<KeyMapping> selectionConsumer) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		this.selectionConsumer = selectionConsumer;
		KeyMapping[] keyMappings = ArrayUtils.clone(minecraft.options.keyMappings);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyMapping keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new KeyBindSelectionList.CategoryEntry(Component.translatable(string2)));
			}

			Component component = Component.translatable(keyMapping.getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyBindSelectionList.KeyEntry(keyMapping, component));
		}
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends KeyBindSelectionList.Entry {
		final Component name;
		private final int width;

		public CategoryEntry(final Component name) {
			this.name = name;
			this.width = KeyBindSelectionList.this.minecraft.font.width(this.name);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			guiGraphics.drawString(KeyBindSelectionList.this.minecraft.font, this.name, KeyBindSelectionList.this.width / 2 - this.width / 2, top + height - 9 - 1, -1);
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			return null;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(new NarratableEntry() {
				@Override
				public NarratableEntry.NarrationPriority narrationPriority() {
					return NarratableEntry.NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput narrationElementOutput) {
					narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindSelectionList.Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends KeyBindSelectionList.Entry {
		private final Component name, boundKey;
		private final Button changeButton;

		KeyEntry(final KeyMapping key, final Component name) {
			this.name = name;
			this.boundKey = key.getTranslatedKeyMessage();
			this.changeButton = Button.builder(Component.translatable("keystrokes.key.select"), button -> {
					selectionConsumer.accept(key);
					keyBindsScreen.onClose();
				})
				.bounds(0, 0, 75, 20)
				.build();
			changeButton.active = !(keyBindsScreen.stroke.getKey() != null && key == keyBindsScreen.stroke.getKey());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindSelectionList.this.scrollBarX() - 10;
			int j = top - 2;
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.setPosition(k, j);
			this.changeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawString(minecraft.font, this.name, left, top + height / 2 - 9 / 2, -1);
			guiGraphics.drawString(minecraft.font, boundKey, left + width / 2 - minecraft.font.width(boundKey) / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.changeButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.changeButton);
		}
	}
}
