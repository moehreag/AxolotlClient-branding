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

import java.awt.*;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.screen.OptionsScreenBuilder;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class HudEditScreen extends Screen {

	private static final BooleanOption snapping = new BooleanOption("snapping", true);
	private static final OptionCategory hudEditScreenCategory = new OptionCategory("hudEditScreen");

	static {
		hudEditScreenCategory.add(snapping);
		AxolotlClient.config.addSubCategory(hudEditScreenCategory);
	}

	private final Screen parent;
	private HudEntry current;
	private DrawPosition offset = null;
	private boolean mouseDown;
	private SnappingHelper snap;

	public HudEditScreen() {
		this(null);
	}

	public HudEditScreen(Screen parent) {
		super();
		updateSnapState();
		mouseDown = false;
		this.parent = parent;
	}

	private void updateSnapState() {
		if (snapping.get() && current != null) {
			List<Rectangle> bounds = HudManager.getInstance().getAllBounds();
			bounds.remove(current.getTrueBounds());
			snap = new SnappingHelper(bounds, current.getTrueBounds());
		} else if (snap != null) {
			snap = null;
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (MinecraftClient.getInstance().world != null)
			fillGradient(0, 0, width, height, new Color(0xB0100E0E, true).hashCode(),
				new Color(0x46212020, true).hashCode());
		else {
			renderDirtBackground(0);
		}

		super.render(mouseX, mouseY, delta);
		GlStateManager.enableTexture();

		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
		entry.ifPresent(abstractHudEntry -> abstractHudEntry.setHovered(true));
		HudManager.getInstance().renderPlaceholder(delta);
		if (mouseDown && snap != null) {
			snap.renderSnaps();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY(Math.round(mouseX),
			Math.round(mouseY));
		if (button == 0) {
			mouseDown = true;
			if (entry.isPresent()) {
				current = entry.get();
				offset = new DrawPosition(Math.round(mouseX - current.getTruePos().x()),
					Math.round(mouseY - current.getTruePos().y()));
				updateSnapState();
			} else {
				current = null;
			}
		} else if (button == 1) {
			entry.ifPresent(abstractHudEntry -> MinecraftClient.getInstance().setScreen(
				new OptionsScreenBuilder(this, abstractHudEntry.getOptionsAsCategory(), AxolotlClient.MODID)));
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int button) {
		if (current != null) {
			AxolotlClient.configManager.save();
		}
		current = null;
		snap = null;
		mouseDown = false;
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long mouseLastClicked) {
		if (current != null) {
			current.setX((mouseX - offset.x()) + current.offsetTrueWidth());
			current.setY(mouseY - offset.y() + current.offsetTrueHeight());
			if (snap != null) {
				Integer snapX, snapY;
				snap.setCurrent(current.getTrueBounds());
				if ((snapX = snap.getCurrentXSnap()) != null) {
					current.setX(snapX + current.offsetTrueWidth());
				}
				if ((snapY = snap.getCurrentYSnap()) != null) {
					current.setY(snapY + current.offsetTrueHeight());
				}
			}
			if (current.tickable()) {
				current.tick();
			}
		}
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		switch (button.id) {
			case 3:
				snapping.toggle();
				button.message = I18n.translate("hud.snapping") + ": "
					+ I18n.translate(snapping.get() ? "options.on" : "options.off");
				AxolotlClient.configManager.save();
				break;
			case 1:
				MinecraftClient.getInstance().setScreen(new OptionsScreenBuilder(this,
					(OptionCategory) new OptionCategory("config", false).addSubCategories(AxolotlClient.CONFIG.getCategories()),
					AxolotlClient.MODID));
				break;
			case 0:
				MinecraftClient.getInstance().setScreen(parent);
				break;
			case 2:
				MinecraftClient.getInstance().setScreen(null);
				break;
		}
	}

	@Override
	public void init() {
		this.buttons.add(new ButtonWidget(3, width / 2 - 50, height / 2 + 12, 100, 20,
			I18n.translate("hud.snapping") + ": " + I18n.translate(snapping.get() ? "options.on" : "options.off")));

		this.buttons.add(
			new ButtonWidget(1, width / 2 - 75, height / 2 - 10, 150, 20, I18n.translate("hud.clientOptions")));

		if (parent != null)
			buttons.add(new ButtonWidget(0, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("back")));
		else
			buttons.add(new ButtonWidget(2, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("close")));
	}
}
