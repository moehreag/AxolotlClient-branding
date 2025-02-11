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

package io.github.axolotlclient.modules.hud.gui;

import java.util.ArrayList;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.modules.hud.gui.component.HudEntry;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public abstract class AbstractHudEntry extends DrawUtil implements HudEntry {

	@Getter
	protected final ForceableBooleanOption enabled = DefaultOptions.getEnabled();
	protected final DoubleOption scale = DefaultOptions.getScale(this);
	protected final Minecraft client = Minecraft.getInstance();
	private final DoubleOption x = DefaultOptions.getX(getDefaultX(), this);
	private final DoubleOption y = DefaultOptions.getY(getDefaultY(), this);
	@Setter
	@Getter
	protected int width;
	@Setter
	@Getter
	protected int height;
	@Setter
	protected boolean hovered = false;
	private Rectangle trueBounds = null;
	private Rectangle renderBounds = null;
	private DrawPosition truePosition = null;
	private DrawPosition renderPosition;
	private OptionCategory category;

	public AbstractHudEntry(int width, int height) {
		this.width = width;
		this.height = height;
		truePosition = new DrawPosition(0, 0);
		renderPosition = new DrawPosition(0, 0);
		renderBounds = new Rectangle(0, 0, 1, 1);
		trueBounds = new Rectangle(0, 0, 1, 1);
	}

	public static float intToFloat(int current, int max, int offset) {
		return Mth.clamp((float) (current) / (max - offset), 0, 1);
	}

	public static int floatToInt(float percent, int max, int offset) {
		return Mth.clamp(Math.round((max - offset) * percent), 0, max);
	}

	public void renderPlaceholderBackground(GuiGraphics graphics) {
		if (hovered) {
			fillRect(graphics, getTrueBounds(), ClientColors.SELECTOR_BLUE.withAlpha(100));
		} else {
			fillRect(graphics, getTrueBounds(), ClientColors.WHITE.withAlpha(50));
		}
		outlineRect(graphics, getTrueBounds(), ClientColors.BLACK);
	}

	public void scale(GuiGraphics graphics) {
		float scale = getScale();
		graphics.pose().scale(scale, scale, 1);
	}

	@Override
	public int getRawTrueX() {
		return truePosition.x();
	}

	public void setX(int x) {
		this.x.set((double) intToFloat(x, client.getWindow().getGuiScaledWidth(), 0));
	}

	@Override
	public float getScale() {
		return scale.get().floatValue();
	}

	public int getRawX() {
		return getPos().x();
	}

	@Override
	public int getRawTrueY() {
		return truePosition.y();
	}

	public int getRawY() {
		return getPos().y();
	}

	public void setY(int y) {
		this.y.set((double) intToFloat(y, client.getWindow().getGuiScaledHeight(), 0));
	}

	/**
	 * Gets the hud's bounds when the matrix has already been scaled.
	 *
	 * @return The bounds.
	 */
	public Rectangle getBounds() {
		return renderBounds;
	}

	public void setBounds(float scale) {
		if (client.getWindow() == null) {
			truePosition = new DrawPosition(0, 0);
			renderPosition = new DrawPosition(0, 0);
			renderBounds = new Rectangle(0, 0, 1, 1);
			trueBounds = new Rectangle(0, 0, 1, 1);
			return;
		}
		int scaledX = floatToInt(x.get().floatValue(), client.getWindow().getGuiScaledWidth(), 0) - offsetTrueWidth();
		int scaledY = floatToInt(y.get().floatValue(), client.getWindow().getGuiScaledHeight(), 0) - offsetTrueHeight();
		if (scaledX < 0) {
			scaledX = 0;
		}
		if (scaledY < 0) {
			scaledY = 0;
		}
		int trueWidth = (int) (getWidth() * getScale());
		if (trueWidth < client.getWindow().getGuiScaledWidth() &&
			scaledX + trueWidth > client.getWindow().getGuiScaledWidth()) {
			scaledX = client.getWindow().getGuiScaledWidth() - trueWidth;
		}
		int trueHeight = (int) (getHeight() * getScale());
		if (trueHeight < client.getWindow().getGuiScaledHeight() &&
			scaledY + trueHeight > client.getWindow().getGuiScaledHeight()) {
			scaledY = client.getWindow().getGuiScaledHeight() - trueHeight;
		}
		truePosition.x(scaledX).y(scaledY);
		renderPosition = truePosition.divide(getScale());
		renderBounds = new Rectangle(renderPosition.x(), renderPosition.y(), getWidth(), getHeight());
		trueBounds = new Rectangle(scaledX, scaledY, (int) (getWidth() * getScale()), (int) (getHeight() * getScale()));
	}

	@Override
	public DrawPosition getPos() {
		return renderPosition;
	}

	public Rectangle getTrueBounds() {
		return trueBounds;
	}

	@Override
	public DrawPosition getTruePos() {
		return truePosition;
	}

	@Override
	public int getTrueWidth() {
		if (trueBounds == null) {
			return HudEntry.super.getTrueWidth();
		}
		return trueBounds.width();
	}

	@Override
	public int getTrueHeight() {
		if (trueBounds == null) {
			return HudEntry.super.getTrueHeight();
		}
		return trueBounds.height();
	}

	@Override
	public void onBoundsUpdate() {
		setBounds();
	}

	public void setBounds() {
		setBounds(getScale());
	}

	public OptionCategory getAllOptions() {
		if (category == null) {
			List<Option<?>> options = getSaveOptions();
			category = OptionCategory.create(getNameKey());
			options.forEach(category::add);
		}
		return category;
	}

	/**
	 * Returns a list of options that should be saved. By default, this includes {@link #getConfigurationOptions()}
	 *
	 * @return a list of options
	 */
	@Override
	public List<Option<?>> getSaveOptions() {
		List<Option<?>> options = getConfigurationOptions();
		options.add(x);
		options.add(y);
		return options;
	}

	/**
	 * Returns a list of options that should be shown in configuration screens
	 *
	 * @return List of options
	 */
	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = new ArrayList<>();
		options.add(enabled);
		options.add(scale);
		return options;
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	@Override
	public boolean isEnabled() {
		return enabled.get();
	}

	@Override
	public void setEnabled(boolean value) {
		enabled.set(value);
	}
}
