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

package io.github.axolotlclient.modules.screenshotUtils;

import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.SpriteButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.LayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DownloadImageScreen extends Screen {
	private static final Identifier SPRITE = Identifier.of("axolotlclient", "go");

	private final Screen parent;

	public DownloadImageScreen(Screen parent) {
		super(Text.translatable("viewScreenshot"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		var hFL = new ImprovedHeaderAndFooterLayout(this);

		hFL.addTitleHeader(getTitle(), textRenderer);
		var urlBox = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.translatable("urlBox"));
		urlBox.setSuggestion(I18n.translate("pasteURL"));
		urlBox.setChangedListener(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.translate("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		urlBox.setMaxLength(52);
		var linear = hFL.addToContents(LinearLayoutWidget.createHorizontal().setSpacing(4));
		linear.copyDefaultSettings().alignVerticallyCenter();
		linear.add(urlBox);
		linear.add(SpriteButtonWidget.builder(Text.translatable("download"), b -> {
				String url = urlBox.getText().trim();
				if (url.isEmpty()) {
					return;
				}
				client.setScreen(ImageScreen.create(this, ImageShare.getInstance().downloadImage(url), true));
			}, true)
			.sprite(SPRITE, 20, 20)
			.width(20).build()).setPosition(width / 2 + 100 + 4, height / 2 - 10);

		hFL.addToFooter(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).build());

		hFL.arrangeElements();
		hFL.visitWidgets(this::addDrawableSelectableElement);
		setInitialFocus(urlBox);
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
	}


	public static class ImprovedHeaderAndFooterLayout implements LayoutWidget {
		public static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 33;
		private static final int CONTENT_MARGIN_TOP = 30;
		private final FrameWidget headerFrame = new FrameWidget();
		private final FrameWidget footerFrame = new FrameWidget();
		private final FrameWidget contentsFrame = new FrameWidget();
		private final Screen screen;
		@Getter
		@Setter
		private int headerHeight;
		@Setter
		@Getter
		private int footerHeight;

		public ImprovedHeaderAndFooterLayout(Screen screen) {
			this(screen, DEFAULT_HEADER_AND_FOOTER_HEIGHT);
		}

		public ImprovedHeaderAndFooterLayout(Screen screen, int height) {
			this(screen, height, height);
		}

		public ImprovedHeaderAndFooterLayout(Screen screen, int headerHeight, int footerHeight) {
			this.screen = screen;
			this.headerHeight = headerHeight;
			this.footerHeight = footerHeight;
			this.headerFrame.copyDefaultSettings().setAlignment(0.5F, 0.5F);
			this.footerFrame.copyDefaultSettings().setAlignment(0.5F, 0.5F);
		}

		@Override
		public void setX(int x) {
		}

		@Override
		public void setY(int y) {
		}

		@Override
		public int getX() {
			return 0;
		}

		@Override
		public int getY() {
			return 0;
		}

		@Override
		public int getWidth() {
			return this.screen.width;
		}

		@Override
		public int getHeight() {
			return this.screen.height;
		}

		public int getContentHeight() {
			return this.screen.height - this.getHeaderHeight() - this.getFooterHeight();
		}

		@Override
		public void visitChildren(Consumer<Widget> visitor) {
			this.headerFrame.visitChildren(visitor);
			this.contentsFrame.visitChildren(visitor);
			this.footerFrame.visitChildren(visitor);
		}

		@Override
		public void arrangeElements() {
			int i = this.getHeaderHeight();
			int j = this.getFooterHeight();
			this.headerFrame.setMinWidth(this.screen.width);
			this.headerFrame.setMinHeight(i);
			this.headerFrame.setPosition(0, 0);
			this.headerFrame.arrangeElements();
			this.footerFrame.setMinWidth(this.screen.width);
			this.footerFrame.setMinHeight(j);
			this.footerFrame.arrangeElements();
			this.footerFrame.setY(this.screen.height - j);
			contentsFrame.setMinHeight(getContentHeight()); // This line makes it possible to center things vertically in this layout. Thanks mojang.
			this.contentsFrame.setMinWidth(this.screen.width);
			this.contentsFrame.arrangeElements();
			int k = i + CONTENT_MARGIN_TOP;
			int l = this.screen.height - j - this.contentsFrame.getHeight();
			this.contentsFrame.setPosition(0, Math.min(k, l));
		}

		public void addTitleHeader(Text message, TextRenderer font) {
			this.headerFrame.add(new TextWidget(message, font));
		}

		public <T extends Widget> T addToFooter(T child) {
			return this.footerFrame.add(child);
		}

		public <T extends Widget> T addToContents(T child) {
			return this.contentsFrame.add(child);
		}
	}
}
