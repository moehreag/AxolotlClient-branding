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

import java.util.Comparator;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ScoreboardHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "scoreboardhud");
	public static final Objective placeholder = Util.make(() -> {
		Scoreboard placeScore = new Scoreboard();
		Objective objective =
			placeScore.addObjective("placeholder", ObjectiveCriteria.DUMMY, Component.literal("Scoreboard"),
									ObjectiveCriteria.RenderType.INTEGER, false, StyledFormat.SIDEBAR_DEFAULT
								   );
		ScoreAccess dark = placeScore.getOrCreatePlayerScore(ScoreHolder.forNameOnly("DarkKronicle"), objective);
		dark.set(8780);

		ScoreAccess moeh = placeScore.getOrCreatePlayerScore(ScoreHolder.forNameOnly("moehreag"), objective);
		moeh.set(743);

		ScoreAccess kode = placeScore.getOrCreatePlayerScore(ScoreHolder.forNameOnly("TheKodeToad"), objective);
		kode.set(2948);

		placeScore.setDisplayObjective(DisplaySlot.SIDEBAR, objective);
		return objective;
	});

	private final ColorOption backgroundColor = new ColorOption("backgroundcolor", new Color(0x4C000000));
	private final ColorOption topColor = new ColorOption("topbackgroundcolor", new Color(0x66000000));
	private final IntegerOption topPadding = new IntegerOption("toppadding", 0, 0, 4);
	private final BooleanOption scores = new BooleanOption("scores", true);
	private final ColorOption scoreColor = new ColorOption("scorecolor", new Color(0xFFFF5555));
	private final EnumOption<AnchorPoint> anchor =
		new EnumOption<>("anchorpoint", AnchorPoint.class, AnchorPoint.MIDDLE_RIGHT);

	public ScoreboardHud() {
		super(200, 146, true);
	}

	@Override
	public void render(GuiGraphics graphics, float delta) {
		graphics.pose().pushPose();
		scale(graphics);
		renderComponent(graphics, delta);
		graphics.pose().popPose();
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		Scoreboard scoreboard = this.client.level.getScoreboard();
		Objective objective = null;
		PlayerTeam playerTeam = scoreboard.getPlayersTeam(client.player.getScoreboardName());
		if (playerTeam != null) {
			DisplaySlot displaySlot = DisplaySlot.teamColorToSlot(playerTeam.getColor());
			if (displaySlot != null) {
				objective = scoreboard.getDisplayObjective(displaySlot);
			}
		}

		Objective objective2 =
			objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective2 != null) {
			this.displayScoreboardSidebar(graphics, objective2);
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		displayScoreboardSidebar(graphics, placeholder);
	}

	// Abusing this could break some stuff/could allow for unfair advantages. The goal is not to do this, so it won't
	// show any more information than it would have in vanilla.
	private void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective) {
		Font font = client.font;
		Scoreboard scoreboard = objective.getScoreboard();
		NumberFormat numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

		@Environment(EnvType.CLIENT)
		record DisplayEntry(Component name, Component score, int scoreWidth) {
		}

		DisplayEntry[] entries = scoreboard.listPlayerScores(objective)
			.stream()
			.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(PlayerScoreEntry::value)
				.reversed()
				.thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
			.limit(15L)
			.map(playerScoreEntry -> {
				PlayerTeam playerTeam = scoreboard.getPlayersTeam(playerScoreEntry.owner());
				Component componentx = playerScoreEntry.ownerName();
				Component component2 = PlayerTeam.formatNameForTeam(playerTeam, componentx);
				Component component3 = playerScoreEntry.formatValue(numberFormat);
				int ix = font.width(component3);
				return new DisplayEntry(component2, component3, ix);
			})
			.toArray(DisplayEntry[]::new);
		Component title = objective.getDisplayName();
		int titleWidth = font.width(title);
		int maxWidth = titleWidth;
		int textOffset = font.width(": ");

		for (DisplayEntry lv : entries) {
			maxWidth = Math.max(maxWidth, font.width(lv.name) + (lv.scoreWidth > 0 && scores.get() ? textOffset + lv.scoreWidth : 0));
		}

		maxWidth += 3;
		int m = entries.length;
		int mainHeight = m * 9;

		int newHeight = mainHeight + 10 + topPadding.get() * 2;

		boolean updated = false;
		if (newHeight + 1 != height) {
			setHeight(newHeight + 1);
			updated = true;
		}
		if (maxWidth + 1 != width) {
			setWidth(maxWidth + 1);
			updated = true;
		}
		if (updated) {
			onBoundsUpdate();
		}

		Rectangle bounds = getBounds();

		int yEnd = bounds.y() + bounds.height();
		int textX = bounds.x()+3;
		int xEnd = bounds.x() + bounds.width()-1;
		int titleEnd = yEnd - mainHeight;
		if (background.get()) {
			guiGraphics.fill(textX - 2, titleEnd - 9 - 1 - topPadding.get()*2, xEnd, titleEnd - 1, topColor.get().toInt());
			guiGraphics.fill(textX - 2, titleEnd - 1, xEnd, yEnd, backgroundColor.get().toInt());
		}
		guiGraphics.drawString(font, title, textX + maxWidth / 2 - titleWidth / 2, titleEnd - 9 - topPadding.get(), -1, shadow.get());

		for (int v = 0; v < m; v++) {
			DisplayEntry lv2 = entries[v];
			int w = yEnd - (m - v) * 9;
			guiGraphics.drawString(font, lv2.name, textX, w, -1, shadow.get());
			if (scores.get()) {
				guiGraphics.drawString(font, lv2.score, xEnd - lv2.scoreWidth, w, scoreColor.get().toInt(), shadow.get());
			}
		}

		if (outline.get() && outlineColor.get().getAlpha() > 0) {
			DrawUtil.outlineRect(guiGraphics, bounds, outlineColor.get());
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.set(options.indexOf(super.backgroundColor), backgroundColor);
		options.add(topColor);
		options.add(scores);
		options.add(scoreColor);
		options.add(anchor);
		options.add(topPadding);
		options.remove(textColor);
		return options;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return (anchor.get());
	}
}
