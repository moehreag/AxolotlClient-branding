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

package io.github.axolotlclient.util;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.requests.User;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class BadgeRenderer {
	public static void renderNametagBadge(Entity entity) {
		if (entity instanceof PlayerEntity && !entity.isSneaking()) {
			if (AxolotlClient.CONFIG.showBadges.get() && User.getOnline(entity.getUuid().toString())) {
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableDepthTest();
				GlStateManager.enableAlphaTest();
				MinecraftClient.getInstance().getTextureManager().bindTexture(AxolotlClient.badgeIcon);

				int x = -(MinecraftClient.getInstance().textRenderer
					.getStringWidth(entity.getUuid() == MinecraftClient.getInstance().player.getUuid()
						? (NickHider.getInstance().hideOwnName.get() ? NickHider.getInstance().hiddenNameSelf.get()
						: entity.getName().asFormattedString())
						: (NickHider.getInstance().hideOtherNames.get() ? NickHider.getInstance().hiddenNameOthers.get()
						: entity.getName().asFormattedString()))
					/ 2
					+ (AxolotlClient.CONFIG.customBadge.get() ? MinecraftClient.getInstance().textRenderer
					.getStringWidth(" " + AxolotlClient.CONFIG.badgeText.get()) : 10));

				GlStateManager.color(1, 1, 1, 1);

				if (AxolotlClient.CONFIG.customBadge.get())
					MinecraftClient.getInstance().textRenderer.draw(AxolotlClient.CONFIG.badgeText.get(), x, 0, -1,
						AxolotlClient.CONFIG.useShadows.get());
				else
					DrawableHelper.drawTexture(x, 0, 0, 0, 8, 8, 8, 8);
			}
		}
	}
}
