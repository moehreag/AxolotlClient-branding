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

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Persistence;
import io.github.axolotlclient.api.util.UUIDHelper;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CreateChannelScreen extends Screen {
	private final Screen parent;

	protected CreateChannelScreen(Screen parent) {
		super(Component.translatable("api.chat.groups.create"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		layout.addTitleHeader(title, font);

		LinearLayout content = LinearLayout.horizontal().spacing(8);
		content.defaultCellSetting().alignVerticallyTop();
		content.defaultCellSetting().alignHorizontallyCenter();

		var names = content.addChild(LinearLayout.vertical().spacing(20 - font.lineHeight + 8 - 5));
		names.defaultCellSetting().paddingTop(5);
		var options = content.addChild(LinearLayout.vertical().spacing(8));
		var nameField = new EditBox(font, 150, 20, Component.empty());
		names.addChild(text("api.chat.groups.name"));
		options.addChild(nameField);

		AtomicReference<Supplier<Integer>> count = new AtomicReference<>();
		AtomicReference<Supplier<Long>> duration = new AtomicReference<>();

		AtomicReference<Consumer<Boolean>> countDisabler = new AtomicReference<>();
		AtomicReference<Consumer<Boolean>> durationDisabler = new AtomicReference<>();
		var persistence = CycleButton.<Persistence.Type>builder(
				type -> Component.translatable("api.chat.persistence." + type.getId()))
			.withValues(Persistence.Type.values()).displayOnlyValue()
			.create(Component.empty(), (cyclingButtonWidget, object) -> {
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
		names.addChild(text("api.chat.groups.persistence"));
		options.addChild(persistence);
		names.addChild(text("api.chat.groups.persistence.count", "api.chat.groups.persistence.count.tooltip"));
		countDisabler.set(sliderAssembly(options::addChild, val -> (int) (val * 100d), count::set));
		names.addChild(text("api.chat.groups.persistence.duration", "api.chat.groups.persistence.duration.tooltip"));
		durationDisabler.set(sliderAssembly(options::addChild, val -> (long) (val * 100d), duration::set));
		countDisabler.get().accept(false);
		durationDisabler.get().accept(false);

		var namesInput = options.addChild(new EditBox(font, 150, 20, Component.empty()));
		names.addChild(text("api.chat.groups.participants"));

		layout.addToContents(content);

		var footer = LinearLayout.horizontal().spacing(8);
		footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, widget -> minecraft.setScreen(parent)).build());
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, widget -> {
			ChannelRequest.createChannel(nameField.getValue(), Persistence.of(persistence.getValue(), count.get().get(),
					duration.get().get()
				),
				Arrays.stream(namesInput.getValue().split(",")).filter(s -> !s.isEmpty()).map(UUIDHelper::ensureUuid)
					.toArray(String[]::new)
			);
			minecraft.setScreen(parent);
		}).build());
		layout.addToFooter(footer);

		layout.arrangeElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	private <T> Consumer<Boolean> sliderAssembly(Consumer<LayoutElement> layoutConsumer, Function<Double, T> valueFunc, Consumer<Supplier<T>> value) {

		AtomicReference<T> currentVal = new AtomicReference<>();
		var slider = new AbstractSliderButton(0, 0, 128, 20, Component.empty(), 0) {
			@Override
			protected void updateMessage() {
				setMessage(Component.literal(String.valueOf(valueFunc.apply(value))));
			}

			@Override
			protected void applyValue() {
				currentVal.set(valueFunc.apply(value));
			}
		};
		slider.updateMessage();
		slider.applyValue();
		value.accept(currentVal::get);
		var text = new EditBox(font, 128, 20, Component.empty());
		var textButton = SpriteIconButton.builder(Component.translatable("slider.text_input"), w -> {
			if (slider.visible) {
				text.setPosition(slider.getX(), slider.getY());
				text.setWidth(slider.getWidth());
				text.setHeight(slider.getHeight());
				text.setValue(String.valueOf(currentVal.get()));
			} else {
				try {
					slider.onClick(slider.getX() + (1d / slider.getWidth()) * Double.parseDouble(text.getValue()),
						slider.getY()
					);
				} catch (Exception ignored) {
				}
			}
			slider.visible = !slider.visible;
			text.visible = !slider.visible;
		}, true).sprite(ResourceLocation.fromNamespaceAndPath("axolotlclient", "cursor"), 8, 8).size(20, 20).build();
		var assembly = LinearLayout.horizontal().spacing(2);
		assembly.addChild(slider);
		assembly.addChild(textButton);
		text.visible = false;
		addRenderableWidget(text);
		layoutConsumer.accept(assembly);
		return val -> slider.active = textButton.active = text.active = val;
	}

	private LayoutElement text(String translationKey) {
		return new StringWidget(Component.translatable(translationKey), font);
	}

	private LayoutElement text(String translationKey, String tooltipKey) {
		var widget = new StringWidget(Component.translatable(translationKey), font);
		widget.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
		return widget;
	}
}
