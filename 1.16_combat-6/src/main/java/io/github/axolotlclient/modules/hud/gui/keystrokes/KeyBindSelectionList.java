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
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class KeyBindSelectionList extends ElementListWidget<KeyBindSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeyBindSelectionScreen keyBindsScreen;
	private final Consumer<KeyBinding> selectionConsumer;
	private int maxNameWidth;

	public KeyBindSelectionList(KeyBindSelectionScreen keyBindsScreen, MinecraftClient minecraft, Consumer<KeyBinding> selectionConsumer) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height - 33, ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		this.selectionConsumer = selectionConsumer;
		KeyBinding[] keyMappings = ArrayUtils.clone(minecraft.options.keysAll);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyBinding keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new CategoryEntry(new TranslatableText(string2)));
			}

			Text component = new TranslatableText(keyMapping.getTranslationKey());
			int i = minecraft.textRenderer.getWidth(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft() + getRowWidth() + 10;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends Entry {
		final Text name;
		private final int width;

		public CategoryEntry(final Text name) {
			this.name = name;
			this.width = KeyBindSelectionList.this.client.textRenderer.getWidth(this.name);
		}

		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			KeyBindSelectionList.this.client.textRenderer.drawWithShadow(guiGraphics, this.name, KeyBindSelectionList.this.width / 2f - this.width / 2f, top + height - 9 - 1, -1);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private final Text name, boundKey;
		private final ButtonWidget changeButton;

		KeyEntry(final KeyBinding key, final Text name) {
			this.name = name;
			this.boundKey = key.getBoundKeyLocalizedText();
			this.changeButton = new ButtonWidget(0, 0, 75, 20, new TranslatableText("keystrokes.key.select"), button -> {
				selectionConsumer.accept(key);
				keyBindsScreen.onClose();
			});
			changeButton.active = !(keyBindsScreen.stroke.getKey() != null && key == keyBindsScreen.stroke.getKey());
		}

		@Override
		public void render(MatrixStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindSelectionList.this.getScrollbarPositionX() - 10;
			int j = top - 2;
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.x = k;
			this.changeButton.y = j;
			this.changeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			client.textRenderer.drawWithShadow(guiGraphics, this.name, left, top + height / 2f - 9 / 2f, -1);
			client.textRenderer.drawWithShadow(guiGraphics, boundKey, left + width / 2f - client.textRenderer.getWidth(boundKey) / 2f, top + height / 2f - 9 / 2f, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.changeButton);
		}
	}
}
