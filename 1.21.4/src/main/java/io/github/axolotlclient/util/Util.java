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
import com.mojang.blaze3d.platform.NativeImage;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Graphics;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.scores.*;
import org.apache.commons.lang3.StringUtils;

public class Util {
	private static final Map<ResourceLocation, DynamicTexture> textures = new HashMap<>();
	public static String lastgame;
	public static String game;

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
		else if (Minecraft.getInstance().getCurrentServer() != null
				 && Minecraft.getInstance().getCurrentServer().ip.toLowerCase()
					 .contains(sidebar.getFirst().toLowerCase())) {
			if (sidebar.getLast().toLowerCase(Locale.ROOT)
					.contains(Minecraft.getInstance().getCurrentServer().ip.toLowerCase(Locale.ROOT))
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
		Minecraft client = Minecraft.getInstance();
		if (client.level == null)
			return lines;

		Scoreboard scoreboard = client.level.getScoreboard();
		Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar == null)
			return lines;

		Collection<PlayerScoreEntry> scores = scoreboard.listPlayerScores(sidebar);
		List<PlayerScoreEntry> list = scores.stream().filter(
				input -> input != null && !input.isHidden())
			.collect(Collectors.toList());

		if (list.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
		} else {
			scores = list;
		}

		for (PlayerScoreEntry score : scores) {
			PlayerTeam team = scoreboard.getPlayerTeam(score.owner());
			if (team == null)
				return lines;
			String text = team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString();
			if (!text.trim().isEmpty())
				lines.add(text);
		}

		lines.add(sidebar.getDisplayName().getString());
		Collections.reverse(lines);

		return lines;
	}

	public static Component formatFromCodes(String formattedString) {
		MutableComponent text = Component.empty();
		String[] arr = formattedString.split("§");

		List<ChatFormatting> modifiers = new ArrayList<>();
		for (String s : arr) {
			ChatFormatting formatting = ChatFormatting.getByCode(!s.isEmpty() ? s.charAt(0) : 0);
			if (formatting != null && formatting.isFormat()) {
				modifiers.add(formatting);
			}
			MutableComponent part = Component.literal(!s.isEmpty() ? s.substring(1) : "");
			if (formatting != null) {
				part.withStyle(formatting);

				if (!modifiers.isEmpty()) {
					modifiers.forEach(part::withStyle);
					if (formatting.equals(ChatFormatting.RESET)) {
						modifiers.clear();
					}
				}
			}
			text.append(part);
		}
		return text;
	}

	public static void sendChatMessage(String msg) {
		msg = StringUtil.trimChatMessage(StringUtils.normalizeSpace(msg.trim()));
		assert Minecraft.getInstance().player != null;
		if (msg.startsWith("/")) {
			Minecraft.getInstance().player.connection.sendCommand(msg.substring(1));
		} else {
			Minecraft.getInstance().player.connection.sendChat(msg);
		}
	}

	public static void sendChatMessage(Component msg) {
		Minecraft.getInstance().gui.getChat().addMessage(msg);
	}

	public static ResourceLocation getTexture(GraphicsOption option) {
		return getTexture(option.get(), option.getName());
	}

	public static ResourceLocation getTexture(Graphics graphics, String name) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("axolotlclient", "graphics_"+ name.toLowerCase(Locale.ROOT));
		try {
			DynamicTexture texture;
			if (!textures.containsKey(id)) {
				texture = new DynamicTexture(NativeImage.read(graphics.getPixelData()));
				Minecraft.getInstance().getTextureManager().register(id, texture);
				textures.put(id, texture);
			} else {
				texture = textures.get(id);
				for (int x = 0; x < graphics.getWidth(); x++) {
					for (int y = 0; y < graphics.getHeight(); y++) {
						texture.getPixels().setPixel(x, y, graphics.getPixelColor(x, y));
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
