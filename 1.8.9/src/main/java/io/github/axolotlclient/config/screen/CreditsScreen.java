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
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.credits.Credits;
import io.github.axolotlclient.mixin.SoundManagerAccessor;
import io.github.axolotlclient.mixin.SoundSystemAccessor;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Window;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.resource.Identifier;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import paulscode.sound.SoundSystem;

public class CreditsScreen extends Screen {

	public static final HashMap<String, String[]> externalModuleCredits = new HashMap<>();
	private final Screen parent;
	private final List<Credit> credits = new ArrayList<>();
	private final SoundInstance bgm = SimpleSoundInstance.of(new Identifier("minecraft", "records.chirp"));
	private Overlay creditOverlay;
	private EntryListWidget creditsList;

	public CreditsScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		if (AxolotlClient.CONFIG.creditsBGM.get() && !minecraft.getSoundManager().isPlaying(bgm)) {
			if (((SoundSystemAccessor) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager())
				.getSoundSystem()).getChannelsByEvent().get(bgm) == null) {
				Minecraft.getInstance().getSoundManager().play(bgm);
			}
		}

		renderBackground();
		if (AxolotlClient.someNiceBackground.get()) { // Credit to pridelib for the colors
			DrawUtil.fill(0, 0, width, height / 6, 0xFFff0018);
			DrawUtil.fill(0, height / 6, width, height * 2 / 6, 0xFFffa52c);
			DrawUtil.fill(0, height * 2 / 6, width, height / 2, 0xFFffff41);
			DrawUtil.fill(0, height * 2 / 3, width, height * 5 / 6, 0xFF0000f9);
			DrawUtil.fill(0, height / 2, width, height * 2 / 3, 0xFF008018);
			DrawUtil.fill(0, height * 5 / 6, width, height, 0xFF86007d);
		}

		GlStateManager.enableAlphaTest();
		super.render(mouseX, mouseY, tickDelta);
		GlStateManager.disableAlphaTest();

		drawCenteredString(this.textRenderer, I18n.translate("credits"), width / 2, 20, -1);

		if (creditOverlay != null) {
			creditOverlay.render();
		} else {
			creditsList.render(mouseX, mouseY, tickDelta);
		}
	}

	@Override
	protected void keyPressed(char character, int code) {
		if (code == 1) {
			stopBGM();
			this.minecraft.openScreen(null);
			if (this.minecraft.screen == null) {
				this.minecraft.closeScreen();
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
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
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		creditsList.mouseReleased(mouseX, mouseY, button);
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		if (button.id == 0) {
			if (creditOverlay == null) {
				minecraft.openScreen(parent);
				stopBGM();
			} else {
				creditOverlay = null;
			}
		} else if (button.id == 1) {
			AxolotlClient.CONFIG.creditsBGM.toggle();
			AxolotlClient.configManager.save();
			stopBGM();
			button.message = I18n.translate("creditsBGM") + ": "
				+ I18n.translate(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off");
		}
	}

	@Override
	public void init() {
		this.buttons.add(
			new ButtonWidget(0, width / 2 - 75, height - 50 + 22, 150, 20, I18n.translate("back")));

		this.buttons.add(new ButtonWidget(1, 6, this.height - 26, 100, 20, I18n.translate("creditsBGM")
			+ ": " + I18n.translate(AxolotlClient.CONFIG.creditsBGM.get() ? "options.on" : "options.off")));

		credits.clear();
		initCredits();

		creditsList = new EntryListWidget(minecraft, width, height, 50, height - 50, 25) {

			@Override
			protected int size() {
				return credits.size();
			}

			@Override
			public void render(int mouseX, int mouseY, float delta) {
				if (this.visible) {
					GlStateManager.enableDepthTest();
					GlStateManager.pushMatrix();
					GlStateManager.translatef(0, 0, 1F);
					this.mouseX = mouseX;
					this.mouseY = mouseY;
					int i = this.getScrollbarPosition();
					int j = i + 6;
					this.capScrolling();
					GlStateManager.disableLighting();
					GlStateManager.disableFog();
					int k = this.width / 2;
					int l = this.minY + 4 - (int) this.scrollAmount;

					int n = this.getMaxScroll();
					if (n > 0) {
						int o = (this.maxY - this.minY) * (this.maxY - this.minY) / this.getHeight();
						o = MathHelper.clamp(o, 32, this.maxY - this.minY - 8);
						int p = (int) this.scrollAmount * (this.maxY - this.minY - o) / n + this.minY;
						if (p < this.minY) {
							p = this.minY;
						}
						GlStateManager.disableTexture();
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder bufferBuilder = tessellator.getBuilder();

						bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
						bufferBuilder.vertex(i, this.maxY, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).nextVertex();
						bufferBuilder.vertex(j, this.maxY, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).nextVertex();
						bufferBuilder.vertex(j, this.minY, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).nextVertex();
						bufferBuilder.vertex(i, this.minY, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).nextVertex();
						tessellator.end();
						bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
						bufferBuilder.vertex(i, (p + o), 0.0).texture(0.0, 1.0).color(128, 128, 128, 255).nextVertex();
						bufferBuilder.vertex(j, (p + o), 0.0).texture(1.0, 1.0).color(128, 128, 128, 255).nextVertex();
						bufferBuilder.vertex(j, p, 0.0).texture(1.0, 0.0).color(128, 128, 128, 255).nextVertex();
						bufferBuilder.vertex(i, p, 0.0).texture(0.0, 0.0).color(128, 128, 128, 255).nextVertex();
						tessellator.end();
						bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
						bufferBuilder.vertex(i, (p + o - 1), 0.0).texture(0.0, 1.0).color(192, 192, 192, 255).nextVertex();
						bufferBuilder.vertex((j - 1), (p + o - 1), 0.0).texture(1.0, 1.0).color(192, 192, 192, 255)
							.nextVertex();
						bufferBuilder.vertex((j - 1), p, 0.0).texture(1.0, 0.0).color(192, 192, 192, 255).nextVertex();
						bufferBuilder.vertex(i, p, 0.0).texture(0.0, 0.0).color(192, 192, 192, 255).nextVertex();
						tessellator.end();
					}
					GlStateManager.enableTexture();

					this.renderList(k, l, mouseX, mouseY);

					GlStateManager.shadeModel(7424);
					GlStateManager.enableAlphaTest();
					GlStateManager.popMatrix();
					GlStateManager.disableBlend();
				}
			}

			@Override
			protected void renderList(int x, int y, int mouseX, int mouseY) {
				Util.applyScissor(0, minY, this.width, maxY - minY);
				super.renderList(x, y, mouseX, mouseY);
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}

			@Override
			public Entry getEntry(int index) {
				return credits.get(index);
			}
		};
	}

	private void initCredits() {
		credits.add(new SpacerTitle("- - - - - - " + I18n.translate("contributors") + " - - - - - -"));

		Credits.getContributors().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		credits.add(new SpacerTitle("- - - - - - " + I18n.translate("other_people") + " - - - - - -"));

		Credits.getOtherPeople().forEach(credit -> credits.add(new Credit(credit.getName(), credit.getThings())));

		if (!externalModuleCredits.isEmpty()) {
			credits.add(new SpacerTitle(
				"- - - - - - " + I18n.translate("external_modules") + " - - - - - -"));
			externalModuleCredits.forEach((s, s2) -> {
				credits.add(new Credit(s, s2));
			});
		}
	}

	@Override
	public void handleMouse() {
		creditsList.handleMouse();
		super.handleMouse();
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		if (creditOverlay != null)
			creditOverlay.init();
		super.resize(client, width, height);
	}

	private void stopBGM() {
		if (((SoundSystemAccessor) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager())
			.getSoundSystem()).getChannelsByEvent().get(bgm) != null) {
			((SoundSystem) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager())
				.getSoundSystem().system)
				.stop(((SoundSystemAccessor) ((SoundManagerAccessor) Minecraft.getInstance()
					.getSoundManager()).getSoundSystem()).getChannelsByEvent().get(bgm));
			((SoundSystem) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager())
				.getSoundSystem().system)
				.removeSource(((SoundSystemAccessor) ((SoundManagerAccessor) Minecraft.getInstance()
					.getSoundManager()).getSoundSystem()).getChannelsByEvent().get(bgm));
		}
	}

	private class Credit extends DrawUtil implements EntryListWidget.Entry {

		private final String name;
		private final String[] things;

		private boolean hovered;

		public Credit(String name, String... things) {
			this.name = name;
			this.things = things;
		}

		@Override
		public void renderOutOfBounds(int index, int x, int y) {
		}

		@Override
		public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY,
						   boolean hovered) {
			if (hovered) {
				drawVerticalLine(x - 100, y, y + 20,
					ClientColors.ERROR.toInt());
				drawVerticalLine(x + 100, y, y + 20,
					ClientColors.ERROR.toInt());
				drawHorizontalLine(x - 100, x + 100, y + 20,
					ClientColors.ERROR.toInt());
				drawHorizontalLine(x - 100, x + 100, y,
					ClientColors.ERROR.toInt());
			}
			this.hovered = hovered;
			drawCenteredString(Minecraft.getInstance().textRenderer, name, x, y + 5,
				hovered ? ClientColors.SELECTOR_RED.toInt() : -1);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			if (hovered) {
				creditOverlay = new Overlay(this);
				return true;
			}
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {
		}
	}

	private class Overlay extends DrawUtil {

		protected final HashMap<String, ClickEvent> effects = new HashMap<>();
		protected final HashMap<Integer, String> lines = new HashMap<>();
		private final Credit credit;
		private final int x;
		private final int y;
		private final Color DARK_GRAY = ClientColors.DARK_GRAY.withAlpha(127);
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
			window = new Window(Minecraft.getInstance());
			width = window.getWidth() - 200;
			height = window.getHeight() - 100;

			int startY = y + 50;
			for (String t : credit.things) {
				while (t.contains("\n")) {
					startY += 12;
					t = t.replace("\n", "");
				}

				if (t.startsWith("http")) {
					effects.put(t, new ClickEvent(ClickEvent.Action.OPEN_URL, t));
					lines.put(startY, Formatting.UNDERLINE + t);
				} else {
					lines.put(startY, t);
				}
				startY += 12;
			}
		}

		public void render() {
			RenderUtil.drawRectangle(x, y, width, height,
				DARK_GRAY);
			DrawUtil.outlineRect(x, y, width, height,
				ClientColors.BLACK.toInt());

			drawCenteredString(Minecraft.getInstance().textRenderer, credit.name, window.getWidth() / 2, y + 7,
				-16784327);

			lines.forEach(
				(integer, s) -> drawCenteredString(Minecraft.getInstance().textRenderer, s, x + width / 2,
					integer, ClientColors.SELECTOR_GREEN.toInt()));
		}

		public boolean isMouseOver(int mouseX, int mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}

		public void mouseClicked(int mouseX, int mouseY) {
			lines.forEach((integer, s) -> {
				if ((mouseY >= integer && mouseY < integer + 11)
					&& mouseX >= x + width / 2 - Minecraft.getInstance().textRenderer.getWidth(s) / 2
					&& mouseX <= x + width / 2 + Minecraft.getInstance().textRenderer.getWidth(s) / 2) {
					m_9528629(
						new LiteralText(s).setStyle(new Style().setClickEvent(effects.get(Formatting.strip(s)))));
				}
			});
		}
	}

	private class SpacerTitle extends Credit {

		public SpacerTitle(String name) {
			super(name, "");
		}

		@Override
		public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY,
						   boolean hovered) {
			drawCenteredString(Minecraft.getInstance().textRenderer, super.name, x, y, -128374);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
			return false;
		}
	}
}
