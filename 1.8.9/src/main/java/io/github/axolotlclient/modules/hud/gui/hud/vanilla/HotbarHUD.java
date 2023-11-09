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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class HotbarHUD extends TextHudEntry {

	public static final Identifier ID = new Identifier("axolotlclient", "hotbarhud");
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");

	public HotbarHUD() {
		super(182, 22, false);
	}

	@Override
	public void render(float delta) {
		if (this.client.getCameraEntity() instanceof PlayerEntity) {
			super.render(delta);
		}
	}

	@Override
	public void renderComponent(float delta) {
		PlayerEntity playerEntity = (PlayerEntity) this.client.getCameraEntity();
		if (playerEntity == null || playerEntity.inventory == null || playerEntity.inventory.main == null) {
			return;
		}
		DrawPosition pos = getPos();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		this.client.getTextureManager().bindTexture(WIDGETS_TEXTURE);

		float f = this.zOffset;
		this.zOffset = -90.0F;
		this.drawTexture(pos.x, pos.y, 0, 0, 182, 22);
		this.drawTexture(pos.x - 1 + playerEntity.inventory.selectedSlot * 20, pos.y - 1, 0, 22, 24, 22);
		this.zOffset = f;
		GlStateManager.enableRescaleNormal();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		DiffuseLighting.enable();

		for (int j = 0; j < 9; ++j) {
			int k = pos.x + j * 20 + 3;
			int l = pos.y + 3;
			if (playerEntity.inventory.main[j] != null) {
				ItemUtil.renderGuiItemModel(playerEntity.inventory.main[j], k, l);
				ItemUtil.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer,
					playerEntity.inventory.main[j], k, l, null, textColor.get().getAsInt(), shadow.get());
			}
		}

		DiffuseLighting.disable();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();
		drawCenteredString(MinecraftClient.getInstance().textRenderer, getName(), pos.x + width / 2,
			pos.y + height / 2 - 4, -1, true);
	}

	@Override
	public boolean movable() {
		return true;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public boolean overridesF3() {
		return true;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> list = new ArrayList<>();
		list.add(enabled);
		list.add(shadow);
		return list;
	}
}
