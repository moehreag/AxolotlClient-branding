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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.mixin.BossBarHudAccessor;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class BossBarHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "bossbarhud");
	private static final ResourceLocation[] BAR_BACKGROUND_SPRITES =
		new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/pink_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/blue_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/red_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/green_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/yellow_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/purple_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/white_background")};
	private static final ResourceLocation[] BAR_PROGRESS_SPRITES =
		new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/pink_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/blue_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/red_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/green_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/yellow_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/purple_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/white_progress")};
	private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES =
		new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/notched_6_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_10_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_12_background"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_20_background")};
	private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES =
		new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/notched_6_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_10_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_12_progress"),
			ResourceLocation.withDefaultNamespace("boss_bar/notched_20_progress")};
	private final BossEvent placeholder = new CustomBossBar(Component.literal("Boss bar"), BossEvent.BossBarColor.WHITE,
															BossEvent.BossBarOverlay.PROGRESS
	);
	private final BossEvent placeholder2 = Util.make(() -> {
		BossEvent boss = new CustomBossBar(Component.literal("More boss bars..."), BossEvent.BossBarColor.PURPLE,
										   BossEvent.BossBarOverlay.PROGRESS
		);
		boss.setProgress(0.45F);
		return boss;
	});
	private final BooleanOption text = new BooleanOption("text", true);
	private final BooleanOption bar = new BooleanOption("bar", true);
	// TODO custom color
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint();
	private Map<UUID, LerpingBossEvent> bossBars = new HashMap<>();

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
		for (LerpingBossEvent bossBar : bossBars.values()) {
			renderBossBar(graphics, scaledPos.x(), by + scaledPos.y(), bossBar);
			by = by + 19;
			if (by > getHeight()) {
				break;
			}
		}
	}

	public void setBossBars() {
		int prevLength = bossBars.size();
		bossBars = ((BossBarHudAccessor) client.gui.getBossOverlay()).axolotlclient$getBossBars();
		if (bossBars != null && bossBars.size() != prevLength) {
			if (bossBars.size() == 0) {
				// Just leave it alone, it's not rendering anyway
				return;
			}
			// Update height
			setHeight(12 + prevLength * 19);
		}
	}

	private void renderBossBar(GuiGraphics graphics, int x, int y, BossEvent bossBar) {
		if (bar.get()) {
			this.drawBar(graphics, x, y, bossBar, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
			int i = Mth.lerpDiscrete(bossBar.getProgress(), 0, 182);
			if (i > 0) {
				this.drawBar(graphics, x, y, bossBar, i, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
			}
		}
		if (text.get()) {
			Component text = bossBar.getName();
			float textX = x + ((float) getWidth() / 2) - ((float) client.font.width(text) / 2);
			float textY = y - 9;
			graphics.drawString(client.font, text, (int) textX, (int) textY, textColor.get().toInt(), shadow.get());
		}
	}

	private void drawBar(GuiGraphics graphics, int x, int y, BossEvent bar, int width, ResourceLocation[] textures, ResourceLocation[] alternativeTextures) {
		graphics.blitSprite(RenderType::guiTextured, textures[bar.getColor().ordinal()], 182, 5, 0, 0, x, y, width, 5);
		if (bar.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
			graphics.blitSprite(RenderType::guiTextured, alternativeTextures[bar.getOverlay().ordinal() - 1], 182, 5, 0,
								0, x, y, width, 5
							   );
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		renderBossBar(graphics, pos.x(), pos.y() + 12, placeholder);
		renderBossBar(graphics, pos.x(), pos.y() + 31, placeholder2);
	}

	@Override
	public ResourceLocation getId() {
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
		return (anchor.get());
	}

	public static class CustomBossBar extends BossEvent {

		public CustomBossBar(Component name, BossBarColor color, BossBarOverlay style) {
			super(Mth.createInsecureUUID(), name, color, style);
		}
	}
}
