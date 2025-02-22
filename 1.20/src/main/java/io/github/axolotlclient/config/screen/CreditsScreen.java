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

import com.mojang.blaze3d.glfw.Window;
import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class CreditsScreen extends Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final List<Credit> credits = new ArrayList<>();
	private final SoundInstance bgm = PositionedSoundInstance.master(SoundEvents.MUSIC_DISC_CHIRP, 1, 1);
	private Overlay creditOverlay;
	private EntryListWidget<Credit> creditsList;

	public CreditsScreen(Screen parent) {
		super(Text.translatable("credits"));
		this.parent = parent;
	}

	@Override
	public void renderBackground(GuiGraphics graphics) {
		if (AxolotlClient.someNiceBackground.get()) { // Credit to pridelib for the colors
			graphics.fill(0, 0, width, height / 6, 0xFFff0018);
			graphics.fill(0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			graphics.fill(0, height * 2 / 6, width, height / 2, 0xFFffff41);
			graphics.fill(0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			graphics.fill(0, height / 2, width, height * 2 / 3, 0xFF008018);
			graphics.fill(0, height * 5 / 6, width, height, 0xFF86007d);
		} else {
			super.renderBackground(graphics);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (AxolotlClient.CONFIG.creditsBGM.get() && !MinecraftClient.getInstance().getSoundManager().isPlaying(bgm)) {
			MinecraftClient.getInstance().getSoundManager().play(bgm);
		}

		renderBackground(graphics);
		if (creditOverlay == null) {
			creditsList.render(graphics, mouseX, mouseY, tickDelta);
		}
		super.render(graphics, mouseX, mouseY, tickDelta);

		DrawUtil.drawCenteredString(graphics, this.textRenderer, I18n.translate("credits"), width / 2, 20, -1, true);

		if (creditOverlay != null) {
			creditOverlay.render(graphics);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputUtil.KEY_ESCAPE_CODE) {
			if (creditOverlay == null) {
				MinecraftClient.getInstance().setScreen(parent);
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

		creditsList = new CreditsList(client, width, height, 50, height - 50, 25);
		addSelectableChild(creditsList);

		this.addDrawableChild(new ButtonWidget.Builder(CommonTexts.BACK, buttonWidget -> {
			if (creditOverlay == null) {
				MinecraftClient.getInstance().setScreen(parent);
				stopBGM();
			} else {
				creditOverlay = null;
			}
		}).positionAndSize(width / 2 - 75, height - 50 + 22, 150, 20).build());

		this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("creditsBGM").append(": ")
			.append(Text.translatable(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")),
			buttonWidget -> {
				AxolotlClient.CONFIG.creditsBGM.toggle();
				AxolotlClient.configManager.save();
				stopBGM();
				buttonWidget.setMessage(Text.translatable("creditsBGM").append(": ").append(
					Text.translatable(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")));
			}).positionAndSize(6, this.height - 26, 100, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		if (creditOverlay != null)
			creditOverlay.init();
		super.resize(client, width, height);
	}

	private void initCredits() {
		credits.add(new SpacerTitle("- - - - - - " + I18n.translate("contributors") + " - - - - - -"));

		Credits.getContributors().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		credits.add(new SpacerTitle("- - - - - - " + I18n.translate("other_people") + " - - - - - -"));

		Credits.getOtherPeople().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		if (!externalModuleCredits.isEmpty()) {
			credits.add(new SpacerTitle("- - - - - - " + I18n.translate("external_modules") + " - - - - - -"));
			externalModuleCredits.forEach((s, s2) -> credits.add(new Credit(s, s2)));
		}
	}

	private void stopBGM() {
		MinecraftClient.getInstance().getSoundManager().stop(bgm);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return super.mouseScrolled(mouseX, mouseY, amount) || creditsList.mouseScrolled(mouseX, mouseY, amount);
	}

	private class CreditsList extends ElementListWidget<Credit> {

		public CreditsList(MinecraftClient minecraftClient, int width, int height, int top, int bottom,
						   int entryHeight) {
			super(minecraftClient, width, height, top, bottom, entryHeight);

			this.setRenderBackground(false);
			this.setRenderHeader(false, 0);

			for (Credit c : credits) {
				addEntry(c);
			}
		}

		@Override
		public void appendNarrations(NarrationMessageBuilder builder) {
			builder.put(NarrationPart.TITLE, "credits");
			super.appendNarrations(builder);
			if (creditOverlay != null) {
				builder.put(NarrationPart.TITLE, creditOverlay.credit.name);
				StringBuilder cs = new StringBuilder();
				for (String s : creditOverlay.credit.things) {
					cs.append(s).append(". ");
				}
				builder.put(NarrationPart.HINT, cs.toString());
			}
		}

		@Override
		public int getRowLeft() {
			return width / 2;
		}
	}

	private class Credit extends ElementListWidget.Entry<Credit> {

		private final String name;
		private final String[] things;
		private final ButtonWidget c;
		private boolean hovered;

		public Credit(String name, String... things) {
			this.name = name;
			this.things = things;
			c = ButtonWidget.builder(Text.of(name), buttonWidget -> creditOverlay = new Overlay(this)).positionAndSize(-2, -2, 1, 1).build();
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			if (hovered || c.isFocused()) {
				RenderUtil.drawOutline(graphics, x - 100, y, 200, 20, ClientColors.ERROR.toInt());
			}
			this.hovered = hovered;
			DrawUtil.drawCenteredString(graphics, MinecraftClient.getInstance().textRenderer, name, x, y + 5,
				hovered || c.isFocused() ? ClientColors.SELECTOR_RED.toInt()
					: -1,
				true);
		}

		@Override
		public List<? extends Element> children() {
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
		public Element getFocused() {
			if (super.getFocused() == null) {
				setFocusedChild(c);
			}
			return super.getFocused();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
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
			window = MinecraftClient.getInstance().getWindow();
			this.width = window.getScaledWidth() - 200;
			this.height = window.getScaledHeight() - 100;

			int startY = y + 50;
			for (String t : credit.things) {
				if (t.startsWith("http")) {
					effects.put(t, new ClickEvent(ClickEvent.Action.OPEN_URL, t));
					lines.put(startY, Formatting.UNDERLINE + t);
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

			DrawUtil.drawCenteredString(graphics, MinecraftClient.getInstance().textRenderer, credit.name,
				window.getScaledWidth() / 2, y + 7, -16784327, true);

			lines.forEach(
				(integer, s) -> DrawUtil.drawCenteredString(graphics, MinecraftClient.getInstance().textRenderer, s,
					x + width / 2, integer, ClientColors.SELECTOR_GREEN.toInt(), true));
		}

		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}

		public void mouseClicked(double mouseX, double mouseY) {
			lines.forEach((integer, s) -> {
				if ((mouseY >= integer && mouseY < integer + 11)
					&& mouseX >= x + width / 2F - MinecraftClient.getInstance().textRenderer.getWidth(s) / 2F
					&& mouseX <= x + width / 2F + MinecraftClient.getInstance().textRenderer.getWidth(s) / 2F) {
					handleTextClick(Style.EMPTY.withClickEvent(effects.get(Formatting.strip(s))));
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
			DrawUtil.drawCenteredString(graphics, MinecraftClient.getInstance().textRenderer, super.name, x, y, -128374,
				true);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}
	}
}
