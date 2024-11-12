/*
 * Copyright © 2021-2024 moehreag <moehreag@gmail.com> & Contributors
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

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.Persistence;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChannelSettingsScreen extends Screen {
	private final Screen parent;
	private final Channel channel;

	protected ChannelSettingsScreen(Screen parent, Channel channel) {
		super(Text.translatable("api.chat.groups.configure"));
		this.parent = parent;
		this.channel = channel;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
	}

	@Override
	protected void init() {
		addDrawableChild(new TextWidget(width/2 - textRenderer.getWidth(title) /2, 36 / 2 - textRenderer.fontHeight /2, textRenderer.getWidth(title), textRenderer.fontHeight, title, textRenderer));

		int leftColX = width / 2 - 4 - 150;
		int leftColYStep = textRenderer.fontHeight + 20 - textRenderer.fontHeight + 8 - 5 + 5;
		int leftColY = 36 + 5 + 30;
		int rightColX = width / 2 + 4;
		int rightColYStep = 20 + 8;
		int rightColY = 36 + 30;

		var nameField = new TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, Text.empty());
		nameField.setText(channel.getName());
		addDrawableChild(text("api.chat.groups.name", leftColX, leftColY));
		addDrawableChild(nameField);
		rightColY += rightColYStep;
		leftColY += leftColYStep;

		AtomicReference<Supplier<Integer>> count = new AtomicReference<>();
		AtomicReference<Supplier<Long>> duration = new AtomicReference<>();

		AtomicReference<Consumer<Boolean>> countDisabler = new AtomicReference<>();
		AtomicReference<Consumer<Boolean>> durationDisabler = new AtomicReference<>();
		var persistence = CyclingButtonWidget.<Persistence.Type>builder(type -> Text.translatable("api.chat.persistence." + type.getId()))
				.values(Persistence.Type.values()).omitKeyText().build(rightColX, rightColY, 150, 20, Text.empty(), (cyclingButtonWidget, object) -> {
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
				});
		persistence.setValue(channel.getPersistence().type());
		rightColY += rightColYStep;
		addDrawableChild(text("api.chat.groups.persistence", leftColX, leftColY));
		leftColY += leftColYStep;
		addDrawableChild(persistence);
		addDrawableChild(text("api.chat.groups.persistence.count", "api.chat.groups.persistence.count.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		countDisabler.set(sliderAssembly(rightColX, rightColY, val -> (int) (val * 100d), count::set, channel.getPersistence().count() / 100d));
		rightColY += rightColYStep;
		addDrawableChild(text("api.chat.groups.persistence.duration", "api.chat.groups.persistence.duration.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		durationDisabler.set(sliderAssembly(rightColX, rightColY, val -> (long) (val * 100d), duration::set, channel.getPersistence().duration() / 100d));
		rightColY += rightColYStep;
		countDisabler.get().accept(false);
		durationDisabler.get().accept(false);

		var namesInput = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, Text.empty()));
		addDrawableChild(text("api.chat.groups.add_participants", leftColX, leftColY));


		int footerY = height - 36 / 2 - 20 / 2;
		addDrawableChild(ButtonWidget.builder(CommonTexts.CANCEL, widget -> client.setScreen(parent)).positionAndSize(width / 2 - 150 - 4, footerY, 150, 20).build());
		addDrawableChild(ButtonWidget.builder(CommonTexts.DONE, widget -> {
			ChannelRequest.updateChannel(channel.getId(), nameField.getText(),
					Persistence.of(persistence.getValue(), count.get().get(), duration.get().get()),
					Arrays.stream(namesInput.getText().split(",")).filter(s -> !s.isEmpty()).toArray(String[]::new));
			client.setScreen(parent);
		}).positionAndSize(width / 2 + 4, footerY, 150, 20).build());
	}

	private <T> Consumer<Boolean> sliderAssembly(int x, int y, Function<Double, T> valueFunc, Consumer<Supplier<T>> value, double initialValue) {

		AtomicReference<T> currentVal = new AtomicReference<>();
		var slider = new SliderWidget(x, y, 128, 20, Text.empty(), initialValue) {
			@Override
			protected void updateMessage() {
				setMessage(Text.literal(String.valueOf(valueFunc.apply(value))));
			}

			@Override
			protected void applyValue() {
				currentVal.set(valueFunc.apply(value));
			}
		};
		slider.updateMessage();
		slider.applyValue();
		value.accept(currentVal::get);
		var text = new TextFieldWidget(textRenderer, x, y, 128, 20, Text.empty());
		var textButton = new ButtonWidget(x + 130, y, 20, 20, Text.empty(), w -> {
			if (slider.visible) {
				text.setPosition(slider.getX(), slider.getY());
				text.setWidth(slider.getWidth());
				text.setText(String.valueOf(currentVal.get()));
			} else {
				try {
					slider.onClick(slider.getX() + (1d / slider.getWidth()) * Double.parseDouble(text.getText()), slider.getY());
				} catch (Exception ignored) {
				}
			}
			slider.visible = !slider.visible;
			text.visible = !slider.visible;
		}, s -> Text.translatable("slider.text_input")) {
			@Override
			protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				super.drawWidget(graphics, mouseX, mouseY, delta);
				graphics.drawTexture(new Identifier("axolotlclient", "textures/gui/sprites/cursor.png"),
						getX() + getWidth()/2 - 4, getY() + getHeight()/2 - 4,
						8, 8, 0, 0, 8, 8, 8, 8);

			}
		};
		text.visible = false;
		addDrawableChild(text);
		return val -> slider.active = textButton.active = text.active = val;
	}

	private ClickableWidget text(String translationKey, int x, int y) {
		Text text = Text.translatable(translationKey);
		return new AbstractTextWidget(x, y, 150, textRenderer.fontHeight, text, textRenderer){

			@Override
			protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				drawScrollingText(graphics, textRenderer, getMessage(), getX(), getY(), getX()+getWidth(), getY()+getHeight(), getTextColor());
			}
		};
	}

	private ClickableWidget text(String translationKey, String tooltipKey, int x, int y) {
		Text text = Text.translatable(translationKey);
		AbstractTextWidget widget = new AbstractTextWidget(x, y, textRenderer.getWidth(text), textRenderer.fontHeight, text, textRenderer){

			@Override
			protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
				drawScrollingText(graphics, textRenderer, getMessage(), getX(), getY(), getX()+getWidth(), getY()+getHeight(), getTextColor());
			}
		};
		widget.setTooltip(Tooltip.create(Text.translatable(tooltipKey)));
		return widget;
	}
}
