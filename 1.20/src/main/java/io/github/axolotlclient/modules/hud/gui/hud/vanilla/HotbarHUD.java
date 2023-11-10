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
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.option.AttackIndicator;
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
	public void renderComponent(GuiGraphics graphics, float delta) {
		graphics.getMatrices().push();
		PlayerEntity playerEntity = MinecraftClient.getInstance().cameraEntity instanceof PlayerEntity
			? (PlayerEntity) MinecraftClient.getInstance().cameraEntity
			: null;
		if (playerEntity != null) {
			ItemStack itemStack = playerEntity.getOffHandStack();
			Arm arm = playerEntity.getMainArm().getOpposite();
			DrawPosition pos = getPos();
			int i = pos.x() + getWidth() / 2;
			graphics.getMatrices().push();
			graphics.getMatrices().translate(0.0F, 0.0F, -90.0F);
			graphics.drawTexture(WIDGETS_TEXTURE, i - 91, pos.y(), 0, 0, 182, 22);
			graphics.drawTexture(WIDGETS_TEXTURE, i - 91 - 1 + playerEntity.getInventory().selectedSlot * 20, pos.y() - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (arm == Arm.LEFT) {
					graphics.drawTexture(WIDGETS_TEXTURE, i - 91 - 29, pos.y() - 1, 24, 22, 29, 24);
				} else {
					graphics.drawTexture(WIDGETS_TEXTURE, i + 91, pos.y() - 1, 53, 22, 29, 24);
				}
			}

			graphics.getMatrices().pop();
			int l = 1;

			for (int m = 0; m < 9; ++m) {
				int n = i - 90 + m * 20 + 2;
				int o = pos.y() + 6 - 3;
				this.renderHotbarItem(graphics, n, o, delta, playerEntity, playerEntity.getInventory().main.get(m), l++);
			}

			if (!itemStack.isEmpty()) {
				int m = pos.y() + 6 - 3;
				if (arm == Arm.LEFT) {
					this.renderHotbarItem(graphics, i - 91 - 26, m, delta, playerEntity, itemStack, l++);
				} else {
					this.renderHotbarItem(graphics, i + 91 + 10, m, delta, playerEntity, itemStack, l++);
				}
			}

			RenderSystem.enableBlend();
			if (this.client.options.getAttackIndicator().get() == AttackIndicator.HOTBAR) {
				float f = this.client.player.getAttackCooldownProgress(0.0F);
				if (f < 1.0F) {
					int n = pos.y() + 2;
					int o = i + 91 + 6;
					if (arm == Arm.RIGHT) {
						o = i - 91 - 22;
					}

					int p = (int) (f * 19.0F);
					graphics.drawTexture(ICONS_TEXTURE, o, n, 0, 94, 18, 18);
					graphics.drawTexture(ICONS_TEXTURE, o, n + 18 - p, 18, 112 - p, 18, p);
				}
			}

			RenderSystem.disableBlend();
		}
		graphics.getMatrices().pop();
	}

	private void renderHotbarItem(GuiGraphics graphics, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) {
		if (!stack.isEmpty()) {
			float f = (float) stack.getCooldown() - tickDelta;
			if (f > 0.0F) {
				float g = 1.0F + f / 5.0F;
				graphics.getMatrices().push();
				graphics.getMatrices().translate((float) (x + 8), (float) (y + 12), 0.0F);
				graphics.getMatrices().scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
				graphics.getMatrices().translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
			}

			graphics.drawItem(player, stack, x, y, seed);
			if (f > 0.0F) {
				graphics.getMatrices().pop();
			}

			graphics.drawItemInSlot(this.client.textRenderer, stack, x, y);
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();

		drawCenteredString(graphics, MinecraftClient.getInstance().textRenderer, getName(), pos.x() + width / 2,
			pos.y() + height / 2 - 4, -1, true);
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
