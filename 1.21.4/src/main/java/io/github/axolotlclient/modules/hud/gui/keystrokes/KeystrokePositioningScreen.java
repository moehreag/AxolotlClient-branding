/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.modules.hud.HudEditScreen;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.snapping.SnappingHelper;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class KeystrokePositioningScreen extends Screen {
	private final Screen parent;
	private final KeystrokeHud hud;
	private KeystrokeHud.Keystroke focused;
	private final KeystrokeHud.Keystroke editing;

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke focused) {
		super(Component.translatable("keystrokes.stroke.move"));
		this.parent = parent;
		this.hud = hud;
		this.editing = focused;
		mouseDown = false;
	}

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud) {
		this(parent, hud, null);
	}

	private DrawPosition offset = null;
	private boolean mouseDown;
	private SnappingHelper snap;

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, -300);
		super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		HudManager.getInstance().renderPlaceholder(guiGraphics, partialTick);
		guiGraphics.pose().popPose();
		renderTransparentBackground(guiGraphics);
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).pos(width/2-75, height-50+22).width(150).build());
		this.addRenderableWidget(Button.builder(Component.translatable("hud.snapping").append(": ")
				.append(Component.translatable(HudEditScreen.isSnappingEnabled() ? "options.on" : "options.off")),
			buttonWidget -> {
				HudEditScreen.toggleSnapping();
				buttonWidget.setMessage(Component.translatable("hud.snapping").append(": ")
					.append(Component.translatable(HudEditScreen.isSnappingEnabled() ? "options.on" : "options.off")));
				AxolotlClient.configManager.save();
			}).bounds(width / 2 - 50, height -50, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		if (editing != null) {
			var rect = editing.getRenderPosition();
			if (rect.isMouseOver(mouseX, mouseY)) {
				DrawUtil.fillRect(guiGraphics, rect, ClientColors.SELECTOR_BLUE.withAlpha(100));
			} else {
				DrawUtil.fillRect(guiGraphics, rect, ClientColors.WHITE.withAlpha(50));
			}
			editing.render(guiGraphics);
			DrawUtil.outlineRect(guiGraphics, rect, Colors.BLACK);
		} else {
			hud.keystrokes.forEach(s -> {
				var rect = s.getRenderPosition();
				if (rect.isMouseOver(mouseX, mouseY)) {
					DrawUtil.fillRect(guiGraphics, rect, ClientColors.SELECTOR_BLUE.withAlpha(100));
				} else {
					DrawUtil.fillRect(guiGraphics, rect, ClientColors.WHITE.withAlpha(50));
				}
				s.render(guiGraphics);
				DrawUtil.outlineRect(guiGraphics, rect, Colors.BLACK);
			});
		}
		if (mouseDown && snap != null) {
			snap.renderSnaps(guiGraphics);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var value = super.mouseClicked(mouseX, mouseY, button);
		if (button == 0) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			Optional<Rectangle> pos = Optional.empty();
			if (editing == null) {
				for (KeystrokeHud.Keystroke k : hud.keystrokes) {
					pos = Optional.of(k.getRenderPosition());
					if (pos.get().isMouseOver(mouseX, mouseY)) {
						entry = Optional.of(k);
						break;
					}
				}
			} else {
				pos = Optional.of(editing.getRenderPosition());
				if (pos.get().isMouseOver(mouseX, mouseY)) {
					entry = Optional.of(editing);
				}
			}
			if (entry.isPresent()) {
				focused = entry.get();
				mouseDown = true;
				var rect = pos.get();
				offset = new DrawPosition((int) Math.round(mouseX - rect.x()),
					(int) Math.round(mouseY - rect.y()));
				updateSnapState();
				return true;
			} else {
				focused = null;
			}
		}
		return value;
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
		hud.saveKeystrokes();
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (focused != null) {
			hud.saveKeystrokes();
		}
		snap = null;
		mouseDown = false;
		focused = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (focused != null && mouseDown) {
			focused.setX((int) mouseX - offset.x());
			focused.setY((int) mouseY - offset.y());
			if (snap != null) {
				Integer snapX, snapY;
				var rect = focused.getRenderPosition();
				snap.setCurrent(rect);
				if ((snapX = snap.getCurrentXSnap()) != null) {
					focused.setX(snapX);
				}
				if ((snapY = snap.getCurrentYSnap()) != null) {
					focused.setY(snapY);
				}
			}
			return true;
		}
		return false;
	}

	private List<Rectangle> getAllBounds() {
		return Stream.concat(HudManager.getInstance().getAllBounds().stream(), hud.keystrokes.stream().map(KeystrokeHud.Keystroke::getRenderPosition)).toList();
	}

	private void updateSnapState() {
		if (HudEditScreen.isSnappingEnabled() && focused != null) {
			snap = new SnappingHelper(getAllBounds(), focused.getRenderPosition());
		} else if (snap != null) {
			snap = null;
		}
	}
}
