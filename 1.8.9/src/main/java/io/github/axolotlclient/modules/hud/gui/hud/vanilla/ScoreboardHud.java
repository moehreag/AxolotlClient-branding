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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.Util;
import net.minecraft.resource.Identifier;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.scoreboard.team.Team;
import net.minecraft.util.Pair;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class ScoreboardHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "scoreboardhud");
	private final ScoreboardObjective placeholder = Util.make(() -> {
		Scoreboard placeholderScoreboard = new Scoreboard();
		ScoreboardObjective objective = placeholderScoreboard.createObjective("Scoreboard", ScoreboardCriterion.DUMMY);
		ScoreboardScore dark = placeholderScoreboard.getScore("DarkKronicle", objective);
		dark.set(8780);

		ScoreboardScore moeh = placeholderScoreboard.getScore("moehreag", objective);
		moeh.set(743);

		ScoreboardScore kode = placeholderScoreboard.getScore("TheKodeToad", objective);
		kode.set(2948);

		placeholderScoreboard.setDisplayObjective(1, objective);

		return objective;
	});

	private final ColorOption backgroundColor = new ColorOption("backgroundcolor", new Color(0x4C000000));
	private final ColorOption topColor = new ColorOption("topbackgroundcolor", new Color(0x66000000));
	private final IntegerOption topPadding = new IntegerOption("toppadding", 0, 0, 4);
	private final BooleanOption scores = new BooleanOption("scores", true);
	private final ColorOption scoreColor = new ColorOption("scorecolor", new Color(0xFFFF5555));
	private final IntegerOption textAlpha = new IntegerOption("text_alpha", 255, 0, 255);
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint(AnchorPoint.MIDDLE_RIGHT);

	public ScoreboardHud() {
		super(200, 146, true);
	}

	@Override
	public void render(float delta) {
		GlStateManager.pushMatrix();
		scale();
		renderComponent(delta);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderComponent(float delta) {
		Scoreboard scoreboard = this.client.world.getScoreboard();
		ScoreboardObjective scoreboardObjective = null;
		Team team = scoreboard.getTeam(this.client.player.getDisplayName().getString());
		if (team != null) {
			int t = team.getColor().getId();
			if (t >= 0) {
				scoreboardObjective = scoreboard.getDisplayObjective(3 + t);
			}
		}

		ScoreboardObjective scoreboardObjective2 = scoreboardObjective != null ? scoreboardObjective
			: scoreboard.getDisplayObjective(1);
		if (scoreboardObjective2 != null) {
			this.renderScoreboardSidebar(scoreboardObjective2, false);
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		renderScoreboardSidebar(placeholder, true);
	}

	// Abusing this could break some stuff/could allow for unfair advantages. The goal is not to do this, so it won't
	// show any more information than it would have in vanilla.
	private void renderScoreboardSidebar(ScoreboardObjective objective, boolean placeholder) {
		Scoreboard scoreboard = objective.getScoreboard();
		Collection<ScoreboardScore> scores = scoreboard.getScores(objective);
		List<ScoreboardScore> filteredScores = scores.stream()
			.filter((testScore) -> testScore.getOwner() != null && !testScore.getOwner().startsWith("#"))
			.collect(Collectors.toList());

		if (filteredScores.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(filteredScores, scores.size() - 15));
		} else {
			scores = filteredScores;
		}

		List<Pair<ScoreboardScore, String>> scoresWText = Lists.newArrayListWithCapacity(scores.size());
		String text = objective.getDisplayName();
		int displayNameWidth = client.textRenderer.getWidth(text);
		int maxWidth = displayNameWidth;
		int spacerWidth = client.textRenderer.getWidth(": ");

		ScoreboardScore scoreboardPlayerScore;
		String formattedText;
		for (Iterator<ScoreboardScore> scoresIterator = scores.iterator(); scoresIterator
			.hasNext(); maxWidth = Math
			.max(maxWidth,
				client.textRenderer.getWidth(formattedText) + (this.scores.get()
					? spacerWidth + client.textRenderer
					.getWidth(Integer.toString(scoreboardPlayerScore.get()))
					: 0))) {
			scoreboardPlayerScore = scoresIterator.next();
			Team team = scoreboard.getTeamOfMember(scoreboardPlayerScore.getOwner());
			formattedText = Team.getMemberDisplayName(team, scoreboardPlayerScore.getOwner());
			scoresWText.add(new Pair<>(scoreboardPlayerScore, formattedText));
		}
		maxWidth = maxWidth + 6;

		int scoresSize = scores.size();
		int scoreHeight = scoresSize * 9;
		int fullHeight = scoreHeight + 11 + topPadding.get() * 2;

		boolean updated = false;
		if (fullHeight + 1 != height) {
			setHeight(fullHeight + 1);
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

		int renderX = bounds.x() + bounds.width() - maxWidth;
		int renderY = bounds.y() + (bounds.height() / 2 - fullHeight / 2) + 1;

		int scoreX = renderX + 4;
		int scoreY = renderY + scoreHeight + 10;
		int num = 0;
		int textOffset = scoreX - 4;

		for (Pair<ScoreboardScore, String> scoreboardPlayerScoreTextPair : scoresWText) {
			++num;
			ScoreboardScore scoreboardPlayerScore2 = scoreboardPlayerScoreTextPair.getLeft();
			String scoreText = scoreboardPlayerScoreTextPair.getRight();
			String score = String.valueOf(scoreboardPlayerScore2.get());
			int relativeY = scoreY - num * 9 + topPadding.get() * 2;

			if (background.get() && backgroundColor.get().getAlpha() > 0 && !placeholder) {
				if (num == scoresSize) {
					RenderUtil.drawRectangle(textOffset, relativeY - 1, maxWidth, 10, backgroundColor.get().toInt());
				} else if (num == 1) {
					RenderUtil.drawRectangle(textOffset, relativeY, maxWidth, 10, backgroundColor.get());
				} else {
					RenderUtil.drawRectangle(textOffset, relativeY, maxWidth, 9, backgroundColor.get());
				}
			}

			if (shadow.get()) {
				client.textRenderer.drawWithShadow(scoreText, (float) scoreX, (float) relativeY, Colors.WHITE.withAlpha(textAlpha.get()).toInt());
			} else {
				client.textRenderer.draw(scoreText, scoreX, relativeY, Colors.WHITE.withAlpha(textAlpha.get()).toInt());
			}
			if (this.scores.get()) {
				drawString(score, (float) (scoreX + maxWidth - client.textRenderer.getWidth(score) - 6),
					(float) relativeY, scoreColor.get().toInt(), shadow.get());
			}
			if (num == scoresSize) {
				// Draw the title
				if (background.get() && !placeholder) {
					RenderUtil.drawRectangle(textOffset, relativeY - 10 - topPadding.get() * 2 - 1, maxWidth,
						10 + topPadding.get() * 2, topColor.get());
				}
				float title = (renderX + (maxWidth - displayNameWidth) / 2F);
				if (shadow.get()) {
					client.textRenderer.drawWithShadow(text, title, (float) (relativeY - 9) - topPadding.get(), Colors.WHITE.withAlpha(textAlpha.get()).toInt());
				} else {
					client.textRenderer.draw(text, (int) title, (relativeY - 9), Colors.WHITE.withAlpha(textAlpha.get()).toInt());
				}
			}
		}

		if (outline.get() && outlineColor.get().getAlpha() > 0 && !placeholder) {
			RenderUtil.drawOutline(textOffset, bounds.y(), maxWidth, fullHeight + 1, outlineColor.get());
		}
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = new ArrayList<>();
		options.add(enabled);
		options.add(scale);
		options.add(textColor);
		options.add(shadow);
		options.add(background);
		options.add(backgroundColor);
		options.add(outline);
		options.add(outlineColor);
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
		return anchor.get();
	}
}
