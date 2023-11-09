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

package io.github.axolotlclient.modules.hypixel.autogg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.options.StringOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;

/**
 * Based on <a href="https://github.com/DragonEggBedrockBreaking/AutoGG/blob/trunk/src/main/java/uk/debb/autogg/mixin/MixinChatHud.java">DragonEggBedrockBreaking's AutoGG Mod</a>
 *
 * @license MPL-2.0
 */

public class AutoGG implements AbstractHypixelMod {

	@Getter
	private final static AutoGG Instance = new AutoGG();
	public final BooleanOption gg = new BooleanOption("printGG", false);
	public final StringOption ggString = new StringOption("ggString", "gg");
	public final BooleanOption gf = new BooleanOption("printGF", false);
	public final StringOption gfString = new StringOption("gfString", "gf");
	public final BooleanOption glhf = new BooleanOption("printGLHF", false);
	public final StringOption glhfString = new StringOption("glhfString", "glhf");
	private final OptionCategory category = new OptionCategory("autogg");
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final BooleanOption onHypixel = new BooleanOption("onHypixel", false);
	private final BooleanOption onBWP = new BooleanOption("onBWP", false);
	private final BooleanOption onPVPL = new BooleanOption("onPVPL", false);
	private final BooleanOption onMMC = new BooleanOption("onMMC", false);
	private final HashMap<String, List<String>> ggStrings = new HashMap<>();
	private final HashMap<String, List<String>> gfStrings = new HashMap<>();
	private final HashMap<String, List<String>> glhfStrings = new HashMap<>();
	private final HashMap<String, BooleanOption> serverMap = new HashMap<>();
	private long lastTime = 0;

	@Override
	public void init() {
		populateGGStrings();
		populateGFStrings();
		populateGLHFStrings();
		serverMap.put("hypixel.net", onHypixel);

		serverMap.put("bedwarspractice.club", onBWP);

		serverMap.put("pvp.land", onPVPL);

		serverMap.put("minemen.club", onMMC);

		category.add(gg);
		category.add(ggString);
		category.add(gf);
		category.add(gfString);
		category.add(glhf);
		category.add(glhfString);
		category.add(onHypixel);
		category.add(onBWP);
		category.add(onPVPL);
		category.add(onMMC);

		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(this::onMessage);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	private void populateGGStrings() {
		ggStrings.put("hypixel.net", addToList(
				"1st Killer -",
				"1st Place -",
				"Winner:",
				" - Damage Dealt -",
				"Winning Team -",
				"1st -",
				"Winners:",
				"Winner:",
				"Winning Team:",
				" won the game!",
				"Top Seeker:",
				"1st Place:",
				"Last team standing!",
				"Winner #1 (",
				"Top Survivors",
				"Winners -",
				"Sumo Duel -",
				"Most Wool Placed -",
				"Your Overall Winstreak:"
			)
		);

		ggStrings.put("bedwarspractice.club", addToList(
			"Winners -",
			"Game Won!",
			"Game Lost!",
			"The winning team is"));

		ggStrings.put("pvp.land", addToList(
			"The match has ended!",
			"Match Results",
			"Winner:",
			"Loser:"
		));

		ggStrings.put("minemen.club", addToList("Match Results"));
	}

	private void populateGFStrings() {
		gfStrings.put("hypixel.net", addToList("SkyWars Experience (Kill)",
			"coins! (Final Kill)"));

		gfStrings.put("bedwarspractice.club", addToList(client.getSession().getUsername() + " FINAL KILL!"));

		gfStrings.put("pvp.land", addToList("slain by " + client.getSession().getUsername()));

		gfStrings.put("minemen.club", addToList("killed by " + client.getSession().getUsername() + "!"));
	}

	private void populateGLHFStrings() {
		glhfStrings.put("hypixel.net", addToList("The game starts in 1 second!"));

		glhfStrings.put("bedwarspractice.club", addToList("Game starting in 1 seconds!", "Game has started!"));

		glhfStrings.put("minemen.club", addToList("1..."));
	}

	private List<String> addToList(String... strings) {
		return Arrays.stream(strings).toList();
	}

	public void onMessage(ReceiveChatMessageEvent event) {
		if (client.getCurrentServerEntry() != null) {
			serverMap.keySet().forEach(s -> {
				if (serverMap.get(s).get() && client.getCurrentServerEntry().address.contains(s)) {
					if (gf.get()) {
						processChat(event.getOriginalMessage(), gfStrings.get(s), gfString.get());
					}
					if (gg.get()) {
						processChat(event.getOriginalMessage(), ggStrings.get(s), ggString.get());
					}
					if (glhf.get()) {
						processChat(event.getOriginalMessage(), glhfStrings.get(s), glhfString.get());
					}
				}
			});
		}
	}

	private void processChat(String messageReceived, List<String> options, String messageToSend) {
		if (System.currentTimeMillis() - this.lastTime > 3000 && options != null) {
			for (String s : options) {
				if (messageReceived.contains(s)) {
					Util.sendChatMessage(messageToSend);
					this.lastTime = System.currentTimeMillis();
					return;
				}
			}
		}
	}
}
