/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DebugCountersHud extends TextHudEntry implements DynamicallyPositionable {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("axolotlclient", "debugcountershud");
	private final EnumOption<AnchorPoint> anchor = new EnumOption<>("anchorpoint", AnchorPoint.class,
		AnchorPoint.TOP_LEFT);
	private final BooleanOption showCCount = new BooleanOption("debugcounters.ccount", true);
	private final BooleanOption showECount = new BooleanOption("debugcounters.ecount", false);
	private final BooleanOption showPCount = new BooleanOption("debugcounters.pcount", false);

	public DebugCountersHud() {
		super(115, 32, true);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		if (client.level == null) {
			renderPlaceholderComponent(graphics, delta);
		}
		DrawPosition pos = getPos();
		int lineY = pos.y() + 2;
		int lineX = pos.x() + 1;

		int xEnd = lineX + 50;
		if (showCCount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, client.levelRenderer.getSectionStatistics(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showECount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, client.levelRenderer.getEntityStatistics(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showPCount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, "P: " + client.particleEngine.countParticles(), lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}

		boolean boundsChanged = false;
		if (lineY != getHeight() + pos.y()) {
			boundsChanged = true;
			setHeight(lineY - pos.y());
		}
		if (xEnd != pos.x() + getWidth()) {
			boundsChanged = true;
			setWidth(xEnd - pos.x());
		}
		if (boundsChanged) {
			onBoundsUpdate();
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		int lineY = pos.y() + 2;
		int lineX = pos.x() + 1;

		int xEnd = lineX + 50;
		if (showCCount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, "C: 186/15000 (s) D: 10, pC: 000, pU: 00, aB: 20", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showECount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, "E: 695/3001, SD: 12", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}
		if (showPCount.get()) {
			xEnd = Math.max(xEnd, graphics.drawString(client.font, "P: 200", lineX, lineY, textColor.get().toInt(), shadow.get()));
			lineY += 10;
		}

		boolean boundsChanged = false;
		if (lineY != getHeight() + pos.y()) {
			boundsChanged = true;
			setHeight(lineY - pos.y());
		}
		if (xEnd != pos.x() + getWidth()) {
			boundsChanged = true;
			setWidth(xEnd - pos.x());
		}
		if (boundsChanged) {
			onBoundsUpdate();
		}
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(showCCount);
		options.add(showECount);
		options.add(showPCount);
		return options;
	}
}
