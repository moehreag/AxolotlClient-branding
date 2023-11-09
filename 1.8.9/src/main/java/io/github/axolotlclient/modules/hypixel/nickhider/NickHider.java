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

package io.github.axolotlclient.modules.hypixel.nickhider;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.options.StringOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class NickHider implements AbstractHypixelMod {

	@Getter
	private static final NickHider Instance = new NickHider();
	public final StringOption hiddenNameSelf = new StringOption("hiddenNameSelf", "You");
	public final StringOption hiddenNameOthers = new StringOption("hiddenNameOthers", "Player");
	public final BooleanOption hideOwnName = new BooleanOption("hideOwnName", false);
	public final BooleanOption hideOtherNames = new BooleanOption("hideOtherNames", false);
	public final BooleanOption hideOwnSkin = new BooleanOption("hideOwnSkin", false);
	public final BooleanOption hideOtherSkins = new BooleanOption("hideOtherSkins", false);
	private final OptionCategory category = new OptionCategory("nickhider");

	@Override
	public void init() {
		category.add(hiddenNameSelf);
		category.add(hiddenNameOthers);
		category.add(hideOwnName);
		category.add(hideOtherNames);
		category.add(hideOwnSkin);
		category.add(hideOtherSkins);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}

	public Text editMessage(Text message) {
		if (hideOwnName.get() || hideOtherNames.get()) {
			String msg = message.asFormattedString();
			String playerName = MinecraftClient.getInstance().player.getGameProfile().getName();
			if (hideOwnName.get() && msg.contains(playerName)) {
				msg = msg.replaceAll(playerName, hiddenNameSelf.get());
			}

			if (hideOtherNames.get()) {
				for (PlayerEntity player : MinecraftClient.getInstance().world.playerEntities) {
					if (msg.contains(player.getGameProfile().getName())) {
						msg = msg.replaceAll(player.getGameProfile().getName(), hiddenNameOthers.get());
					}
				}
			}

			return new LiteralText(msg).setStyle(message.getStyle().deepCopy());
		}
		return message;
	}
}
