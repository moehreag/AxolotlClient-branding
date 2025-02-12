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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.modules.hypixel.bedwars.upgrades.BedwarsTeamUpgrades;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import io.github.axolotlclient.util.events.impl.ScoreboardRenderEvent;
import io.github.axolotlclient.util.notifications.Notifications;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * @author DarkKronicle
 */

public class BedwarsGame {
	private final Map<String, BedwarsPlayer> players = new HashMap<>();
	private final Map<UUID, BedwarsPlayer> playersById = new HashMap<>();
	private final MinecraftClient mc;
	private final BedwarsMod mod;
	@Getter
	private final BedwarsTeamUpgrades upgrades = new BedwarsTeamUpgrades();
	private BedwarsTeam won = null;
	private int wonTick = -1;
	private int seconds = 0;
	private Text topBarText = Text.empty();
	private Text bottomBarText = Text.empty();
	private BedwarsPlayer me = null;
	@Getter
	private boolean started = false;
	private BedwarsPlayer lastKill;
	private BedwarsPlayer lastKiller;


	public BedwarsGame(BedwarsMod mod) {
		mc = MinecraftClient.getInstance();
		this.mod = mod;
	}

	public void onStart() {
		mod.upgradesOverlay.onStart(upgrades);
		players.clear();
		playersById.clear();
		Map<BedwarsTeam, List<PlayerListEntry>> teamPlayers = new HashMap<>();
		for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
			String name = mc.inGameHud.getPlayerListHud().getPlayerName(player).getString().replaceAll("§.", "");
			if (name.charAt(1) != ' ') {
				continue;
			}
			BedwarsTeam team = BedwarsTeam.fromPrefix(name.charAt(0)).orElse(null);
			if (team == null) {
				continue;
			}
			teamPlayers.compute(team, (t, entries) -> {
				if (entries == null) {
					List<PlayerListEntry> players = new ArrayList<>();
					players.add(player);
					return players;
				}
				entries.add(player);
				return entries;
			});
		}
		for (Map.Entry<BedwarsTeam, List<PlayerListEntry>> teamPlayerList : teamPlayers.entrySet()) {
			teamPlayerList.getValue().sort(Comparator.comparing(p -> p.getProfile().getName()));
			List<PlayerListEntry> value = teamPlayerList.getValue();
			for (int i = 0; i < value.size(); i++) {
				PlayerListEntry e = value.get(i);
				BedwarsPlayer p = new BedwarsPlayer(teamPlayerList.getKey(), e, i + 1);
				if (mc.player.getGameProfile().getName().equals(e.getProfile().getName())) {
					me = p;
				}
				players.put(e.getProfile().getName(), p);
				playersById.put(e.getProfile().getId(), p);
			}
		}
		this.started = true;
	}

	public Text getTopBarText() {
		return topBarText;
	}

	public Text getBottomBarText() {
		return bottomBarText;
	}

	private String calculateTopBarText() {
		String topBar = getFormattedTime();
		if (me.getStats() != null) {
			topBar += "\n" +
					  "K: " + me.getStats().getGameKills() +
					  " D: " + me.getStats().getGameDeaths() +
					  " B: " + me.getStats().getGameBedsBroken();
		}
		return topBar;
	}

	private String calculateBottomBarText() {
		return Formatting.DARK_AQUA + "Last Kill: " + Formatting.RESET + (lastKill == null ? "N/A" : lastKill.getColoredName()) +
			   Formatting.DARK_AQUA + " Last Killed By: " + Formatting.RESET + (lastKiller == null ? "N/A" : lastKiller.getColoredName());
		// left in here because it'll be useful later on
		/*Comparator<BedwarsPlayer> comparator = Comparator.comparingInt(o -> o.getStats().getGameKills());
		return "Top 3 Killers: \n" + players.values().stream().filter(Objects::nonNull)
			.sorted(comparator.reversed()).limit(3)
			.map(p -> p.getColoredName() + ": " + p.getStats().getGameKills())
			.collect(Collectors.joining("\n"));*/
	}

	public String getFormattedTime() {
		int minute = seconds / 60;
		int second = seconds % 60;
		String time = minute + ":";
		if (second < 10) {
			time += "0" + second;
		} else {
			time += second;
		}
		return time;
	}

	public Optional<BedwarsPlayer> getPlayer(UUID uuid) {
		return Optional.ofNullable(playersById.getOrDefault(uuid, null));
	}

	public Optional<BedwarsPlayer> getPlayer(String name) {
		return Optional.ofNullable(players.getOrDefault(name, null));
	}

	private void debug(String message) {
		mc.inGameHud.getChatHud().addMessage(Text.literal("§b§lINFO:§8 " + message));
	}

	private void died(ReceiveChatMessageEvent event, BedwarsPlayer player, @Nullable BedwarsPlayer killer, BedwarsDeathType type, boolean finalDeath) {
		player.died();
		if (killer != null) {
			killer.killed(finalDeath);
		}
		if (mod.overrideMessages.get()) {
			event.setNewMessage(Text.literal(formatDeath(player, killer, type, finalDeath)));
		}
		if (me.equals(killer)) {
			lastKill = player;
		} else if (me.equals(player)) {
			lastKiller = killer;
		}
	}

	private String formatDisconnect(BedwarsPlayer disconnected) {
		String playerFormatted = getPlayerFormatted(disconnected);
		return playerFormatted + " §7§o/disconnected/";
	}

	private String formatReconnect(BedwarsPlayer reconnected) {
		String playerFormatted = getPlayerFormatted(reconnected);
		return playerFormatted + " §7§o/reconnected/";
	}

	private String formatEliminated(BedwarsTeam team) {
		StringBuilder message = new StringBuilder(
			"§6§l§oTEAM ELIMINATED §8§l> " + team.getColorSection() + team.getName() + " Team §7/eliminated/ ");
		for (BedwarsPlayer p : players.values().stream()
			.filter(b -> b.getTeam() == team)
			.sorted(Comparator.comparingInt(BedwarsPlayer::getNumber))
			.toList()) {
			BedwarsPlayerStats stats = p.getStats();
			if (stats == null) {
				continue;
			}
			message.append("\n")
				.append("§b")
				.append(stats.getStars())
				.append(" ")
				.append(p.getColoredName())
				.append("§7 Beds: §f")
				.append(stats.getBedsBroken())
				.append("§7 Finals: §f")
				.append(stats.getFinalKills())
				.append("§7 FKDR: §f")
				.append(String.format("%.2f", stats.getFKDR()))
				.append("§7 BBLR: §f")
				.append(String.format("%.2f", stats.getBBLR()));
		}
		return message.toString();
	}

	private String formatBed(BedwarsTeam team, BedwarsPlayer breaker) {
		String playerFormatted = getPlayerFormatted(breaker);
		return "§6§l§oBED BROKEN §8§l> " + team.getColorSection() + team.getName() + " Bed §7/broken/ " + playerFormatted +
			   (breaker.getStats() == null || breaker.getTeam() != me.getTeam() ? "" : " §6" + breaker.getStats().getBedsBroken());
	}

	private String formatDeath(BedwarsPlayer player, @Nullable BedwarsPlayer killer, BedwarsDeathType type, boolean finalDeath) {
		String inner = type.getInner().get();
		if (finalDeath) {
			inner = "§6§l/" + inner.toUpperCase(Locale.ROOT) + "/";
		} else {
			inner = "§7/" + inner + "/";
		}
		String playerFormatted = getPlayerFormatted(player);
		if (killer == null) {
			return playerFormatted + " " + inner;
		}
		String killerFormatted = getPlayerFormatted(killer);
		if (finalDeath && killer.getStats() != null && killer.getTeam() == me.getTeam()) {
			killerFormatted += " §6" + killer.getStats().getFinalKills();
		}
		return playerFormatted + " " + inner + " " + killerFormatted;
	}

	private String getPlayerFormatted(BedwarsPlayer player) {
		return player.getColoredTeamNumber() + " " + player.getProfile().getProfile().getName();
	}

	public boolean isTeamEliminated(BedwarsTeam team) {
		return players.values().stream().filter(b -> b.getTeam() == team).allMatch(BedwarsPlayer::isFinalKilled);
	}

	public void onChatMessage(String rawMessage, ReceiveChatMessageEvent event) {
		try {
			if (mod.removeAnnoyingMessages.get() && BedwarsMessages.matched(BedwarsMessages.ANNOYING_MESSAGES, rawMessage).isPresent()) {
				event.setCancelled(true);
				return;
			}
			if (BedwarsDeathType.getDeath(rawMessage, (type, m) -> {
				died(m, rawMessage, event, type);
			})) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.BED_DESTROY, rawMessage, m -> {
				Optional<BedwarsPlayer> player = BedwarsMessages.matched(BedwarsMessages.BED_BREAK, rawMessage).flatMap(m1 -> getPlayer(m1.group(1)));
				if (player.isEmpty()) {
					AxolotlClient.LOGGER.warn("Unknown bed break message: "+rawMessage);
					Notifications.getInstance().addStatus("bedwars.unknown_bed_break", "bedwars.unknown_message");
					return;
				}
				BedwarsTeam team = BedwarsTeam.fromName(m.group(1)).orElse(me.getTeam());
				bedDestroyed(event, team, player.get());
			})) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.DISCONNECT, rawMessage, m -> getPlayer(m.group(1)).ifPresent(p -> disconnected(event, p)))) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.RECONNECT, rawMessage, m -> getPlayer(m.group(1)).ifPresent(p -> reconnected(event, p)))) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.GAME_END, rawMessage, m -> {
				BedwarsTeam win = players.values().stream().filter(p -> !p.isFinalKilled()).findFirst().map(BedwarsPlayer::getTeam).orElse(null);
				this.won = win;
				this.wonTick = mc.inGameHud.getTicks() + 10;
			})) {
				return;
			}
			if (BedwarsMessages.matched(BedwarsMessages.TEAM_ELIMINATED, rawMessage, m -> BedwarsTeam.fromName(m.group(1)).ifPresent(t -> teamEliminated(event, t)))) {
				return;
			}
			upgrades.onMessage(rawMessage);
		} catch (Exception e) {
			debug("Error: " + e);
		}
	}

	private void died(Matcher m, String rawMessage, ReceiveChatMessageEvent event, BedwarsDeathType type) {
		BedwarsPlayer killed = getPlayer(m.group(1)).orElse(null);
		BedwarsPlayer killer = null;
		if (type != BedwarsDeathType.SELF_UNKNOWN && type != BedwarsDeathType.SELF_VOID) {
			killer = getPlayer(m.group(2)).orElse(null);
		}
		if (killed == null) {
			debug("Player " + m.group(1) + " was not found");
			return;
		}
		died(event, killed, killer, type, BedwarsMessages.matched(BedwarsMessages.FINAL_KILL, rawMessage).isPresent());
	}

	private void gameEnd(BedwarsTeam win) {
		if (me == null) {
			BedwarsMod.getInstance().gameEnd();
			return;
		}

		mc.inGameHud.getChatHud().addMessage(
			Text.literal("§8§m----------[§7Winstreaks§8]----------")
		);
		for (BedwarsPlayer p : players.values()) {
			if (p.getStats() != null && p.getStats().getWinstreak() > 0) {
				boolean winner = p.getTeam().equals(win);
				int before = p.getStats().getWinstreak();
				int after = winner ? before + 1 : 0;
				mc.inGameHud.getChatHud().addMessage(
					Text.literal(
						getPlayerFormatted(p) + "§8: §7" + before + " §8 -> §" + (winner ? "a" : "c") + after
					));
			}
		}

		BedwarsMod.getInstance().gameEnd();
	}

	private void teamEliminated(ReceiveChatMessageEvent event, BedwarsTeam team) {
		// Make sure everyone is dead, just in case
		players.values().stream().filter(b -> b.getTeam() == team).forEach(b -> {
			b.setBed(false);
			b.died();
		});
		if (mod.overrideMessages.get()) {
			event.setNewMessage(Text.literal(formatEliminated(team)));
		}
	}

	private void bedDestroyed(ReceiveChatMessageEvent event, BedwarsTeam team, @Nullable BedwarsPlayer breaker) {
		players.values().stream().filter(b -> b.getTeam() == team).forEach(b -> b.setBed(false));
		if (breaker != null && breaker.getStats() != null) {
			breaker.getStats().addBed();
		}
		if (mod.overrideMessages.get()) {
			event.setNewMessage(Text.literal(formatBed(team, breaker)));
		}
	}

	private void disconnected(ReceiveChatMessageEvent event, BedwarsPlayer player) {
		player.disconnected();
		if (mod.overrideMessages.get()) {
			event.setNewMessage(Text.literal(formatDisconnect(player)));
		}
	}


	private void reconnected(ReceiveChatMessageEvent event, BedwarsPlayer player) {
		player.reconnected();
		if (mod.overrideMessages.get()) {
			event.setNewMessage(Text.literal(formatReconnect(player)));
		}
	}

	public void onScoreboardRender(ScoreboardRenderEvent event) {
		Scoreboard scoreboard = event.getObjective().getScoreboard();
		Collection<ScoreboardEntry> scores = scoreboard.getEntriesForObjective(event.getObjective());
		List<ScoreboardEntry> filteredScores = scores.stream()
			.filter(p_apply_1_ -> p_apply_1_.owner() != null && !p_apply_1_.isHidden())
			.collect(Collectors.toList());
		Collections.reverse(filteredScores);
		if (filteredScores.size() < 3) {
			return;
		}
		ScoreboardEntry score = filteredScores.get(2);
		Team team = scoreboard.getPlayerTeam(score.owner());
		String timer = Team.decorateName(team, Text.literal(score.owner())).getString();
		if (!timer.contains(":")) {
			return;
		}
		int seconds;
		try {
			seconds = Integer.parseInt(timer.split(":")[1].substring(0, 2));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int target = (60 - seconds) % 60;
		if (this.seconds % 60 != target) {
			// Update seconds
			while (this.seconds % 60 != target) {
				updateClock();
			}
			topBarText = Text.literal(calculateTopBarText());
			bottomBarText = Text.literal(calculateBottomBarText());
		}
	}

	private void updateClock() {
		this.seconds++;
	}

	public void tick() {
		int currentTick = mc.inGameHud.getTicks();
		if (won != null && currentTick >= wonTick) {
			gameEnd(won);
		}
		players.values().forEach(p -> p.tick(currentTick));
	}

	public void updateEntries(List<PlayerListEntry> entries) {
		// Update latencies and other information for entries
		entries.forEach(entry ->
			getPlayer(entry.getProfile().getName()).ifPresent(player -> player.updateListEntry(entry))
		);
	}

	public List<PlayerListEntry> getTabPlayerList(List<PlayerListEntry> original) {
		updateEntries(original);
		return players.values().stream().filter(b -> !b.isFinalKilled()).sorted((b1, b2) -> {
			if (b1.getTeam() == b2.getTeam()) {
				return Integer.compare(b1.getNumber(), b2.getNumber());
			}
			return Integer.compare(b1.getTeam().ordinal(), b2.getTeam().ordinal());
		}).map(BedwarsPlayer::getProfile).collect(Collectors.toList());
	}

	public BedwarsPlayer getSelf() {
		return me;
	}

	public String getLevelHead(AbstractClientPlayerEntity entity) {
		BedwarsPlayer player = getPlayer(entity.getUuid()).orElse(null);
		if (player == null) {
			return null;
		}
		BedwarsPlayerStats stats = player.getStats();
		if (stats == null) {
			return null;
		}
		BedwarsLevelHeadMode mode = (mod.bedwarsLevelHeadMode.get());
		return mode.apply(stats);
	}

	public void renderCustomScoreboardObjective(GuiGraphics graphics, String playerName, ScoreboardObjective objective, int y, int endX) {
		BedwarsPlayer bedwarsPlayer = getPlayer(playerName).orElse(null);
		if (bedwarsPlayer == null) {
			return;
		}
		String render;
		int color;
		if (!bedwarsPlayer.isAlive()) {
			if (bedwarsPlayer.isDisconnected()) {
				return;
			}
			int tickTillLive = Math.max(0, bedwarsPlayer.getTickAlive() - mc.inGameHud.getTicks());
			float secondsTillLive = tickTillLive / 20f;
			render = String.format("%.1f", secondsTillLive) + "s";
			color = new Color(200, 200, 200).toInt();
		} else {
			int health = objective.getScoreboard().getOrCreateScore(ScoreHolder.of(playerName), objective).getScore();
			color = ClientColors.blend(new Color(255, 255, 255), new Color(215, 0, 64), (int) (1 - (health / 20f))).toInt();
			render = String.valueOf(health);
		}
		// Health
		graphics.drawShadowedText(mc.textRenderer,
			render,
			(endX - mc.textRenderer.getWidth(render)),
			y,
			color
		);

	}
}
