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

package io.github.axolotlclient.modules.hypixel;

import lombok.Getter;

@Getter
public enum HypixelGameType {
	MAIN("Main Lobby"),
	BLOCKING_DEAD("The Blocking Dead"),
	BOUNTY_HUNTERS("Bounty Hunters"),
	CREEPER_ATTACK("Creeper Attack"),
	CAPTURE_THE_WOOL("Capture The Wool"),
	DRAGON_WARS("Dragon Wars"),
	ENDER_SPLEEF("Ender Spleef"),
	FARM_HUNT("Farm Hunt"),
	FOOTBALL("Football"),
	GALAXY_WARS("Galaxy Wars"),
	HIDE_AND_SEEK("Hide and Seek", "Prop Hunt", "Party Pooper"),
	HOLE_IN_THE_WALL("Hole in the Wall"),
	HYPIXEL_SAYS("Hypixel Says"),
	MINI_WALLS("Mini Walls"),
	PARTY_GAMES("Party Games"),
	PIXEL_PAINTERS("Pixel Painters"),
	PIXEL_PARTY("Pixel Party", "Normal Mode", "Hyper Mode"),
	THROW_OUT("Throw Out"),
	ZOMBIES("Zombies", "Dead End", "Bad Blood", "Alien Arcadium"),
	BEDWARS("Bed Wars", "Solo", "Doubles", "3v3v3v3", "4v4v4v4", "4v4", "Dreams"),
	BLITZ_SG("Blitz SG", "Solo", "Teams"),
	BUILD_BATTLE("Build Battle", "Solo Mode", "Teams Mode", "Pro Mode", "Guess The Build"),
	ARENA_BRAWL("Arena Brawl", "1v1", "2v2", "4v4"),
	PAINTBALL("Paintball Warfare"),
	QUAKECRAFT("Quakecraft", "Solo", "Teams"),
	THE_WALLS("The Walls"),
	TURBO_KART_RACERS("Turbo Kart Racers"),
	VAMPIREZ("VampireZ"),
	COPS_AND_CRIMS("Cops and Crims", "Challenge Mode", "Defusal", "Gun Game", "Team Deathmatch"),
	BLITZ_DUELS("Blitz Duels"),
	BOW_DUELS("Bow Duels"),
	BOXING_DUELS("Boxing Duels"),
	CLASSIC_DUELS("Classic Duels"),
	COMBO_DUELS("Combo Duels"),
	DUEL_ARENA("Duel Arena"),
	MEGA_WALLS_DUELS("Mega Walls Duels", "1v1", "2v2"),
	NODEBUFF_DUELS("NoDebuff Duels"),
	OP_DUELS("OP Duels", "1v1", "2v2"),
	PARKOUR_DUELS("Parkour Duels"),
	SKYWARS_DUELS("SkyWars Duels", "1v1", "2v2"),
	SUMO_DUELS("Sumo Duels"),
	THE_BRIDGE("The Bridge", "1v1", "2v2", "3v3", "4v4", "2v2v2v2", "3v3v3v3", "CTF 3v3"),
	TNT_GAMES_DUELS("TNT Games Duels"),
	UHC_DUELS("UHC Duels", "1v1", "2v2", "4v4", "8 Player FFA"),
	HOUSING("Housing"),
	HYPIXEL_SMP("Hypixel SMP"),
	MEGA_WALLS("Mega Walls", "Standard", "Face Off", "Challenge"),
	MURDER_MYSTERY("Murder Mystery", "Classic", "Double Up!", "Assassins", "Infection"),
	DROPPER("Dropper"),
	SKYBLOCK("Skyblock", "Classic", "Ironman", "Stranded"),
	SKYWARS("SkyWars", "Solo Normal", "Solo Insane", "Doubles Normal", "Doubles Insane", "Lucky Block Solo", "Lucky Block Team"),
	SMASH_HEROES("Smash Heroes", "1v1", "2v2", "Solo", "Team", "Friends"),
	HYPIXEL_PIT("The Hypixel Pit"),
	BOW_SPLEEF("Bow Spleef"),
	PVP_RUN("PVP Run"),
	TNT_RUN("TNT Run"),
	TNT_TAG("TNT Tag"),
	WIZARDS("Wizards"),
	UHC_CHAMPIONS("UHC Champions", "Solo", "Teams of Three"),
	SPEED_UHC("Speed UHC", "Solo Normal", "Team Normal"),
	CAPTURE_THE_FLAG("Capture The Flag", "Domination", "Team Deathmatch"),
	WOOL_WARS("Wool Wars");

	private final String name;

	private final String[] gameModes;

	HypixelGameType(String name, String... gameModes) {
		this.name = name;
		this.gameModes = gameModes;
	}
}
