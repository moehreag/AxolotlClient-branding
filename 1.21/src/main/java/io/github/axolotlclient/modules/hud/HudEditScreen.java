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

import java.util.List;
import java.util.Optional;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.ConfigStyles;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class HudEditScreen extends Screen {

	private static final BooleanOption snapping = new BooleanOption("snapping", true);
	private static final OptionCategory hudEditScreenCategory = OptionCategory.create("hudEditScreen");

	static {
		hudEditScreenCategory.add(snapping);
		AxolotlClient.config.add(hudEditScreenCategory);
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
		super(Text.empty());
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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);

		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY(mouseX, mouseY);
		entry.ifPresent(abstractHudEntry -> abstractHudEntry.setHovered(true));
		HudManager.getInstance().renderPlaceholder(graphics, delta);
		if (mouseDown && snap != null) {
			snap.renderSnaps(graphics);
		}
	}

	@Override
	public void init() {
		this.addDrawableSelectableElement(new ButtonWidget.Builder(Text.translatable("hud.snapping").append(": ")
			.append(Text.translatable(snapping.get() ? "options.on" : "options.off")),
			buttonWidget -> {
				snapping.toggle();
				buttonWidget.setMessage(Text.translatable("hud.snapping").append(": ")
					.append(Text.translatable(snapping.get() ? "options.on" : "options.off")));
				AxolotlClient.configManager.save();
			}).positionAndSize(width / 2 - 50, height / 2 + 12, 100, 20).build());

		this.addDrawableSelectableElement(new ButtonWidget.Builder(Text.translatable("hud.clientOptions"),
			buttonWidget -> {
				Screen screen = ConfigStyles.createScreen(this, AxolotlClient.configManager.getRoot());
				MinecraftClient.getInstance().setScreen(screen);
			}).positionAndSize(width / 2 - 75, height / 2 - 10, 150, 20).build());

		if (parent != null)
			addDrawableSelectableElement(new ButtonWidget.Builder(CommonTexts.BACK, buttonWidget -> MinecraftClient.getInstance().setScreen(parent))
				.positionAndSize(width / 2 - 75, height - 50 + 22, 150, 20).build());
		else
			addDrawableSelectableElement(new ButtonWidget.Builder(Text.translatable("close"),
				buttonWidget -> MinecraftClient.getInstance().setScreen(null))
				.positionAndSize(width / 2 - 75, height - 50 + 22, 150, 20).build());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		Optional<HudEntry> entry = HudManager.getInstance().getEntryXY((int) Math.round(mouseX),
			(int) Math.round(mouseY));
		if (button == 0) {
			mouseDown = true;
			if (entry.isPresent()) {
				current = entry.get();
				offset = new DrawPosition((int) Math.round(mouseX - current.getTruePos().x()),
					(int) Math.round(mouseY - current.getTruePos().y()));
				updateSnapState();
				return true;
			} else {
				current = null;
			}
		} else if (button == 1) {
			entry.ifPresent(abstractHudEntry -> {
				Screen screen = ConfigStyles.createScreen(this, abstractHudEntry.getCategory());
				MinecraftClient.getInstance().setScreen(screen);
			});
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (current != null) {
			AxolotlClient.configManager.save();
		}
		current = null;
		snap = null;
		mouseDown = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (current != null) {
			current.setX((int) mouseX - offset.x() + current.offsetTrueWidth());
			current.setY((int) mouseY - offset.y() + current.offsetTrueHeight());
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
			return true;
		}
		return false;
	}
}
