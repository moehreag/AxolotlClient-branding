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

package io.github.axolotlclient.modules.hud;

import java.util.*;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.KeyBindOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.AbstractModule;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.gui.hud.*;
import io.github.axolotlclient.modules.hud.gui.hud.item.ArmorHud;
import io.github.axolotlclient.modules.hud.gui.hud.item.ArrowHud;
import io.github.axolotlclient.modules.hud.gui.hud.item.ItemUpdateHud;
import io.github.axolotlclient.modules.hud.gui.hud.simple.*;
import io.github.axolotlclient.modules.hud.gui.hud.vanilla.*;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.modules.hypixel.bedwars.BedwarsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class HudManager extends AbstractModule {

	private final static HudManager INSTANCE = new HudManager();
	private final OptionCategory hudCategory = new OptionCategory("hud", false);
	private final Map<Identifier, HudEntry> entries;
	private final MinecraftClient client;

	private HudManager() {
		this.entries = new LinkedHashMap<>();
		client = MinecraftClient.getInstance();
		hudCategory.add(new KeyBindOption("key.openHud", InputUtil.KEY_RIGHT_SHIFT_CODE, keyBind -> MinecraftClient.getInstance().setScreen(new HudEditScreen())));
	}

	public static HudManager getInstance() {
		return INSTANCE;
	}

	public void init() {
		//KeyBindingHelper.registerKeyBinding(key);

		AxolotlClient.CONFIG.addCategory(hudCategory);

		add(new PingHud());
		add(new FPSHud());
		add(new CPSHud());
		add(new ArmorHud());
		add(new PotionsHud());
		add(new KeystrokeHud());
		add(new ToggleSprintHud());
		add(new IPHud());
		add(new iconHud());
		add(new SpeedHud());
		add(new ScoreboardHud());
		add(new CrosshairHud());
		add(new CoordsHud());
		add(new ActionBarHud());
		add(new BossBarHud());
		add(new ArrowHud());
		add(new ItemUpdateHud());
		add(new PackDisplayHud());
		add(new IRLTimeHud());
		add(new ReachHud());
		add(new HotbarHUD());
		add(new MemoryHud());
		add(new PlayerCountHud());
		add(new CompassHud());
		add(new TPSHud());
		add(new ComboHud());
		add(new PlayerHud());
		entries.put(BedwarsMod.getInstance().getUpgradesOverlay().getId(), BedwarsMod.getInstance().getUpgradesOverlay());

		entries.values().forEach(HudEntry::init);

		refreshAllBounds();
	}

	public void tick() {
		entries.values().stream().filter(hudEntry -> hudEntry.isEnabled() && hudEntry.tickable())
			.forEach(HudEntry::tick);
	}

	public HudManager add(AbstractHudEntry entry) {
		entries.put(entry.getId(), entry);
		hudCategory.addSubCategory(entry.getAllOptions());
		return this;
	}

	public void refreshAllBounds() {
		for (HudEntry entry : getEntries()) {
			entry.onBoundsUpdate();
		}
	}

	public List<HudEntry> getEntries() {
		if (entries.size() > 0) {
			return new ArrayList<>(entries.values());
		}
		return new ArrayList<>();
	}

	public HudEntry get(Identifier identifier) {
		return entries.get(identifier);
	}

	public void render(MatrixStack matrices, float delta) {
		client.getProfiler().push("Hud Modules");
		if (!(client.currentScreen instanceof HudEditScreen)) {
			for (HudEntry hud : getEntries()) {
				if (hud.isEnabled() && (!client.options.debugEnabled || hud.overridesF3())) {
					client.getProfiler().push(hud.getName());
					hud.render(matrices, delta);
					client.getProfiler().pop();
				}
			}
		}
		client.getProfiler().pop();
	}

	public Optional<HudEntry> getEntryXY(int x, int y) {
		for (HudEntry entry : getMoveableEntries()) {
			Rectangle bounds = entry.getTrueBounds();
			if (bounds.x() <= x && bounds.x() + bounds.width() >= x && bounds.y() <= y
				&& bounds.y() + bounds.height() >= y) {
				return Optional.of(entry);
			}
		}
		return Optional.empty();
	}

	public List<HudEntry> getMoveableEntries() {
		if (entries.size() > 0) {
			return entries.values().stream().filter((entry) -> entry.isEnabled() && entry.movable())
				.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	public void renderPlaceholder(MatrixStack matrices, float delta) {
		for (HudEntry hud : getEntries()) {
			if (hud.isEnabled()) {
				hud.renderPlaceholder(matrices, delta);
			}
		}
	}

	public List<Rectangle> getAllBounds() {
		ArrayList<Rectangle> bounds = new ArrayList<>();
		for (HudEntry entry : getMoveableEntries()) {
			bounds.add(entry.getTrueBounds());
		}
		return bounds;
	}
}
