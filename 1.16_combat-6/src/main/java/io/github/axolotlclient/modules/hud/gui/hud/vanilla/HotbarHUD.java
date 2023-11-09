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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.ItemUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.options.AttackIndicator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class HotbarHUD extends TextHudEntry {

	public static final Identifier ID = new Identifier("axolotlclient", "hotbarhud");
	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");

	public HotbarHUD() {
		super(182, 22, false);
	}

	@Override
	public void renderComponent(MatrixStack matrices, float delta) {
		matrices.push();
		PlayerEntity playerEntity = MinecraftClient.getInstance().cameraEntity instanceof PlayerEntity
			? (PlayerEntity) MinecraftClient.getInstance().cameraEntity
			: null;
		if (playerEntity != null) {
			//scale(matrices);
			DrawPosition pos = getPos();

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS_TEXTURE);
			RenderSystem.enableBlend();
			ItemStack itemStack = playerEntity.getOffHandStack();
			Arm arm = playerEntity.getMainArm().getOpposite();
			int j = this.getZOffset();
			this.setZOffset(-90);
			this.drawTexture(matrices, pos.x, pos.y, 0, 0, 182, 22);
			this.drawTexture(matrices, pos.x - 1 + playerEntity.inventory.selectedSlot * 20, pos.y - 1, 0, 22, 24,
				22);
			if (!itemStack.isEmpty()) {
				if (arm == Arm.LEFT) {
					this.drawTexture(matrices, pos.x - 29, pos.y - 1, 24, 22, 29, 24);
				} else {
					this.drawTexture(matrices, pos.x + width, pos.y - 1, 53, 22, 29, 24);
				}
			}

			this.setZOffset(j);

			for (int n = 0; n < 9; ++n) {
				int k = pos.x + n * 20 + 3;
				int l = pos.y + 3;
				ItemUtil.renderGuiItemModel(getScale(), playerEntity.inventory.main.get(n), k, l);
				ItemUtil.renderGuiItemOverlay(matrices, client.textRenderer, playerEntity.inventory.main.get(n), k,
					l, null, -1, true);
			}

			if (!itemStack.isEmpty()) {
				if (arm == Arm.LEFT) {
					ItemUtil.renderGuiItemModel(getScale(), itemStack, pos.x - 26, pos.y + 3);
					ItemUtil.renderGuiItemOverlay(matrices, client.textRenderer, itemStack, pos.x - 26, pos.y + 3, null,
						-1, true);
				} else {
					ItemUtil.renderGuiItemModel(getScale(), itemStack, pos.x + width + 10, pos.y + 3);
					ItemUtil.renderGuiItemOverlay(matrices, client.textRenderer, itemStack, pos.x + width + 10,
						pos.y + 3, null, -1, true);
				}
			}

			if (this.client.options.attackIndicator == AttackIndicator.HOTBAR) {
				float f = this.client.player.getAttackCooldownProgress(0.0F);
				if (f < 1.0F) {
					int o = pos.y + 2;
					int p = pos.x + width + 6;
					if (arm == Arm.RIGHT) {
						p = pos.x - 22;
					}

					MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
					int q = (int) (f * 19.0F);
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					this.drawTexture(matrices, p, o, 0, 94, 18, 18);
					this.drawTexture(matrices, p, o + 18 - q, 18, 112 - q, 18, q);
				}
			}

			RenderSystem.disableBlend();
		}
		matrices.pop();
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
		DrawPosition pos = getPos();

		drawCenteredString(matrices, MinecraftClient.getInstance().textRenderer, getName(), pos.x + width / 2,
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
		return list;
	}
}
