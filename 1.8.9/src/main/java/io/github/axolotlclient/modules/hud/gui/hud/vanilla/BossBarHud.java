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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.util.ClientColors;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.entity.living.mob.hostile.boss.BossBar;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class BossBarHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "bossbarhud");
	private static final Identifier BARS_TEXTURE = new Identifier("textures/gui/icons.png");
	private final CustomBossBar placeholder = new CustomBossBar("Boss bar", ClientColors.WHITE);

	private final BooleanOption text = new BooleanOption("text", true);
	private final BooleanOption bar = new BooleanOption("bar", true);
	// TODO custom color
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(AnchorPoint.TOP_MIDDLE);

	public BossBarHud() {
		super(184, 24, false);
	}

	@Override
	public void renderComponent(float delta) {
		GlStateManager.enableAlphaTest();
		DrawPosition pos = getPos();
		if (BossBar.name != null && BossBar.timer > 0) {
			client.getTextureManager().bind(BARS_TEXTURE);
			--BossBar.timer;
			if (bar.get()) {
				//GlStateManager.color4f(barColor.get().getRed(), barColor.get().getGreen(), barColor.get().getBlue(), barColor.get().getAlpha());
				drawTexture(pos.x, pos.y + 12, 0, 74, 182, 5);
				drawTexture(pos.x, pos.y + 12, 0, 74, 182, 5);
				if (BossBar.health * 183F > 0) {
					//GlStateManager.color4f(barColor.get().getRed(), barColor.get().getGreen(), barColor.get().getBlue(), barColor.get().getAlpha());
					drawTexture(pos.x, pos.y + 12, 0, 79, (int) (BossBar.health * 183F), 5);
				}
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (text.get()) {
				String string = BossBar.name;
				client.textRenderer.draw(string,
					(float) ((pos.x + width / 2) - client.textRenderer.getWidth(BossBar.name) / 2),
					(float) (pos.y + 2), textColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		placeholder.render(pos.x, pos.y + 14);
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(text);
		options.add(bar);
		options.add(anchor);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	@RequiredArgsConstructor
	public class CustomBossBar extends GuiElement {

		private final String name;
		private final Color barColor;

		public void render(int x, int y) {
			GlStateManager.enableTexture();
			if (bar.get()) {
				Minecraft.getInstance().getTextureManager().bind(BARS_TEXTURE);
				GlStateManager.color4f(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), barColor.getAlpha());
				this.drawTexture(x + 1, y, 0, 79, width, 5);
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (text.get()) {
				client.textRenderer.draw(name, (float) ((x + width / 2) - client.textRenderer.getWidth(name) / 2),
					(float) (y - 10), textColor.get().toInt(), shadow.get());
			}
		}
	}
}
