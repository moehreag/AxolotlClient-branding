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
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class SpecialKeystrokeSelectionList extends ContainerObjectSelectionList<SpecialKeystrokeSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final AddSpecialKeystrokeScreen keyBindsScreen;
	private int maxNameWidth;

	public SpecialKeystrokeSelectionList(AddSpecialKeystrokeScreen keyBindsScreen, Minecraft minecraft) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		KeystrokeHud.SpecialKeystroke[] strokes = ArrayUtils.clone(KeystrokeHud.SpecialKeystroke.values());
		Arrays.sort(strokes);

		for (KeystrokeHud.SpecialKeystroke keyMapping : strokes) {
			Component component = Component.translatable(keyMapping.getKey().getName());
			int i = minecraft.font.width(component);
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
	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private final Component name, boundKey;
		private final Button addButton;
		private final KeystrokeHud.Keystroke keystroke;

		KeyEntry(final KeystrokeHud.SpecialKeystroke key, final Component name) {
			this.name = name;
			this.keystroke = keyBindsScreen.hud.newSpecialStroke(key);
			this.boundKey = key.getKey().getTranslatedKeyMessage();
			this.addButton = Button.builder(Component.translatable("keystrokes.stroke.add"), button -> keyBindsScreen.hud.keystrokes.add(keystroke))
				.bounds(0, 0, 75, 20)
				.build();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = SpecialKeystrokeSelectionList.this.scrollBarX() - 10;
			int j = top - 2;
			int k = i - 5 - this.addButton.getWidth();
			this.addButton.setPosition(k, j);
			this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.pose().pushPose();
			var rect = keystroke.getRenderPosition();
			float scale = Math.min( (float) height / rect.height(), (float) 100 / rect.width());
			guiGraphics.pose().translate(left, top, 0);
			guiGraphics.pose().scale(scale, scale, 1);
			guiGraphics.pose().translate(-rect.x(), -rect.y(), 0);
			keystroke.render(guiGraphics);
			guiGraphics.pose().popPose();
			guiGraphics.drawString(minecraft.font, boundKey, left+110+(k-left-110)/3, top+height/2 - 9/2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.addButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.addButton);
		}
	}
}
