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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PotionsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "potionshud");
	protected static final Identifier INVENTORY_TEXTURE = new Identifier("textures/gui/container/inventory.png");
	private final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint();
	private final EnumOption<CardinalOrder> order = DefaultOptions.getCardinalOrder(CardinalOrder.TOP_DOWN);
	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", false);
	private final BooleanOption showEffectName = new BooleanOption("showEffectNames", true);
	private final List<StatusEffectInstance> placeholder = Util.make(() -> {
		List<StatusEffectInstance> list = new ArrayList<>();
		StatusEffectInstance effect = new StatusEffectInstance(StatusEffect.SPEED.id, 9999);
		StatusEffectInstance jump = new StatusEffectInstance(StatusEffect.JUMP_BOOST.id, 99999);
		StatusEffectInstance haste = new StatusEffectInstance(StatusEffect.HASTE.id, Integer.MAX_VALUE);
		haste.setPermanent(true);
		list.add(effect);
		list.add(jump);
		list.add(haste);
		return list;
	});

	public PotionsHud() {
		super(50, 200, false);
	}

	@Override
	public void renderComponent(float delta) {
		List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
		if (effects.isEmpty()) {
			return;
		}
		renderEffects(effects);
	}

	private void renderEffects(List<StatusEffectInstance> effects) {
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
		CardinalOrder direction = order.get();

		Rectangle bounds = getBounds();
		int x = bounds.x();
		int y = bounds.y();
		for (int i = 0; i < effects.size(); i++) {
			StatusEffectInstance effect = effects.get(direction.getDirection() == -1 ? i : effects.size() - i - 1);
			if (direction.isXAxis()) {
				renderPotion(effect, x + lastPos + 1, y + 1);
				lastPos += (iconsOnly.get() ? 20 : (showEffectName.get() ? 20 + client.textRenderer.getWidth(I18n.translate(effect.getName()) + " " +
					Util.toRoman(effect.getAmplifier()+1)) : 50));
			} else {
				renderPotion(effect, x + 1, y + 1 + lastPos);
				lastPos += 20;
			}
		}
	}

	private int calculateWidth(List<StatusEffectInstance> effects) {
		if (order.get().isXAxis()) {
			if (iconsOnly.get()) {
				return 20 * effects.size() + 2;
			}
			if (!showEffectName.get()) {
				return 50 * effects.size() + 2;
			}
			return effects.stream()
				.map(effect -> I18n.translate(effect.getName()) + " " + Util.toRoman(effect.getAmplifier()))
				.mapToInt(client.textRenderer::getWidth).map(i -> i + 20).sum() + 2;
		} else {
			if (iconsOnly.get()) {
				return 20;
			}
			if (!showEffectName.get()) {
				return 50;
			}
			return effects.stream()
				.map(effect -> I18n.translate(effect.getName()) + " " + Util.toRoman(effect.getAmplifier()))
				.map(client.textRenderer::getWidth).max(Integer::compare).orElse(38) + 22;
		}
	}

	private int calculateHeight(List<StatusEffectInstance> effects) {
		if (order.get().isXAxis()) {
			return 22;
		} else {
			return 20 * effects.size() + 2;
		}
	}

	private void renderPotion(StatusEffectInstance effect, int x, int y) {
		StatusEffect type = StatusEffect.BY_ID[effect.getId()];
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bind(INVENTORY_TEXTURE);
		int m = type.getIconIndex();
		this.drawTexture(x, y, m % 8 * 18, 198 + m / 8 * 18, 18, 18);
		if (!iconsOnly.get()) {
			if (showEffectName.get()) {
				String string = I18n.translate(effect.getName()) + " " + Util.toRoman(effect.getAmplifier());

				drawString(string, (float) (x + 19), (float) (y + 1), 16777215, shadow.get());
				String duration = StatusEffect.getDurationString(effect);
				drawString(duration, (float) (x + 19), (float) (y + 1 + 10), textColor.get().toInt(), shadow.get());
			} else {
				drawString(StatusEffect.getDurationString(effect), x + 19, y + 5, textColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		renderEffects(placeholder);
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
		return anchor.get();
	}
}
