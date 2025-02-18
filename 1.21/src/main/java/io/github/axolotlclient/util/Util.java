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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.texture.NativeImage;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Graphics;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.scoreboard.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class Util {
	public static String lastgame;
	public static String game;

	public static String getGame() {
		List<String> sidebar = getSidebar();

		if (sidebar.isEmpty())
			game = "";
		else if (MinecraftClient.getInstance().getCurrentServerEntry() != null
			&& MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase()
			.contains(sidebar.getFirst().toLowerCase())) {
			if (sidebar.getLast().toLowerCase(Locale.ROOT)
				.contains(MinecraftClient.getInstance().getCurrentServerEntry().address.toLowerCase(Locale.ROOT))
				|| sidebar.getLast().contains("Playtime")) {
				game = "In Lobby";
			} else {
				if (sidebar.getLast().contains("--------")) {
					game = "Playing Bridge Practice";
				} else {
					game = "Playing " + sidebar.getLast();
				}
			}
		} else {
			game = "Playing " + sidebar.getFirst();
		}

		if (!Objects.equals(lastgame, game) && game.isEmpty())
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
		ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (sidebar == null)
			return lines;

		Collection<ScoreboardEntry> scores = scoreboard.getEntriesForObjective(sidebar);
		List<ScoreboardEntry> list = scores.stream().filter(
				input -> input != null && input.owner() != null && !input.isHidden())
			.collect(Collectors.toList());

		if (list.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
		} else {
			scores = list;
		}

		for (ScoreboardEntry score : scores) {
			Team team = scoreboard.getPlayerTeam(score.owner());
			if (team == null)
				return lines;
			String text = team.getPrefix().getString() + team.getSuffix().getString();
			if (!text.trim().isEmpty())
				lines.add(text);
		}

		lines.add(sidebar.getDisplayName().getString());
		Collections.reverse(lines);

		return lines;
	}

	public static Text formatFromCodes(String formattedString) {
		MutableText text = Text.empty();
		String[] arr = formattedString.split("§");

		List<Formatting> modifiers = new ArrayList<>();
		for (String s : arr) {
			Formatting formatting = Formatting.byCode(!s.isEmpty() ? s.charAt(0) : 0);
			if (formatting != null && formatting.isModifier()) {
				modifiers.add(formatting);
			}
			MutableText part = Text.literal(!s.isEmpty() ? s.substring(1) : "");
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

	public static void sendChatMessage(String msg) {
		msg = ChatUtil.cutString(StringUtils.normalizeSpace(msg.trim()));
		assert MinecraftClient.getInstance().player != null;
		if (msg.startsWith("/")) {
			MinecraftClient.getInstance().player.networkHandler.sendCommand(msg.substring(1));
		} else {
			MinecraftClient.getInstance().player.networkHandler.sendChatMessage(msg);
		}
	}

	public static void addMessageToChatHud(Text msg) {
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(msg);
	}

	public static Identifier getTexture(GraphicsOption option) {
		return getTexture(option.get(), option.getName());
	}

	public static Identifier getTexture(Graphics graphics, String name) {
		Identifier id = Identifier.of("axolotlclient", "graphics_" + name.toLowerCase(Locale.ROOT));
		try {
			NativeImageBackedTexture texture;
			if (MinecraftClient.getInstance().getTextureManager().getOrDefault(id, null) == null) {
				texture = new NativeImageBackedTexture(NativeImage.read(graphics.getPixelData()));
				MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
			} else {
				texture = (NativeImageBackedTexture) MinecraftClient.getInstance().getTextureManager().getTexture(id);
				for (int x = 0; x < graphics.getWidth(); x++) {
					for (int y = 0; y < graphics.getHeight(); y++) {
						texture.getImage().setPixelColor(x, y, graphics.getPixelColor(x, y));
					}
				}
			}

			texture.upload();
		} catch (IOException e) {
			AxolotlClient.LOGGER.error("Failed to bind texture for " + name + ": ", e);
		}
		return id;
	}

	public static double lerp(double start, double end, double percent) {
		return start + ((end - start) * percent);
	}

	public static String toRoman(int number) {
		if (number > 0) {
			return "I".repeat(number).replace("IIIII", "V").replace("IIII", "IV")
				.replace("VV", "X").replace("VIV", "IX").replace("XXXXX", "L").replace("XXXX", "XL")
				.replace("LL", "C").replace("LXL", "XC").replace("CCCCC", "D").replace("CCCC", "CD")
				.replace("DD", "M").replace("DCD", "CM");
		}
		return "";
	}
}
