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
import io.github.axolotlclient.api.types.Persistence;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.CyclingButtonWidget;
import net.minecraft.client.gui.widget.button.SpriteButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CreateChannelScreen extends Screen {
	private final Screen parent;
	protected CreateChannelScreen(Screen parent) {
		super(Text.translatable("api.chat.groups.create"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
		layout.addToHeader(title, client.textRenderer);

		LinearLayoutWidget content = LinearLayoutWidget.createHorizontal().setSpacing(8);
		content.getDefaultSettings().alignVerticallyTop();
		content.getDefaultSettings().alignHorizontallyCenter();

		LinearLayoutWidget names = content.add(LinearLayoutWidget.createVertical().setSpacing(20-client.textRenderer.fontHeight+8-5));
		names.getDefaultSettings().setTopPadding(5);
		LinearLayoutWidget options = content.add(LinearLayoutWidget.createVertical().setSpacing(8));
		var nameField = new TextFieldWidget(client.textRenderer, 150, 20, Text.empty());
		names.add(text("api.chat.groups.name"));
		options.add(nameField);

		AtomicReference<Supplier<Integer>> count = new AtomicReference<>();
		AtomicReference<Supplier<Long>> duration = new AtomicReference<>();

		AtomicReference<Consumer<Boolean>> countDisabler = new AtomicReference<>();
		AtomicReference<Consumer<Boolean>> durationDisabler = new AtomicReference<>();
		var persistence = CyclingButtonWidget.<Persistence.Type>builder(type -> Text.translatable("api.chat.persistence."+type.getId()))
			.values(Persistence.Type.values()).omitKeyText().build(Text.empty(), (cyclingButtonWidget, object) -> {
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
		names.add(text("api.chat.groups.persistence"));
		options.add(persistence);
		names.add(text("api.chat.groups.persistence.count", "api.chat.groups.persistence.count.tooltip"));
		countDisabler.set(sliderAssembly(options::add, val -> (int) (val*100d), count::set));
		names.add(text("api.chat.groups.persistence.duration", "api.chat.groups.persistence.duration.tooltip"));
		durationDisabler.set(sliderAssembly(options::add, val -> (long) (val * 100d), duration::set));
		countDisabler.get().accept(false);
		durationDisabler.get().accept(false);

		var namesInput = content.add(new TextFieldWidget(client.textRenderer, 150, 20, Text.empty()));
		names.add(text("api.chat.groups.participants"));

		layout.addToContents(content);

		LinearLayoutWidget footer = LinearLayoutWidget.createHorizontal().setSpacing(8);
		footer.add(ButtonWidget.builder(CommonTexts.CANCEL, widget -> client.setScreen(parent)).build());
		footer.add(ButtonWidget.builder(CommonTexts.DONE, widget -> {
			ChannelRequest.createChannel(nameField.getText(),
				Persistence.of(persistence.getValue(), count.get().get(), duration.get().get()),
				Arrays.stream(namesInput.getText().split(",")).filter(s -> !s.isEmpty()).toArray(String[]::new));
			client.setScreen(parent);
		}).build());
		layout.addToFooter(footer);

		layout.arrangeElements();
		layout.visitWidgets(this::addDrawableSelectableElement);
	}

	private <T> Consumer<Boolean> sliderAssembly(Consumer<Widget> layoutConsumer, Function<Double, T> valueFunc, Consumer<Supplier<T>> value) {

		AtomicReference<T> currentVal = new AtomicReference<>();
		var slider = new SliderWidget(0, 0, 128, 20, Text.empty(), 0) {
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
		var text = new TextFieldWidget(client.textRenderer, 128, 20, Text.empty());
		var textButton = SpriteButtonWidget.builder(Text.translatable("slider.text_input"), w -> {
			if (slider.visible) {
				text.setText(String.valueOf(currentVal.get()));
			} else {
				try {
					slider.onClick(slider.getX() + (1d / slider.getWidth()) * Double.parseDouble(text.getText()), slider.getY());
				} catch (Exception ignored) {}
			}
			slider.visible = !slider.visible;
			text.visible = !slider.visible;
		}, true).sprite(Identifier.of("axolotlclient", "cursor"), 8, 8).dimensions(20, 20).build();
		LinearLayoutWidget assembly = new LinearLayoutWidget(0, 0, LinearLayoutWidget.Orientation.HORIZONTAL) {
			@Override
			public void arrangeElements() {
				super.arrangeElements();
				text.setPosition(slider.getX(), slider.getY());
				text.setWidth(slider.getWidth());
				text.setHeight(slider.getHeight());
			}
		}.setSpacing(2);
		assembly.add(slider);
		assembly.add(textButton);
		text.visible = false;
		addDrawableSelectableElement(text);
		layoutConsumer.accept(assembly);
		return val -> slider.active = textButton.active = text.active = val;
	}

	private Widget text(String translationKey) {
		return new TextWidget(Text.translatable(translationKey), client.textRenderer);
	}

	private Widget text(String translationKey, String tooltipKey) {
		var widget = new TextWidget(Text.translatable(translationKey), client.textRenderer);
		widget.setTooltip(Tooltip.create(Text.translatable(tooltipKey)));
		return widget;
	}
}
