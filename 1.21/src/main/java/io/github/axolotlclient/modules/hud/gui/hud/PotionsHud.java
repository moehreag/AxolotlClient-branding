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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.CardinalOrder;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Holder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PotionsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = Identifier.of("kronhud", "potionshud");

	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint();

	private final EnumOption<CardinalOrder> order = DefaultOptions.getCardinalOrder(CardinalOrder.TOP_DOWN);

	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private final BooleanOption showEffectName = new BooleanOption("showEffectNames", true);

	public PotionsHud() {
		super(50, 200, false);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
		if (effects.isEmpty()) {
			return;
		}
		renderEffects(graphics, effects, delta);
	}

	private void renderEffects(GuiGraphics graphics, List<StatusEffectInstance> effects, float tickDelta) {
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
			StatusEffectInstance effect = effects.get(direction.getDirection() == -1 ? i : effects.size() - i - 1);
			if (direction.isXAxis()) {
				renderPotion(graphics, effect, x + lastPos + 1, y + 1, tickDelta);
				lastPos += (iconsOnly.get() ? 20 : (showEffectName.get() ? 20 + client.textRenderer.getWidth(Text.translatable(effect.getTranslationKey()).append(" ")
					.append(Util.toRoman(effect.getAmplifier() + 1))) : 50));
			} else {
				renderPotion(graphics, effect, x + 1, y + 1 + lastPos, tickDelta);
				lastPos += 20;
			}
		}
	}

	private int calculateWidth(List<StatusEffectInstance> effects) {
		if ((order.get()).isXAxis()) {
			if (iconsOnly.get()) {
				return 20 * effects.size() + 2;
			}
			if (!showEffectName.get()) {
				return 50 * effects.size() + 2;
			}
			return effects.stream()
					   .map(effect -> Text.translatable(effect.getTranslationKey()).append(" ").append(Util.toRoman(effect.getAmplifier())))
					   .mapToInt(client.textRenderer::getWidth).map(i -> i + 20).sum() + 2;
		} else {
			if (iconsOnly.get()) {
				return 20;
			}
			if (!showEffectName.get()) {
				return 50;
			}
			return effects.stream()
					   .map(effect -> Text.translatable(effect.getTranslationKey()).append(" ").append(Util.toRoman(effect.getAmplifier())))
					   .map(client.textRenderer::getWidth).max(Integer::compare).orElse(38) + 22;
		}
	}

	private int calculateHeight(List<StatusEffectInstance> effects) {
		if ((order.get()).isXAxis()) {
			return 22;
		} else {
			return 20 * effects.size() + 2;
		}
	}

	private void renderPotion(GuiGraphics graphics, StatusEffectInstance effect, int x, int y, float tickDelta) {
		Holder<StatusEffect> type = effect.getEffectType();
		Sprite sprite = client.getStatusEffectSpriteManager().getSprite(type);

		RenderSystem.setShaderTexture(0, sprite.getId());
		RenderSystem.setShaderColor(1, 1, 1, 1);
		graphics.drawSprite(x, y, 0, 18, 18, sprite);
		if (!iconsOnly.get()) {
			float tickrate = client.world != null ? client.world.getTickManager().getTickRate() : 1;
			if (showEffectName.get()) {
				Text string = Text.translatable(effect.getTranslationKey()).append(" ").append(Util.toRoman(effect.getAmplifier()));

				graphics.drawText(client.textRenderer, string, x + 19, y + 1, textColor.get().toInt(), shadow.get());
				Text duration = StatusEffectUtil.durationToString(effect, 1, tickrate);
				graphics.drawText(client.textRenderer, duration, x + 19, y + 1 + 10, 8355711, shadow.get());
			} else {
				graphics.drawText(client.textRenderer, StatusEffectUtil.durationToString(effect, 1, tickrate), x + 19, y + 5,
					textColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.SPEED, 9999);
		StatusEffectInstance jump = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 99999);
		StatusEffectInstance haste = new StatusEffectInstance(StatusEffects.HASTE, -1);
		renderEffects(graphics, List.of(effect, jump, haste), 0);
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(order);
		options.add(iconsOnly);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return (anchor.get());
	}
}
