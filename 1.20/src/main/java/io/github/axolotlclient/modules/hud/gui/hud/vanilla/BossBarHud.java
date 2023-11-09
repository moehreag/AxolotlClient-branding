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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.mixin.BossBarHudAccessor;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class BossBarHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "bossbarhud");
	private static final Identifier BARS_TEXTURE = new Identifier("textures/gui/bars.png");
	private final BossBar placeholder = new CustomBossBar(Text.literal("Boss bar"), BossBar.Color.WHITE,
		BossBar.Style.PROGRESS);
	private final BossBar placeholder2 = Util.make(() -> {
		BossBar boss = new CustomBossBar(Text.literal("More boss bars..."), BossBar.Color.PURPLE,
			BossBar.Style.PROGRESS);
		boss.setPercent(0.45F);
		return boss;
	});
	private final BooleanOption text = new BooleanOption("text", true);
	private final BooleanOption bar = new BooleanOption("bar", true);
	// TODO custom color
	private final EnumOption anchor = DefaultOptions.getAnchorPoint();
	private Map<UUID, ClientBossBar> bossBars = new HashMap<>();

	public BossBarHud() {
		super(184, 80, false);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		setBossBars();
		if (bossBars == null || this.bossBars.isEmpty()) {
			return;
		}
		DrawPosition scaledPos = getPos();
		int by = 12;
		for (ClientBossBar bossBar : bossBars.values()) {
			renderBossBar(graphics, scaledPos.x(), by + scaledPos.y(), bossBar);
			by = by + 19;
			if (by > getHeight()) {
				break;
			}
		}
	}

	public void setBossBars() {
		int prevLength = bossBars.size();
		bossBars = ((BossBarHudAccessor) client.inGameHud.getBossBarHud()).axolotlclient$getBossBars();
		if (bossBars != null && bossBars.size() != prevLength) {
			if (bossBars.size() == 0) {
				// Just leave it alone, it's not rendering anyway
				return;
			}
			// Update height
			setHeight(12 + prevLength * 19);
		}
	}

	private void renderBossBar(GuiGraphics graphics, int x, int y, BossBar bossBar) {
		if (bar.get()) {
			graphics.drawTexture(BARS_TEXTURE, x, y, 0, bossBar.getColor().ordinal() * 5 * 2, 182, 5, 256, 256);
			if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
				graphics.drawTexture(BARS_TEXTURE, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2, 182, 5,
					256, 256);
			}

			int i = (int) (bossBar.getPercent() * 183.0F);
			if (i > 0) {
				graphics.drawTexture(BARS_TEXTURE, x, y, 0, bossBar.getColor().ordinal() * 5 * 2 + 5, i, 5, 256, 256);
				if (bossBar.getStyle() != BossBar.Style.PROGRESS) {
					graphics.drawTexture(BARS_TEXTURE, x, y, 0, 80 + (bossBar.getStyle().ordinal() - 1) * 5 * 2 + 5,
						i, 5, 256, 256);
				}
			}
		}
		if (text.get()) {
			Text text = bossBar.getName();
			float textX = x + ((float) getWidth() / 2) - ((float) client.textRenderer.getWidth(text) / 2);
			float textY = y - 9;
			graphics.drawText(client.textRenderer, text, (int) textX, (int) textY, textColor.get().getAsInt(), shadow.get());
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		renderBossBar(graphics, pos.x(), pos.y() + 12, placeholder);
		renderBossBar(graphics, pos.x(), pos.y() + 31, placeholder2);
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
		return AnchorPoint.valueOf(anchor.get());
	}

	public static class CustomBossBar extends BossBar {

		public CustomBossBar(Text name, Color color, Style style) {
			super(MathHelper.randomUuid(), name, color, style);
		}
	}
}
