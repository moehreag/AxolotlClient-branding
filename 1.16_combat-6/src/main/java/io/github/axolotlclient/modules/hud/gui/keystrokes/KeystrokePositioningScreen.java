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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class KeystrokePositioningScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private final Screen parent;
	private final KeystrokeHud hud;
	private KeystrokeHud.Keystroke focused;
	private final KeystrokeHud.Keystroke editing;

	public KeystrokePositioningScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke focused) {
		super("keystrokes.stroke.move");
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
	public void init() {
		super.init();
		addDrawableChild(new ButtonWidget(width / 2 - 75, height - 50 + 22, 150, 20, ScreenTexts.BACK, b -> onClose()));
		this.addDrawableChild(new ButtonWidget(width / 2 - 50, height - 50, 100, 20, new TranslatableText("hud.snapping").append(": ")
			.append(new TranslatableText(HudEditScreen.isSnappingEnabled() ? "options.on" : "options.off")),
			buttonWidget -> {
				HudEditScreen.toggleSnapping();
				buttonWidget.setMessage(new TranslatableText("hud.snapping").append(": ")
					.append(new TranslatableText(HudEditScreen.isSnappingEnabled() ? "options.on" : "options.off")));
				AxolotlClient.configManager.save();
			}));
	}

	@Override
	public void render(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.push();
		guiGraphics.translate(0, 0, -300);
		super.renderBackground(guiGraphics);
		HudManager.getInstance().renderPlaceholder(guiGraphics, partialTick);
		guiGraphics.pop();
		fillGradient(guiGraphics, 0, 0, this.width, this.height, -1072689136, -804253680);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		if (editing != null) {
			drawStroke(guiGraphics, mouseX, mouseY, editing);
		} else {
			hud.keystrokes.forEach(s -> drawStroke(guiGraphics, mouseX, mouseY, s));
		}
		if (mouseDown && snap != null) {
			snap.renderSnaps(guiGraphics);
		}
	}

	private void drawStroke(MatrixStack guiGraphics, int mouseX, int mouseY, KeystrokeHud.Keystroke s) {
		var rect = getScaledRenderPos(s);
		if (rect.isMouseOver(mouseX, mouseY)) {
			DrawUtil.fillRect(guiGraphics, rect, ClientColors.SELECTOR_BLUE.withAlpha(100));
		} else {
			DrawUtil.fillRect(guiGraphics, rect, ClientColors.WHITE.withAlpha(50));
		}
		guiGraphics.push();
		guiGraphics.scale(hud.getScale(), hud.getScale(), 1);
		s.render(guiGraphics);
		guiGraphics.pop();
		DrawUtil.outlineRect(guiGraphics, rect, Colors.BLACK);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var value = super.mouseClicked(mouseX, mouseY, button);
		if (button == 0) {
			Optional<KeystrokeHud.Keystroke> entry = Optional.empty();
			Optional<Rectangle> pos = Optional.empty();
			if (editing == null) {
				for (KeystrokeHud.Keystroke k : hud.keystrokes) {
					pos = Optional.of(getScaledRenderPos(k));
					if (pos.get().isMouseOver(mouseX, mouseY)) {
						entry = Optional.of(k);
						break;
					}
				}
			} else {
				pos = Optional.of(getScaledRenderPos(editing));
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
		client.openScreen(parent);
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
			focused.setX((int) Math.round((mouseX - offset.x()) / hud.getScale()));
			focused.setY((int) Math.round((mouseY - offset.y()) / hud.getScale()));
			if (snap != null) {
				Integer snapX, snapY;
				var rect = getScaledRenderPos(focused);
				snap.setCurrent(rect);
				if ((snapX = snap.getCurrentXSnap()) != null) {
					focused.setX((int) (snapX / hud.getScale()));
				}
				if ((snapY = snap.getCurrentYSnap()) != null) {
					focused.setY(Math.round(snapY / hud.getScale()));
				}
			}
			return true;
		}
		return false;
	}

	private Rectangle getScaledRenderPos(KeystrokeHud.Keystroke stroke) {
		return stroke.getRenderPosition().scale(hud.getScale());
	}

	private List<Rectangle> getAllBounds() {
		return Stream.concat(HudManager.getInstance().getAllBounds().stream(), hud.keystrokes.stream().map(this::getScaledRenderPos)).toList();
	}

	private void updateSnapState() {
		if (HudEditScreen.isSnappingEnabled() && focused != null) {
			snap = new SnappingHelper(getAllBounds(), getScaledRenderPos(focused));
		} else if (snap != null) {
			snap = null;
		}
	}
}
