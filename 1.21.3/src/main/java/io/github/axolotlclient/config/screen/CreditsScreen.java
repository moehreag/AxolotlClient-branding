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

package io.github.axolotlclient.config.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreditsScreen extends Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final List<Credit> credits = new ArrayList<>();
	private final SoundInstance bgm = SimpleSoundInstance.forUI(SoundEvents.MUSIC_DISC_CHIRP.value(), 1, 1);
	private Overlay creditOverlay;
	private ContainerObjectSelectionList<Credit> creditsList;

	public CreditsScreen(Screen parent) {
		super(Component.translatable("credits"));
		this.parent = parent;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (AxolotlClient.someNiceBackground.get()) { // Credit to pridelib for the colors
			graphics.fill(0, 0, width, height / 6, 0xFFff0018);
			graphics.fill(0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			graphics.fill(0, height * 2 / 6, width, height / 2, 0xFFffff41);
			graphics.fill(0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			graphics.fill(0, height / 2, width, height * 2 / 3, 0xFF008018);
			graphics.fill(0, height * 5 / 6, width, height, 0xFF86007d);
		} else {
			super.renderBackground(graphics, mouseX, mouseY, delta);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (AxolotlClient.CONFIG.creditsBGM.get() && !minecraft.getSoundManager().isActive(bgm)) {
			minecraft.getSoundManager().play(bgm);
		}

		super.render(graphics, mouseX, mouseY, tickDelta);

		graphics.drawCenteredString(this.font, Component.translatable("credits"), width / 2, 20, -1);

		if (creditOverlay != null) {
			creditOverlay.render(graphics);
		} else {
			creditsList.render(graphics, mouseX, mouseY, tickDelta);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			if (creditOverlay == null) {
				minecraft.setScreen(parent);
			} else {
				creditOverlay = null;
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void init() {
		credits.clear();
		initCredits();

		creditsList = new CreditsList(minecraft, width, height - 100, 50, 25);
		addWidget(creditsList);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, buttonWidget -> {
			if (creditOverlay == null) {
				minecraft.setScreen(parent);
				stopBGM();
			} else {
				creditOverlay = null;
			}
		}).bounds(width / 2 - 75, height - 50 + 22, 150, 20).build());

		this.addRenderableWidget(Button.builder(Component.translatable("creditsBGM").append(": ")
				.append(Component.translatable(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")),
			buttonWidget -> {
				AxolotlClient.CONFIG.creditsBGM.toggle();
				AxolotlClient.configManager.save();
				stopBGM();
				buttonWidget.setMessage(Component.translatable("creditsBGM").append(": ").append(
					Component.translatable(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")));
			}).bounds(6, this.height - 26, 100, 20).build());
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		if (creditOverlay != null)
			creditOverlay.init();
		super.resize(client, width, height);
	}

	private void initCredits() {
		credits.add(new SpacerTitle("- - - - - - " + I18n.get("contributors") + " - - - - - -"));

		Credits.getContributors().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		credits.add(new SpacerTitle("- - - - - - " + I18n.get("other_people") + " - - - - - -"));

		Credits.getOtherPeople().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		if (!externalModuleCredits.isEmpty()) {
			credits.add(new SpacerTitle("- - - - - - " + I18n.get("external_modules") + " - - - - - -"));
			externalModuleCredits.forEach((s, s2) -> credits.add(new Credit(s, s2)));
		}
	}

	private void stopBGM() {
		minecraft.getSoundManager().stop(bgm);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if (creditOverlay != null) {
			if (!creditOverlay.isMouseOver(mouseX, mouseY)) {
				creditOverlay = null;
				this.creditsList.mouseClicked(mouseX, mouseY, button);
			} else {
				creditOverlay.mouseClicked(mouseX, mouseY);
			}
		} else {
			this.creditsList.mouseClicked(mouseX, mouseY, button);
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return creditsList.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
			   || creditsList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
		return super.mouseScrolled(mouseX, mouseY, amountX, amountY) || creditsList.mouseScrolled(mouseX, mouseY, amountX, amountY);
	}

	private class CreditsList extends ContainerObjectSelectionList<Credit> {

		public CreditsList(Minecraft minecraftClient, int width, int height, int top,
						   int entryHeight) {
			super(minecraftClient, width, height, top, entryHeight);
			this.setRenderHeader(false, 0);

			for (Credit c : credits) {
				addEntry(c);
			}
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput builder) {
			builder.add(NarratedElementType.TITLE, "credits");
			super.updateWidgetNarration(builder);
			if (creditOverlay != null) {
				builder.add(NarratedElementType.TITLE, creditOverlay.credit.name);
				StringBuilder cs = new StringBuilder();
				for (String s : creditOverlay.credit.things) {
					cs.append(s).append(". ");
				}
				builder.add(NarratedElementType.HINT, cs.toString());
			}
		}

		@Override
		public int getRowLeft() {
			return width / 2;
		}
	}

	private class Credit extends ContainerObjectSelectionList.Entry<Credit> {

		private final String name;
		private final String[] things;
		private final Button c;
		private boolean hovered;

		public Credit(String name, String... things) {
			this.name = name;
			this.things = things;
			c = Button.builder(Component.literal(name), buttonWidget -> creditOverlay = new Overlay(this)).bounds(-2, -2, 1, 1).build();
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			if (hovered || c.isFocused()) {
				RenderUtil.drawOutline(graphics, x - 100, y, 200, 20, ClientColors.ERROR.toInt());
			}
			this.hovered = hovered;
			DrawUtil.drawCenteredString(graphics, font, name, x, y + 5,
				hovered || c.isFocused() ? ClientColors.SELECTOR_RED.toInt()
					: -1,
				true);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(c);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (hovered) {
				creditOverlay = new Overlay(this);
				return true;
			}
			return false;
		}

		@Nullable
		@Override
		public GuiEventListener getFocused() {
			if (super.getFocused() == null) {
				setFocused(c);
			}
			return super.getFocused();
		}

		@Override
		@NotNull
		public List<? extends NarratableEntry> narratables() {
			return List.of(c);
		}
	}

	private class Overlay {

		protected final HashMap<String, ClickEvent> effects = new HashMap<>();
		protected final HashMap<Integer, String> lines = new HashMap<>();
		private final Credit credit;
		private final int x;
		private final int y;
		private Window window;
		private int width;
		private int height;

		public Overlay(Credit credit) {
			x = 100;
			y = 50;
			this.credit = credit;

			init();
		}

		public void init() {
			window = minecraft.getWindow();
			this.width = window.getGuiScaledWidth() - 200;
			this.height = window.getGuiScaledHeight() - 100;

			int startY = y + 50;
			for (String t : credit.things) {
				if (t.startsWith("http")) {
					effects.put(t, new ClickEvent(ClickEvent.Action.OPEN_URL, t));
					lines.put(startY, ChatFormatting.UNDERLINE + t);
				} else {
					lines.put(startY, t);
				}
				startY += 12;
			}
		}

		public void render(GuiGraphics graphics) {
			RenderUtil.drawRectangle(graphics, x, y, width, height,
				ClientColors.DARK_GRAY.withAlpha(127));
			DrawUtil.outlineRect(graphics, x, y, width, height,
				ClientColors.BLACK.toInt());

			DrawUtil.drawCenteredString(graphics, font, credit.name,
				window.getGuiScaledWidth() / 2, y + 7, -16784327, true);

			lines.forEach(
				(integer, s) -> DrawUtil.drawCenteredString(graphics, font, s,
					x + width / 2, integer, ClientColors.SELECTOR_GREEN.toInt(), true));
		}

		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}

		public void mouseClicked(double mouseX, double mouseY) {
			lines.forEach((integer, s) -> {
				if ((mouseY >= integer && mouseY < integer + 11)
					&& mouseX >= x + width / 2F - font.width(s) / 2F
					&& mouseX <= x + width / 2F + font.width(s) / 2F) {
					handleComponentClicked(Style.EMPTY.withClickEvent(effects.get(ChatFormatting.stripFormatting(s))));
				}
			});
		}
	}

	private class SpacerTitle extends Credit {

		public SpacerTitle(String name) {
			super(name, "");
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			DrawUtil.drawCenteredString(graphics, font, super.name, x, y, -128374,
				true);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		@NotNull
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}
	}
}
