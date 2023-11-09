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

package io.github.axolotlclient.config.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class CreditsScreen extends Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final List<Credit> credits = new ArrayList<>();
	private final SoundInstance bgm = PositionedSoundInstance.master(SoundEvents.MUSIC_DISC_CHIRP, 1, 1);
	private Overlay creditOverlay;
	private EntryListWidget<Credit> creditsList;

	public CreditsScreen(Screen parent) {
		super(new TranslatableText("credits"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
		if (AxolotlClient.CONFIG.creditsBGM.get() && !MinecraftClient.getInstance().getSoundManager().isPlaying(bgm)) {
			MinecraftClient.getInstance().getSoundManager().play(bgm);
		}

		renderBackground(matrices);
		if (AxolotlClient.someNiceBackground.get()) { // Credit to pridelib for the colors
			DrawUtil.fill(matrices, 0, 0, width, height / 6, 0xFFff0018);
			DrawUtil.fill(matrices, 0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			DrawUtil.fill(matrices, 0, height * 2 / 6, width, height / 2, 0xFFffff41);
			DrawUtil.fill(matrices, 0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			DrawUtil.fill(matrices, 0, height / 2, width, height * 2 / 3, 0xFF008018);
			DrawUtil.fill(matrices, 0, height * 5 / 6, width, height, 0xFF86007d);
		}

		super.render(matrices, mouseX, mouseY, tickDelta);

		DrawUtil.drawCenteredString(matrices, this.textRenderer, I18n.translate("credits"), width / 2, 20, -1, true);

		if (creditOverlay != null) {
			creditOverlay.render(matrices);
		} else {
			creditsList.render(matrices, mouseX, mouseY, tickDelta);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (creditOverlay == null) {
				MinecraftClient.getInstance().openScreen(parent);
			} else {
				creditOverlay = null;
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public List<? extends Element> children() {
		if (CreditsScreen.this.creditOverlay != null) {
			List<? extends Element> l = new ArrayList<>(super.children());
			l.remove(creditsList);
			return l;
		}
		return super.children();
	}

	@Override
	public void init() {
		credits.clear();
		initCredits();

		creditsList = new CreditsList(client, width, height, 50, height - 50, 25);
		addChild(creditsList);

		this.addButton(
			new ButtonWidget(width / 2 - 75, height - 50 + 22, 150, 20, ScreenTexts.BACK, buttonWidget -> {
				if (creditOverlay == null) {
					MinecraftClient.getInstance().openScreen(parent);
					stopBGM();
				} else {
					creditOverlay = null;
				}
			}));

		this.addButton(new ButtonWidget(6, this.height - 26, 100, 20,
			new TranslatableText("creditsBGM").append(": ").append(
				new TranslatableText(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")),
			buttonWidget -> {
				AxolotlClient.CONFIG.creditsBGM.toggle();
				AxolotlClient.configManager.save();
				stopBGM();
				buttonWidget.setMessage(new TranslatableText("creditsBGM").append(": ").append(
					new TranslatableText(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")));
			}));
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

			this.setRenderHeader(false, 0);

			for (Credit c : credits) {
				addEntry(c);
			}
		}

		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			this.renderBackground(matrices);
			int i = this.getScrollbarPositionX();
			int j = i + 6;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.getBuffer();
			int k = this.getRowLeft();
			int l = this.top + 4 - (int) this.getScrollAmount();
			this.renderList(matrices, k, l, mouseX, mouseY, delta);
			RenderSystem.disableAlphaTest();
			RenderSystem.shadeModel(7425);
			RenderSystem.disableTexture();
			int o = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
			if (o > 0) {
				int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getMaxPosition());
				p = MathHelper.clamp(p, 32, this.bottom - this.top - 8);
				int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / o + this.top;
				if (q < this.top) {
					q = this.top;
				}

				bufferBuilder.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
				bufferBuilder.vertex(i, this.bottom, 0.0).color(0, 0, 0, 255).texture(0.0F, 1.0F).next();
				bufferBuilder.vertex(j, this.bottom, 0.0).color(0, 0, 0, 255).texture(1.0F, 1.0F).next();
				bufferBuilder.vertex(j, this.top, 0.0).color(0, 0, 0, 255).texture(1.0F, 0.0F).next();
				bufferBuilder.vertex(i, this.top, 0.0).color(0, 0, 0, 255).texture(0.0F, 0.0F).next();
				bufferBuilder.vertex(i, (q + p), 0.0).color(128, 128, 128, 255).texture(0.0F, 1.0F).next();
				bufferBuilder.vertex(j, (q + p), 0.0).color(128, 128, 128, 255).texture(1.0F, 1.0F).next();
				bufferBuilder.vertex(j, q, 0.0).color(128, 128, 128, 255).texture(1.0F, 0.0F).next();
				bufferBuilder.vertex(i, q, 0.0).color(128, 128, 128, 255).texture(0.0F, 0.0F).next();
				bufferBuilder.vertex(i, (q + p - 1), 0.0).color(192, 192, 192, 255).texture(0.0F, 1.0F).next();
				bufferBuilder.vertex((j - 1), (q + p - 1), 0.0).color(192, 192, 192, 255).texture(1.0F, 1.0F).next();
				bufferBuilder.vertex((j - 1), q, 0.0).color(192, 192, 192, 255).texture(1.0F, 0.0F).next();
				bufferBuilder.vertex(i, q, 0.0).color(192, 192, 192, 255).texture(0.0F, 0.0F).next();
				tessellator.draw();
			}

			RenderSystem.enableTexture();
			RenderSystem.shadeModel(7424);
			RenderSystem.enableAlphaTest();
			RenderSystem.disableBlend();
		}


        /*@Override
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
        }*/

		@Override
		protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
			Util.applyScissor(0, top, this.width, bottom - top);
			super.renderList(matrices, x, y, mouseX, mouseY, delta);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}

		@Override
		public int getRowLeft() {
			return width / 2;
		}

		@Override
		public boolean changeFocus(boolean lookForwards) {
			if (creditOverlay != null) {
				return false;
			}
			return super.changeFocus(lookForwards);
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
			c = new ButtonWidget(-2, -2, 1, 1, Text.of(name), buttonWidget -> creditOverlay = new Overlay(this));
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			if (hovered || c.isFocused()) {
				drawVerticalLine(matrices, x - 100, y, y + 20,
					io.github.axolotlclient.AxolotlClientConfig.Color.ERROR.getAsInt());
				drawVerticalLine(matrices, x + 100, y, y + 20,
					io.github.axolotlclient.AxolotlClientConfig.Color.ERROR.getAsInt());
				drawHorizontalLine(matrices, x - 100, x + 100, y + 20,
					io.github.axolotlclient.AxolotlClientConfig.Color.ERROR.getAsInt());
				drawHorizontalLine(matrices, x - 100, x + 100, y,
					io.github.axolotlclient.AxolotlClientConfig.Color.ERROR.getAsInt());
			}
			this.hovered = hovered;
			DrawUtil.drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, name, x, y + 5,
				hovered || c.isFocused() ? io.github.axolotlclient.AxolotlClientConfig.Color.SELECTOR_RED.getAsInt()
					: -1,
				true);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.singletonList(c);
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
				setFocused(c);
			}
			return super.getFocused();
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

		public void render(MatrixStack matrices) {
			RenderUtil.drawRectangle(matrices, x, y, width, height,
				io.github.axolotlclient.AxolotlClientConfig.Color.DARK_GRAY.withAlpha(127));
			DrawUtil.outlineRect(matrices, x, y, width, height,
				io.github.axolotlclient.AxolotlClientConfig.Color.BLACK.getAsInt());

			DrawUtil.drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, credit.name,
				window.getScaledWidth() / 2, y + 7, -16784327, true);

			lines.forEach(
				(integer, s) -> DrawUtil.drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, s,
					x + width / 2, integer, Color.SELECTOR_GREEN.getAsInt(), true));
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
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
						   int mouseY, boolean hovered, float tickDelta) {
			DrawUtil.drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, super.name, x, y, -128374,
				true);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		public boolean changeFocus(boolean lookForwards) {
			return false;
		}
	}
}
