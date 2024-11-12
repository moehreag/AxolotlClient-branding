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

package io.github.axolotlclient.api.chat;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ClickableWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.SliderWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Persistence;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;

public class CreateChannelScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {
	private final Screen parent;
	private String tooltip;
	private Element tooltipElement;

	protected CreateChannelScreen(Screen parent) {
		super(I18n.translate("api.chat.groups.create"));
		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, title, width / 2, 36 / 2 - textRenderer.fontHeight / 2, -1);


		if (tooltip != null) {
			tooltipElement = hoveredElement(mouseX, mouseY).orElse(null);
			renderTooltip(textRenderer.split(tooltip, 170), mouseX, mouseY);
		}
		if (hoveredElement(mouseX, mouseY).map(e -> e != tooltipElement).orElse(true)) {
			tooltip = null;
		}
	}

	@Override
	public void init() {
		int leftColX = width / 2 - 4 - 150;
		int leftColYStep = textRenderer.fontHeight + 20 - textRenderer.fontHeight + 8 - 5 + 5;
		int leftColY = 36 + 5 + 30;
		int rightColX = width / 2 + 4;
		int rightColYStep = 20 + 8;
		int rightColY = 36 + 30;

		var nameField = new io.github.axolotlclient.AxolotlClientConfig.impl.ui.TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, "");
		addDrawableChild(text("api.chat.groups.name", leftColX, leftColY));
		addDrawableChild(nameField);
		rightColY += rightColYStep;
		leftColY += leftColYStep;

		AtomicReference<Supplier<Integer>> count = new AtomicReference<>();
		AtomicReference<Supplier<Long>> duration = new AtomicReference<>();

		AtomicReference<Consumer<Boolean>> countDisabler = new AtomicReference<>();
		AtomicReference<Consumer<Boolean>> durationDisabler = new AtomicReference<>();
		var persistence = new VanillaButtonWidget(rightColX, rightColY, 150, 20, "", a -> {
		}) {
			final Persistence.Type[] persistenceValues = Persistence.Type.values();
			int current = 0;

			@Override
			public void drawWidget(int mouseX, int mouseY, float delta) {
				setMessage(I18n.translate("api.chat.persistence." + persistenceValues[current].getId()));
				super.drawWidget(mouseX, mouseY, delta);
			}

			@Override
			public void onPress() {
				current++;
				if (current >= persistenceValues.length) {
					current = 0;
				}
				Persistence.Type object = persistenceValues[current];
				switch (object) {
					case COUNT_DURATION -> {
						countDisabler.get().accept(true);
						countDisabler.get().accept(true);
					}
					case DURATION -> {
						durationDisabler.get().accept(true);
						countDisabler.get().accept(false);
					}
					case COUNT -> {
						countDisabler.get().accept(true);
						durationDisabler.get().accept(false);
					}
					case CHANNEL -> {
						countDisabler.get().accept(false);
						durationDisabler.get().accept(false);
					}
				}
			}

			public Persistence.Type getValue() {
				return persistenceValues[current];
			}
		};
		rightColY += rightColYStep;
		addDrawableChild(text("api.chat.groups.persistence", leftColX, leftColY));
		leftColY += leftColYStep;
		addDrawableChild(persistence);
		addDrawableChild(text("api.chat.groups.persistence.count", "api.chat.groups.persistence.count.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		countDisabler.set(sliderAssembly(rightColX, rightColY, val -> (int) (val * 100d), count::set));
		rightColY += rightColYStep;
		addDrawableChild(text("api.chat.groups.persistence.duration", "api.chat.groups.persistence.duration.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		durationDisabler.set(sliderAssembly(rightColX, rightColY, val -> (long) (val * 100d), duration::set));
		rightColY += rightColYStep;
		countDisabler.get().accept(false);
		durationDisabler.get().accept(false);

		var namesInput = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, ""));
		addDrawableChild(text("api.chat.groups.participants", leftColX, leftColY));

		int footerY = height - 36 / 2 - 20 / 2;
		addDrawableChild(new VanillaButtonWidget(width / 2 - 150 - 4, footerY, 150, 20, I18n.translate("gui.cancel"), widget -> minecraft.openScreen(parent)));
		addDrawableChild(new VanillaButtonWidget(width / 2 + 4, footerY, 150, 20, I18n.translate("gui.done"), widget -> {
			ChannelRequest.createChannel(nameField.getText(),
				Persistence.of(persistence.getValue(), count.get().get(), duration.get().get()),
				Arrays.stream(namesInput.getText().split(",")).filter(s -> !s.isEmpty()).toArray(String[]::new));
			minecraft.openScreen(parent);
		}));
	}

	private <T> Consumer<Boolean> sliderAssembly(int x, int y, Function<Double, T> valueFunc, Consumer<Supplier<T>> value) {

		AtomicReference<T> currentVal = new AtomicReference<>();
		DoubleOption opt = new DoubleOption("", 0d, d -> currentVal.set(valueFunc.apply(d)), 0d, 1d);
		var slider = new SliderWidget<>(x, y, 128, 20, opt) {
			@Override
			public String getMessage() {
				return String.valueOf(currentVal.get());
			}
		};
		opt.set(0d);
		value.accept(currentVal::get);
		var text = new TextFieldWidget(textRenderer, x, y, 128, 20, "");
		var textButton = new ButtonWidget(x + 130, y, 20, 20, I18n.translate("slider.text_input"), w -> {
			if (slider.visible) {
				text.setX(slider.getX());
				text.setY(slider.getY());
				text.setWidth(slider.getWidth());
				text.setText(String.valueOf(currentVal.get()));
			} else {
				try {
					opt.set(Double.parseDouble(text.getText()));
				} catch (Exception ignored) {
				}
			}
			slider.visible = !slider.visible;
			text.visible = !slider.visible;
		}) {
			@Override
			public void drawWidget(int mouseX, int mouseY, float delta) {
				this.client.getTextureManager().bind(WIDGETS_LOCATION);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				int k = this.active ? (this.hovered ? 2 : 1) : 0;
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
				GlStateManager.blendFunc(770, 771);
				this.drawTexture(this.getX(), this.getY(), 0, 46 + k * 20, this.getWidth() / 2, this.getHeight());
				this.drawTexture(this.getX() + this.getWidth() / 2, this.getY(), 200 - this.getWidth() / 2, 46 + k * 20, this.getWidth() / 2, this.getHeight());
				client.getTextureManager().bind(new Identifier("axolotlclient", "textures/gui/sprites/cursor.png"));
				drawTexture(getX() + getWidth() / 2 - 4, getY() + getHeight() / 2 - 4,
					8, 8, 8, 8, 8, 8, 8, 8);

			}
		};
		text.visible = false;
		addDrawableChild(text);
		addDrawableChild(textButton);
		addDrawableChild(slider);
		return val -> slider.active = textButton.active = text.active = val;
	}

	private ClickableWidget text(String translationKey, int x, int y) {
		String text = I18n.translate(translationKey);
		return new ClickableWidget(x, y, 150, textRenderer.fontHeight, text) {

			@Override
			public void drawWidget(int mouseX, int mouseY, float delta) {
				DrawUtil.drawScrollingText(getMessage(), getX(), getY(), getWidth(), getHeight(), Colors.WHITE);
			}

			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return false;
			}
		};
	}

	private ClickableWidget text(String translationKey, String tooltipKey, int x, int y) {
		String text = I18n.translate(translationKey);
		return new ClickableWidget(x, y, 150, textRenderer.fontHeight, text) {

			@Override
			public void drawWidget(int mouseX, int mouseY, float delta) {
				DrawUtil.drawScrollingText(getMessage(), getX(), getY(), getWidth(), getHeight(), Colors.WHITE);
				if (isHovered()) {
					CreateChannelScreen.this.tooltip = I18n.translate(tooltipKey);
					/*GlStateManager.enableBlend();
					GlStateManager.disableDepthTest();
					GlStateManager.pushMatrix();
					GlStateManager.translatef(0, 0, 200);
					CreateChannelScreen.this.renderTooltip(I18n.translate(tooltipKey), mouseX, mouseY);
					GlStateManager.disableLighting();
					GlStateManager.disableDepthTest();
					GlStateManager.popMatrix();
					GlStateManager.color3f(1, 1, 1);*/
				}
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return false;
			}
		};
	}
}
