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

package io.github.axolotlclient.modules.hud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
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
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.options.GenericOption;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class HudManager extends AbstractModule {

	private final static Path CUSTOM_MODULE_SAVE_PATH = AxolotlClient.resolveConfigFile("custom_hud.json");
	private final static HudManager INSTANCE = new HudManager();
	private final OptionCategory hudCategory = OptionCategory.create("hud");
	private final Map<Identifier, HudEntry> entries;
	private final MinecraftClient client;
	private final KeyBinding key;

	private HudManager() {
		this.entries = new LinkedHashMap<>();
		client = MinecraftClient.getInstance();
		key = new KeyBinding("key.openHud", GLFW.GLFW_KEY_RIGHT_SHIFT, "category.axolotlclient");
	}

	public static HudManager getInstance() {
		return INSTANCE;
	}

	public void init() {
		KeyBindingHelper.registerKeyBinding(key);
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (key.wasPressed()) {
				client.openScreen(new HudEditScreen());
			}
		});

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
		add(new MouseMovementHud());
		entries.put(BedwarsMod.getInstance().getUpgradesOverlay().getId(), BedwarsMod.getInstance().getUpgradesOverlay());

		entries.values().forEach(HudEntry::init);

		refreshAllBounds();

		Events.GAME_LOAD_EVENT.register(mc -> loadCustomEntries());

		hudCategory.add(new GenericOption("hud.custom_entry", "hud.custom_entry.add", () -> {
			CustomHudEntry entry = new CustomHudEntry();
			entry.setEnabled(true);
			entry.init();
			entry.onBoundsUpdate();
			entry.getAllOptions().includeInParentTree(false);
			add(entry);
			client.currentScreen.resize(client, client.currentScreen.width, client.currentScreen.height);
			saveCustomEntries();
		}));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveCustomEntries());
	}

	@SuppressWarnings("unchecked")
	private void loadCustomEntries() {
		try {
			if (Files.exists(CUSTOM_MODULE_SAVE_PATH)) {
				var obj = (List<Object>) GsonHelper.read(Files.readString(CUSTOM_MODULE_SAVE_PATH));
				obj.forEach(o -> {
					CustomHudEntry entry = new CustomHudEntry();
					var values = (Map<String, Object>)o;
					entry.getAllOptions().getOptions().forEach(opt -> {
						if (values.containsKey(opt.getName())) {
							opt.fromSerializedValue((String) values.get(opt.getName()));
						}
					});
					entry.getCategory().includeInParentTree(false);
					add(entry);
					entry.init();
					entry.onBoundsUpdate();
				});
			}
		} catch (IOException e) {
//TODO notify
		}
	}

	public void saveCustomEntries() {
		try {
			Files.createDirectories(CUSTOM_MODULE_SAVE_PATH.getParent());
			var writer = Files.newBufferedWriter(CUSTOM_MODULE_SAVE_PATH);
			var json = GsonHelper.GSON.newJsonWriter(writer);
			json.beginArray();
			for (Map.Entry<Identifier, HudEntry> entry : entries.entrySet()) {
				HudEntry hudEntry = entry.getValue();
				if (hudEntry instanceof CustomHudEntry hud) {
					json.beginObject();
					for (Option<?> opt : hud.getCategory().getOptions()) {
						json.name(opt.getName());
						json.value(opt.toSerializedValue());
					}
					json.endObject();
				}
			}
			json.endArray();
			json.close();
		} catch (IOException e) {
//TODO notify
		}
	}

	public void tick() {
		entries.values().stream().filter(hudEntry -> hudEntry.isEnabled() && hudEntry.tickable())
			.forEach(HudEntry::tick);
	}

	public HudManager add(AbstractHudEntry entry) {
		entries.put(entry.getId(), entry);
		hudCategory.add(entry.getAllOptions());
		return this;
	}

	public void refreshAllBounds() {
		for (HudEntry entry : getEntries()) {
			entry.onBoundsUpdate();
		}
	}

	public List<HudEntry> getEntries() {
		if (!entries.isEmpty()) {
			return new ArrayList<>(entries.values());
		}
		return new ArrayList<>();
	}

	public HudEntry get(Identifier identifier) {
		return entries.get(identifier);
	}

	public void removeEntry(Identifier identifier) {
		hudCategory.getSubCategories().remove(entries.remove(identifier).getCategory());
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
