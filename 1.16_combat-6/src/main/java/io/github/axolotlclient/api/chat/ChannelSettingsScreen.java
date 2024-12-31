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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.Persistence;
import io.github.axolotlclient.api.util.UUIDHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ChannelSettingsScreen extends Screen {
	private final Screen parent;
	private final Channel channel;

	protected ChannelSettingsScreen(Screen parent, Channel channel) {
		super(new TranslatableText("api.channel.configure"));
		this.parent = parent;
		this.channel = channel;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		drawCenteredText(graphics, textRenderer, title, width / 2, 36 / 2 - textRenderer.fontHeight / 2, -1);
		super.render(graphics, mouseX, mouseY, delta);
		hoveredElement(mouseX, mouseY).filter(e -> e instanceof ButtonWidget).map(b -> (ButtonWidget)b).ifPresent(b -> b.renderToolTip(graphics, mouseX, mouseY));
	}

	@Override
	protected void init() {
		int leftColX = width / 2 - 4 - 150;
		int leftColYStep = textRenderer.fontHeight + 20 - textRenderer.fontHeight + 8 - 5 + 5;
		int leftColY = 36 + 5 + 30;
		int rightColX = width / 2 + 4;
		int rightColYStep = 20 + 8;
		int rightColY = 36 + 30;

		var nameField = new TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, LiteralText.EMPTY);
		nameField.setText(channel.getRealName());
		addButton(text("api.chat.groups.name", leftColX, leftColY));
		addButton(nameField);
		rightColY += rightColYStep;
		leftColY += leftColYStep;

		AtomicReference<Supplier<Integer>> count = new AtomicReference<>();
		AtomicReference<Supplier<Long>> duration = new AtomicReference<>();

		AtomicReference<Consumer<Boolean>> countDisabler = new AtomicReference<>();
		AtomicReference<Consumer<Boolean>> durationDisabler = new AtomicReference<>();
		var persistence = new ButtonWidget(rightColX, rightColY, 150, 20, LiteralText.EMPTY, a -> {
		}) {
			final Persistence.Type[] values = Persistence.Type.values();
			int current = channel.getPersistence().type().ordinal();

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				setMessage(new TranslatableText("api.chat.persistence." + values[current].getId()));
				super.renderButton(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void onPress() {
				current++;
				if (current >= values.length) {
					current = 0;
				}
				Persistence.Type object = values[current];
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
				return values[current];
			}
		};
		rightColY += rightColYStep;
		addButton(text("api.chat.groups.persistence", leftColX, leftColY));
		leftColY += leftColYStep;
		addButton(persistence);
		addButton(text("api.chat.groups.persistence.count", "api.chat.groups.persistence.count.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		countDisabler.set(sliderAssembly(rightColX, rightColY, val -> (int) (val * 100d), count::set, channel.getPersistence().count() / 100d));
		rightColY += rightColYStep;
		addButton(text("api.chat.groups.persistence.duration", "api.chat.groups.persistence.duration.tooltip", leftColX, leftColY));
		leftColY += leftColYStep;
		durationDisabler.set(sliderAssembly(rightColX, rightColY, val -> (long) (val * 100d), duration::set, channel.getPersistence().duration() / 100d));
		rightColY += rightColYStep;
		countDisabler.get().accept(false);
		durationDisabler.get().accept(false);

		var namesInput = addButton(new TextFieldWidget(textRenderer, rightColX, rightColY, 150, 20, LiteralText.EMPTY));
		addButton(text("api.chat.groups.add_participants", leftColX, leftColY));

		int footerY = height - 36 / 2 - 20 / 2;
		addButton(new ButtonWidget(width / 2 - 150 - 4, footerY, 150, 20, ScreenTexts.CANCEL, widget -> client.openScreen(parent)));
		addButton(new ButtonWidget(width / 2 + 4, footerY, 150, 20, ScreenTexts.DONE, widget -> {
			ChannelRequest.updateChannel(channel.getId(), nameField.getText(),
				Persistence.of(persistence.getValue(), count.get().get(), duration.get().get()),
				Arrays.stream(namesInput.getText().split(",")).filter(s -> !s.isEmpty()).map(UUIDHelper::ensureUuid).toArray(String[]::new));
			client.openScreen(parent);
		}));
	}

	private <T> Consumer<Boolean> sliderAssembly(int x, int y, Function<Double, T> valueFunc, Consumer<Supplier<T>> value, double initialValue) {

		AtomicReference<T> currentVal = new AtomicReference<>();
		var slider = new SliderWidget(x, y, 128, 20, LiteralText.EMPTY, initialValue) {
			@Override
			protected void updateMessage() {
				setMessage(new LiteralText(String.valueOf(valueFunc.apply(value))));
			}

			@Override
			protected void applyValue() {
				currentVal.set(valueFunc.apply(value));
			}
		};
		slider.updateMessage();
		slider.applyValue();
		value.accept(currentVal::get);
		var text = new TextFieldWidget(textRenderer, x, y, 128, 20, LiteralText.EMPTY);
		var textButton = new ButtonWidget(x + 130, y, 20, 20, new TranslatableText("slider.text_input"), w -> {
			if (slider.visible) {
				text.x = slider.x;
				text.y = slider.y;
				text.setWidth(slider.getWidth());
				text.setText(String.valueOf(currentVal.get()));
			} else {
				try {
					slider.onClick(slider.x + (1d / slider.getWidth()) * Double.parseDouble(text.getText()), slider.y);
				} catch (Exception ignored) {
				}
			}
			slider.visible = !slider.visible;
			text.visible = !slider.visible;
		}) {
			@SuppressWarnings("deprecation")
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				client.getTextureManager().bindTexture(WIDGETS_LOCATION);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
				int i = this.getYImage(this.isHovered());
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.enableDepthTest();
				this.drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
				this.drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				client.getTextureManager().bindTexture(new Identifier("axolotlclient", "textures/gui/sprites/cursor.png"));
				drawTexture(matrices,
					x + getWidth() / 2 - 4, y + getHeight() / 2 - 4,
					8, 8, 0, 0, 8, 8, 8, 8);

			}
		};
		text.visible = false;
		addButton(text);
		addButton(textButton);
		addButton(slider);
		return val -> slider.active = textButton.active = text.active = val;
	}

	private AbstractButtonWidget text(String translationKey, int x, int y) {
		Text text = new TranslatableText(translationKey);
		return new AbstractButtonWidget(x, y, 150, textRenderer.fontHeight, text) {

			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				DrawUtil.drawScrollingText(matrices, getMessage(), this.x, this.y, this.width, this.height, Colors.WHITE);
			}

			@Override
			public void playDownSound(SoundManager soundManager) {
			}
		};
	}

	private AbstractButtonWidget text(String translationKey, String tooltipKey, int x, int y) {
		Text text = new TranslatableText(translationKey);
		AbstractButtonWidget widget = new ButtonWidget(x, y, 150, textRenderer.fontHeight, text, a -> {
		}) {

			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				DrawUtil.drawScrollingText(matrices, getMessage(), this.x, this.y, this.width, this.height, Colors.WHITE);
			}

			@Override
			public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
				ChannelSettingsScreen.this.renderOrderedTooltip(matrices, textRenderer.wrapLines(new TranslatableText(tooltipKey), 170), mouseX, mouseY);
			}

			@Override
			public void playDownSound(SoundManager soundManager) {
			}
		};
		return widget;
	}
}
