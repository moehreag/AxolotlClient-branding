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

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Graphics;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.mixin.MinecraftClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.team.Team;
import net.minecraft.text.Formatting;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

public class Util {

	public static final Color GlColor = new Color();
	public static String lastgame;
	public static String game;

	@ApiStatus.Internal
	public static Window window;

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

	public static int toGlCoordsX(int x) {
		if (window == null) {
			window = new Window(Minecraft.getInstance());
		}
		return x * window.getScale();
	}

	public static int toGlCoordsY(int y) {
		if (window == null) {
			window = new Window(Minecraft.getInstance());
		}
		int scale = window.getScale();
		return Minecraft.getInstance().height - y * scale - scale;
	}

	public static int toMCCoordsX(int x) {
		if (window == null) {
			window = new Window(Minecraft.getInstance());
		}
		return x * window.getWidth() / Minecraft.getInstance().width;
	}

	public static int toMCCoordsY(int y) {
		if (window == null) {
			window = new Window(Minecraft.getInstance());
		}
		return window.getHeight() - y * window.getHeight() / Minecraft.getInstance().height - 1;
	}

	public static Window getWindow() {
		if (window == null) {
			try {
				window = new Window(Minecraft.getInstance());
			} catch (Exception e) {
				return null;
			}
		}
		return window;
	}

	public static void sendChatMessage(String msg) {
		Minecraft.getInstance().player.sendChat(msg);
	}

	public static void sendChatMessage(Text msg) {
		Minecraft.getInstance().gui.getChat().addMessage(msg);
	}

	public static String splitAtCapitalLetters(String string) {
		if (string == null || string.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (char c : string.toCharArray()) {
			if (Character.isUpperCase(c) && c != string.charAt(0)) {
				builder.append(" ");
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static String getGame() {
		List<String> sidebar = getSidebar();

		if (sidebar.isEmpty())
			game = "";
		else if (Util.getCurrentServerAddress() != null
			&& Util.getCurrentServerAddress().toLowerCase().contains(sidebar.get(0).toLowerCase())) {
			if (sidebar.get(sidebar.size() - 1).toLowerCase(Locale.ROOT)
				.contains(Util.getCurrentServerAddress().toLowerCase(Locale.ROOT))
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

		if (!Objects.equals(lastgame, game) && game.isEmpty())
			game = lastgame;
		else
			lastgame = game;

		if (game == null) {
			game = "";
		}

		return Formatting.strip(game);
	}

	public static List<String> getSidebar() {
		List<String> lines = new ArrayList<>();
		Minecraft client = Minecraft.getInstance();
		if (client.world == null)
			return lines;

		Scoreboard scoreboard = client.world.getScoreboard();
		if (scoreboard == null)
			return lines;
		ScoreboardObjective sidebar = scoreboard.getDisplayObjective(1);
		if (sidebar == null)
			return lines;

		Collection<ScoreboardScore> scores = scoreboard.getScores(sidebar);
		List<ScoreboardScore> list = scores.stream().filter(
				input -> input != null && input.getOwner() != null && !input.getOwner().startsWith("#"))
			.collect(Collectors.toList());

		if (list.size() > 15) {
			scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
		} else {
			scores = list;
		}

		for (ScoreboardScore score : scores) {
			Team team = scoreboard.getTeamOfMember(score.getOwner());
			if (team == null)
				return lines;
			String text = team.getPrefix() + team.getSuffix();
			if (!text.trim().isEmpty())
				lines.add(text);
		}

		lines.add(sidebar.getDisplayName());
		Collections.reverse(lines);

		return lines;
	}

	public static String getCurrentServerAddress() {
		if (Minecraft.getInstance().isInSingleplayer()) {
			return null;
		}

		if (Minecraft.getInstance().getCurrentServerEntry() != null) {
			return Minecraft.getInstance().getCurrentServerEntry().address;
		}
		return ((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress() != null
			? ((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress()
			: null;
	}

	public static double calculateDistance(Vec3d pos1, Vec3d pos2) {
		return calculateDistance(pos1.x, pos2.x, pos1.y, pos2.y, pos1.z, pos2.z);
	}

	public static double calculateDistance(double x1, double x2, double y1, double y2, double z1, double z2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
	}

	public static <T> T make(Supplier<T> factory) {
		return factory.get();
	}

	public static <T> T make(T object, Consumer<T> initializer) {
		initializer.accept(object);
		return object;
	}

	public static boolean currentServerAddressContains(String address) {
		if (Minecraft.getInstance().isInSingleplayer()
			|| Minecraft.getInstance().isIntegratedServerRunning()) {
			return false;
		}
		if (Minecraft.getInstance().getCurrentServerEntry() != null) {
			return Minecraft.getInstance().getCurrentServerEntry().address.contains(address);
		}
		return ((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress() != null
			&& ((MinecraftClientAccessor) Minecraft.getInstance()).getServerAddress().contains(address);
	}

	public static float lerp(float start, float end, float percent) {
		return start + ((end - start) * percent);
	}

	public static String toRoman(int number) {
		if (number > 0) {
			return Strings.repeat("I", number).replace("IIIII", "V").replace("IIII", "IV")
				.replace("VV", "X").replace("VIV", "IX").replace("XXXXX", "L").replace("XXXX", "XL")
				.replace("LL", "C").replace("LXL", "XC").replace("CCCCC", "D").replace("CCCC", "CD")
				.replace("DD", "M").replace("DCD", "CM");
		}
		return "";
	}

	public static Identifier getTexture(GraphicsOption option) {
		return getTexture(option.get(), option.getName());
	}

	public static Identifier getTexture(Graphics graphics, String name) {
		Identifier id = new Identifier("axolotlclient", "graphics_" + name.toLowerCase(Locale.ROOT));
		try {
			DynamicTexture texture;
			if (Minecraft.getInstance().getTextureManager().get(id) == null) {
				texture = new DynamicTexture(ImageIO.read(new ByteArrayInputStream(graphics.getPixelData())));
				Minecraft.getInstance().getTextureManager().register(id, texture);
			} else {
				texture = (DynamicTexture) Minecraft.getInstance().getTextureManager().get(id);
				int[] pix = texture.getPixels();
				for (int x = 0; x < graphics.getWidth(); x++) {
					for (int y = 0; y < graphics.getHeight(); y++) {
						int rows = (y) * graphics.getWidth() + x;
						pix[rows] = graphics.getPixelColor(x, y);
					}
				}
			}

			texture.upload();
		} catch (IOException e) {
			AxolotlClient.LOGGER.error("Failed to bind texture for " + name + ": ", e);
		}
		return id;
	}

	public static void bindTexture(GraphicsOption option) {
		Identifier id = getTexture(option);
		Minecraft.getInstance().getTextureManager().bind(id);
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
