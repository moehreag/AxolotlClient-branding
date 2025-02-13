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
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class SpecialKeystrokeSelectionList extends ElementListWidget<SpecialKeystrokeSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final AddSpecialKeystrokeScreen keyBindsScreen;
	private int maxNameWidth;

	public SpecialKeystrokeSelectionList(AddSpecialKeystrokeScreen keyBindsScreen, Minecraft minecraft) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height - 33, ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		KeystrokeHud.SpecialKeystroke[] strokes = ArrayUtils.clone(KeystrokeHud.SpecialKeystroke.values());
		Arrays.sort(strokes);

		for (KeystrokeHud.SpecialKeystroke keyMapping : strokes) {
			String component = I18n.translate(keyMapping.getKey().getName());
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
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private final String name, boundKey;
		private final ButtonWidget addButton;
		private final KeystrokeHud.Keystroke keystroke;

		KeyEntry(final KeystrokeHud.SpecialKeystroke key, final String name) {
			this.name = name;
			this.keystroke = keyBindsScreen.hud.newSpecialStroke(key);
			this.boundKey = GameOptions.getKeyName(key.getKey().getKeyCode());
			this.addButton = new VanillaButtonWidget(0, 0, 75, 20, I18n.translate("keystrokes.stroke.add"), button -> keyBindsScreen.hud.keystrokes.add(keystroke));
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = SpecialKeystrokeSelectionList.this.getScrollbarPositionX() - 10;
			int j = top - 2;
			int k = i - 5 - this.addButton.getWidth();
			this.addButton.setPosition(k, j);
			this.addButton.render(mouseX, mouseY, partialTick);
			GlStateManager.pushMatrix();
			var rect = keystroke.getRenderPosition();
			float scale = Math.min((float) height / rect.height(), (float) 100 / rect.width());
			GlStateManager.translatef(left, top, 0);
			GlStateManager.scalef(scale, scale, 1);
			GlStateManager.translatef(-rect.x(), -rect.y(), 0);
			keystroke.render();
			GlStateManager.popMatrix();
			client.textRenderer.drawWithShadow(boundKey, left + 110 + (k - left - 110) /3f, top + height / 2f - 9 / 2f, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.addButton);
		}
	}
}
