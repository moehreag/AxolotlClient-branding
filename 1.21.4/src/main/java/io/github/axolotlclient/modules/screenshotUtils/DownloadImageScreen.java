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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DownloadImageScreen extends Screen {
	private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("axolotlclient", "go");

	private final Screen parent;

	public DownloadImageScreen(Screen parent) {
		super(Component.translatable("viewScreenshot"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		var hFL = new ImprovedHeaderAndFooterLayout(this);

		hFL.addTitleHeader(getTitle(), font);
		var urlBox = new EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, Component.translatable("urlBox"));
		urlBox.setSuggestion(I18n.get("pasteURL"));
		urlBox.setResponder(s -> {
			if (s.isEmpty()) {
				urlBox.setSuggestion(I18n.get("pasteURL"));
			} else {
				urlBox.setSuggestion("");
			}
		});
		urlBox.setMaxLength(63);
		var linear = hFL.addToContents(LinearLayout.horizontal().spacing(4));
		linear.defaultCellSetting().alignVerticallyMiddle();
		linear.addChild(urlBox);
		linear.addChild(SpriteIconButton.builder(Component.translatable("download"), b -> {
				String url = urlBox.getValue().trim();
				if (url.isEmpty()) {
					return;
				}
				minecraft.setScreen(ImageScreen.create(this, ImageShare.getInstance().downloadImage(url), true));
			}, true)
			.sprite(SPRITE, 20, 20)
			.width(20).build()).setPosition(width / 2 + 100 + 4, height / 2 - 10);

		hFL.addToFooter(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).build());

		hFL.arrangeElements();
		hFL.visitWidgets(this::addRenderableWidget);
		setInitialFocus(urlBox);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}


	public static class ImprovedHeaderAndFooterLayout implements Layout {
		public static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 33;
		private static final int CONTENT_MARGIN_TOP = 30;
		private final FrameLayout headerFrame = new FrameLayout();
		private final FrameLayout footerFrame = new FrameLayout();
		private final FrameLayout contentsFrame = new FrameLayout();
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
			this.headerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
			this.footerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
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
		public void visitChildren(Consumer<LayoutElement> visitor) {
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

		public void addTitleHeader(Component message, Font font) {
			this.headerFrame.addChild(new StringWidget(message, font));
		}

		public <T extends LayoutElement> T addToFooter(T child) {
			return this.footerFrame.addChild(child);
		}

		public <T extends LayoutElement> T addToContents(T child) {
			return this.contentsFrame.addChild(child);
		}
	}
}
