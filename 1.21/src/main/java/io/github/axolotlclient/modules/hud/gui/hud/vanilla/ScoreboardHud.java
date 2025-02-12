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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ScoreboardHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = Identifier.of("kronhud", "scoreboardhud");
	public static final ScoreboardObjective placeholder = Util.make(() -> {
		Scoreboard placeScore = new Scoreboard();
		ScoreboardObjective objective = placeScore.addObjective("placeholder", ScoreboardCriterion.DUMMY,
			Text.literal("Scoreboard"), ScoreboardCriterion.RenderType.INTEGER, false, StyledNumberFormat.RED);
		ScoreAccess dark = placeScore.getOrCreateScore(ScoreHolder.of("DarkKronicle"), objective);
		dark.setScore(8780);

		ScoreAccess moeh = placeScore.getOrCreateScore(ScoreHolder.of("moehreag"), objective);
		moeh.setScore(743);

		ScoreAccess kode = placeScore.getOrCreateScore(ScoreHolder.of("TheKodeToad"), objective);
		kode.setScore(2948);

		placeScore.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
		return objective;
	});

	private final ColorOption backgroundColor = new ColorOption("backgroundcolor", new Color(0x4C000000));
	private final ColorOption topColor = new ColorOption("topbackgroundcolor", new Color(0x66000000));
	private final IntegerOption topPadding = new IntegerOption("toppadding", 0, 0, 4);
	private final BooleanOption scores = new BooleanOption("scores", true);
	private final ColorOption scoreColor = new ColorOption("scorecolor", new Color(0xFFFF5555));
	private final IntegerOption textAlpha = new IntegerOption("text_alpha", 255, 0, 255);
	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.MIDDLE_RIGHT);

	public ScoreboardHud() {
		super(200, 146, true);
	}

	@Override
	public void render(GuiGraphics graphics, float delta) {
		graphics.getMatrices().push();
		scale(graphics);
		renderComponent(graphics, delta);
		graphics.getMatrices().pop();
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		Scoreboard scoreboard = this.client.world.getScoreboard();
		ScoreboardObjective scoreboardObjective = null;
		Team team = scoreboard.getPlayerTeam(this.client.player.getProfileName());
		if (team != null) {
			int t = team.getColor().getColorIndex();
			if (t >= 0) {
				scoreboardObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BY_ID.apply(3 + t));
			}
		}

		ScoreboardObjective scoreboardObjective2 = scoreboardObjective != null ? scoreboardObjective
			: scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (scoreboardObjective2 != null) {
			this.renderScoreboardSidebar(graphics, scoreboardObjective2);
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		renderScoreboardSidebar(graphics, placeholder);
	}

	// Abusing this could break some stuff/could allow for unfair advantages. The goal is not to do this, so it won't
	// show any more information than it would have in vanilla.
	private void renderScoreboardSidebar(GuiGraphics graphics, ScoreboardObjective objective) {
		TextRenderer font = client.textRenderer;
		Scoreboard scoreboard = objective.getScoreboard();
		NumberFormatOverride numberFormat = objective.getNumberFormatOverrideOrElse(StyledNumberFormat.RED);

		@Environment(EnvType.CLIENT)
		record DisplayEntry(Text name, Text score, int scoreWidth) {
		}

		DisplayEntry[] entries = scoreboard.getEntriesForObjective(objective)
			.stream()
			.filter(entry -> !entry.isHidden())
			.sorted(Comparator.comparing(ScoreboardEntry::value)
				.reversed()
				.thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
			.limit(15L)
			.map(entry -> {
				Team playerTeam = scoreboard.getPlayerTeam(entry.owner());
				Text componentx = entry.getDisplay();
				Text component2 = Team.decorateName(playerTeam, componentx);
				Text component3 = entry.getNumber(numberFormat);
				int ix = font.getWidth(component3);
				return new DisplayEntry(component2, component3, ix);
			})
			.toArray(DisplayEntry[]::new);
		Text title = objective.getDisplayName();
		int titleWidth = font.getWidth(title);
		int maxWidth = titleWidth;
		int textOffset = font.getWidth(": ");

		for (DisplayEntry entry : entries) {
			maxWidth = Math.max(maxWidth, font.getWidth(entry.name) + (entry.scoreWidth > 0 && scores.get() ? textOffset + entry.scoreWidth : 0));
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
		int textX = bounds.x() + 3;
		int xEnd = bounds.x() + bounds.width() - 1;
		int titleEnd = yEnd - mainHeight;
		if (background.get()) {
			graphics.fill(textX - 2, titleEnd - 9 - 1 - topPadding.get() * 2, xEnd, titleEnd - 1, topColor.get().toInt());
			graphics.fill(textX - 2, titleEnd - 1, xEnd, yEnd, backgroundColor.get().toInt());
		}
		graphics.drawText(font, title, textX + maxWidth / 2 - titleWidth / 2, titleEnd - 9 - topPadding.get(), ColorUtil.Argb32.of(textAlpha.get(), -1), shadow.get());

		for (int v = 0; v < m; v++) {
			DisplayEntry lv2 = entries[v];
			int w = yEnd - (m - v) * 9;
			graphics.drawText(font, lv2.name, textX, w, ColorUtil.Argb32.of(textAlpha.get(), -1), shadow.get());
			if (scores.get()) {
				graphics.drawText(font, lv2.score, xEnd - lv2.scoreWidth, w, scoreColor.get().toInt(), shadow.get());
			}
		}

		if (outline.get() && outlineColor.get().getAlpha() > 0) {
			DrawUtil.outlineRect(graphics, bounds, outlineColor.get());
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(options.indexOf(super.backgroundColor), backgroundColor);
		options.remove(super.backgroundColor);
		options.add(topColor);
		options.add(scores);
		options.add(scoreColor);
		options.add(anchor);
		options.add(topPadding);
		options.remove(textColor);
		options.add(textAlpha);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return (anchor.get());
	}
}
