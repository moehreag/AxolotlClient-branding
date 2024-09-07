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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.scoreboard.*;
import net.minecraft.text.MutableText;
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
		Scoreboard scoreboard = objective.getScoreboard();
		Collection<ScoreboardEntry> scores = scoreboard.getEntriesForObjective(objective);
		List<ScoreboardEntry> filteredScores = scores.stream()
			.filter((testScore) -> testScore.owner() != null && !testScore.isHidden())
			.collect(Collectors.toList());

		if (filteredScores.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(filteredScores, scores.size() - 15));
		} else {
			scores = filteredScores;
		}

		List<Pair<ScoreboardEntry, Text>> scoresWText = Lists.newArrayListWithCapacity(scores.size());
		Text text = objective.getDisplayName();
		int displayNameWidth = client.textRenderer.getWidth(text);
		int maxWidth = displayNameWidth;
		int spacerWidth = client.textRenderer.getWidth(": ");

		ScoreboardEntry scoreboardPlayerScore;
		MutableText formattedText;
		for (Iterator<ScoreboardEntry> scoresIterator = scores.iterator(); scoresIterator
			.hasNext(); maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(formattedText) + spacerWidth
													  + client.textRenderer.getWidth(Integer.toString(scoreboardPlayerScore.value())))) {
			scoreboardPlayerScore = scoresIterator.next();
			Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.owner());
			formattedText = Team.decorateName(team, Text.literal(scoreboardPlayerScore.owner()));
			scoresWText.add(Pair.of(scoreboardPlayerScore, formattedText));
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

		for (Pair<ScoreboardEntry, Text> scoreboardPlayerScoreTextPair : scoresWText) {
			++num;
			ScoreboardEntry scoreboardPlayerScore2 = scoreboardPlayerScoreTextPair.getFirst();
			Text scoreText = scoreboardPlayerScoreTextPair.getSecond();
			String score = String.valueOf(scoreboardPlayerScore2.value());
			int relativeY = scoreY - num * 9 + topPadding.get() * 2;

			if (background.get() && backgroundColor.get().toInt() > 0) {
				if (num == scoresSize) {
					RenderUtil.drawRectangle(graphics, textOffset, relativeY - 1, maxWidth, 10,
						backgroundColor.get().toInt());
				} else if (num == 1) {
					RenderUtil.drawRectangle(graphics, textOffset, relativeY, maxWidth, 10,
						backgroundColor.get().toInt());
				} else {
					RenderUtil.drawRectangle(graphics, textOffset, relativeY, maxWidth, 9,
						backgroundColor.get().toInt());
				}
			}

			graphics.drawText(client.textRenderer, scoreText, scoreX, relativeY, -1, shadow.get());
			if (this.scores.get()) {
				drawString(graphics, score, (float) (scoreX + maxWidth - client.textRenderer.getWidth(score) - 6),
					(float) relativeY, scoreColor.get().toInt(), shadow.get());
			}
			if (num == scoresSize) {
				// Draw the title
				if (background.get()) {
					RenderUtil.drawRectangle(graphics, textOffset, relativeY - 10 - topPadding.get() * 2 - 1, maxWidth,
						10 + topPadding.get() * 2, topColor.get());
				}
				float title = (float) (renderX + (maxWidth - displayNameWidth) / 2);
				graphics.drawText(client.textRenderer, text, (int) title, (relativeY - 9) - topPadding.get(), -1, shadow.get());
			}
		}

		if (outline.get() && outlineColor.get().getAlpha() > 0) {
			RenderUtil.drawOutline(graphics, textOffset, bounds.y(), maxWidth, fullHeight + 2, outlineColor.get());
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
