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

package io.github.axolotlclient.util;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Util {

	public static final Color GlColor = new Color();
	private static final TreeMap<Integer, String> map = new TreeMap<>();
	public static String lastgame;
	public static String game;

	static {
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	/**
	 * Gets the amount of ticks in between start and end, on a 24000 tick system.
	 *
	 * @param start The start of the time you wish to measure
	 * @param end   The end of the time you wish to measure
	 * @return The amount of ticks in between start and end
	 */
	public static int getTicksBetween(int start, int end) {
		if (end < start)
			end += 24000;
		return end - start;
	}

	public static String getGame() {
		List<String> sidebar = getSidebar();

		if (sidebar.isEmpty())
			game = "";
		else if (MinecraftClient.getInstance().getCurrentServerEntry() != null
			&& MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase()
			.contains(sidebar.get(0).toLowerCase())) {
			if (sidebar.get(sidebar.size() - 1).toLowerCase(Locale.ROOT)
				.contains(MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase(Locale.ROOT))
				|| sidebar.get(sidebar.size() - 1).contains("Playtime")) {
				game = "In Lobby";
			} else {
				if (sidebar.get(sidebar.size() - 1).contains("--------")) {
					game = "Playing Bridge Practice";
				} else {
					game = "Playing " + sidebar.get(sidebar.size() - 1);
				}
			}
		} else {
			game = "Playing " + sidebar.get(0);
		}

		if (!Objects.equals(lastgame, game) && game.equals(""))
			game = lastgame;
		else
			lastgame = game;

		if (game == null) {
			game = "";
		}

		return game;
	}

	public static List<String> getSidebar() {
		List<String> lines = new ArrayList<>();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null)
			return lines;

		Scoreboard scoreboard = client.world.getScoreboard();
		if (scoreboard == null)
			return lines;
		ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(1);
		if (sidebar == null)
			return lines;

		Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(sidebar);
		List<ScoreboardPlayerScore> list = scores.stream().filter(
				input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
			.collect(Collectors.toList());

		if (list.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
		} else {
			scores = list;
		}

		for (ScoreboardPlayerScore score : scores) {
			Team team = scoreboard.getPlayerTeam(score.getPlayerName());
			if (team == null)
				return lines;
			String text = team.getPrefix().getString() + team.getSuffix().getString();
			if (text.trim().length() > 0)
				lines.add(text);
		}

		lines.add(sidebar.getDisplayName().getString());
		Collections.reverse(lines);

		return lines;
	}

	public static Text formatFromCodes(String formattedString) {
		MutableText text = LiteralText.EMPTY.copy();
		String[] arr = formattedString.split("§");

		List<Formatting> modifiers = new ArrayList<>();
		for (String s : arr) {
			Formatting formatting = Formatting.byCode(s.length() > 0 ? s.charAt(0) : 0);
			if (formatting != null && formatting.isModifier()) {
				modifiers.add(formatting);
			}
			MutableText part = new LiteralText(s.length() > 0 ? s.substring(1) : "");
			if (formatting != null) {
				part.formatted(formatting);

				if (!modifiers.isEmpty()) {
					modifiers.forEach(part::formatted);
					if (formatting.equals(Formatting.RESET)) {
						modifiers.clear();
					}
				}
			}
			text.append(part);
		}
		return text;
	}

	public static double calculateDistance(Vec3d pos1, Vec3d pos2) {
		return calculateDistance(pos1.x, pos2.x, pos1.y, pos2.y, pos1.z, pos2.z);
	}

	public static double calculateDistance(double x1, double x2, double y1, double y2, double z1, double z2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
	}

	public static void sendChatMessage(String msg) {
		MinecraftClient.getInstance().player.sendChatMessage(msg);
	}

	public static void sendChatMessage(Text msg) {
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(msg);
	}

	public static void applyScissor(int x, int y, int width, int height) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		Window window = MinecraftClient.getInstance().getWindow();
		double scale = window.getScaleFactor();
		GL11.glScissor((int) (x * scale), (int) ((window.getScaledHeight() - height - y) * scale),
			(int) (width * scale), (int) (height * scale));
	}

	public static double lerp(double start, double end, double percent) {
		return start + ((end - start) * percent);
	}

	// https://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java
	public static String toRoman(int number) {
		try {
			if (number < 0) {
				return toRoman(Math.abs(number));
			}

			int l = map.floorKey(number);
			if (number == l) {
				return map.get(number);
			}
			return map.get(l) + toRoman(number - l);
		} catch (Exception e) {
			return String.valueOf(number);
		}
	}

	public static class Color {

		public float red = 1.0F;
		public float green = 1.0F;
		public float blue = 1.0F;
		public float alpha = 1.0F;

		public Color() {
		}

		public Color(float red, float green, float blue, float alpha) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
		}
	}
}
