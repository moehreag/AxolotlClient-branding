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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.CardinalOrder;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PotionsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "potionshud");

	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint();

	private final EnumOption<CardinalOrder> order = DefaultOptions.getCardinalOrder(CardinalOrder.TOP_DOWN);

	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private final BooleanOption showEffectName = new BooleanOption("showEffectNames", true);
	private final ColorOption timerTextColor = new ColorOption("potionshud.timer_text_color", Color.parse("#7F7F7F"));

	public PotionsHud() {
		super(50, 200, false);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		List<MobEffectInstance> effects = new ArrayList<>(client.player.getActiveEffects());
		if (effects.isEmpty()) {
			return;
		}
		renderEffects(graphics, effects, delta);
	}

	private void renderEffects(GuiGraphics graphics, List<MobEffectInstance> effects, float tickDelta) {
		int calcWidth = calculateWidth(effects);
		int calcHeight = calculateHeight(effects);
		boolean changed = false;
		if (calcWidth != width) {
			setWidth(calcWidth);
			changed = true;
		}
		if (calcHeight != height) {
			setHeight(calcHeight);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
		int lastPos = 0;
		CardinalOrder direction = (order.get());

		Rectangle bounds = getBounds();
		int x = bounds.x();
		int y = bounds.y();
		for (int i = 0; i < effects.size(); i++) {
			MobEffectInstance effect = effects.get(direction.getDirection() == -1 ? i : effects.size() - i - 1);
			if (direction.isXAxis()) {
				renderPotion(graphics, effect, x + lastPos + 1, y + 1, tickDelta);
				lastPos += (iconsOnly.get() ? 20 : (showEffectName.get() ? 20 + client.font.width(
					effect.getEffect().value().getDisplayName().copy().append(CommonComponents.SPACE)
						.append(Util.toRoman(effect.getAmplifier() + 1))) : 50));
			} else {
				renderPotion(graphics, effect, x + 1, y + 1 + lastPos, tickDelta);
				lastPos += 20;
			}
		}
	}

	private int calculateWidth(List<MobEffectInstance> effects) {
		if ((order.get()).isXAxis()) {
			if (iconsOnly.get()) {
				return 20 * effects.size() + 2;
			}
			if (!showEffectName.get()) {
				return 50 * effects.size() + 2;
			}
			return effects.stream().map(effect -> Component.translatable(effect.getDescriptionId()).append(" ")
				.append(Util.toRoman(effect.getAmplifier()))).mapToInt(client.font::width).map(i -> i + 20).sum() + 2;
		} else {
			if (iconsOnly.get()) {
				return 20;
			}
			if (!showEffectName.get()) {
				return 50;
			}
			return effects.stream().map(effect -> Component.translatable(effect.getDescriptionId()).append(" ")
				.append(Util.toRoman(effect.getAmplifier()))).map(client.font::width).max(Integer::compare).orElse(38) +
				22;
		}
	}

	private int calculateHeight(List<MobEffectInstance> effects) {
		if ((order.get()).isXAxis()) {
			return 22;
		} else {
			return 20 * effects.size() + 2;
		}
	}

	private void renderPotion(GuiGraphics graphics, MobEffectInstance effect, int x, int y, float tickDelta) {
		Holder<MobEffect> type = effect.getEffect();
		TextureAtlasSprite sprite = client.getMobEffectTextures().get(type);

		graphics.blitSprite(RenderType::guiTextured, sprite, x, y, 18, 18);
		if (!iconsOnly.get()) {
			float tickrate = client.level != null ? client.level.tickRateManager().tickrate() : 1;
			if (showEffectName.get()) {
				Component string = effect.getEffect().value().getDisplayName().copy().append(CommonComponents.SPACE)
					.append(Util.toRoman(effect.getAmplifier() + 1));

				graphics.drawString(client.font, string, x + 19, y + 1, textColor.get().toInt(), shadow.get());
				Component duration = MobEffectUtil.formatDuration(effect, 1, tickrate);
				graphics.drawString(client.font, duration, x + 19, y + 1 + 10, timerTextColor.get().toInt(), shadow.get());
			} else {
				graphics.drawString(client.font, MobEffectUtil.formatDuration(effect, 1, tickrate), x + 19, y + 5,
					timerTextColor.get().toInt(), shadow.get()
				);
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		MobEffectInstance effect = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 9999);
		MobEffectInstance jump = new MobEffectInstance(MobEffects.JUMP, 99999);
		MobEffectInstance haste = new MobEffectInstance(MobEffects.DIG_SPEED, -1);
		renderEffects(graphics, List.of(effect, jump, haste), 0);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(order);
		options.add(iconsOnly);
		options.add(showEffectName);
		options.add(timerTextColor);
		return options;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return (anchor.get());
	}
}
