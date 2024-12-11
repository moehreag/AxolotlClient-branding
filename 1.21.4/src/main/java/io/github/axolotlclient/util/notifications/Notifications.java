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

package io.github.axolotlclient.util.notifications;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Notifications implements NotificationProvider {

	@Getter
	private static final Notifications Instance = new Notifications();

	public void addStatus(String titleKey, String descKey, Object... args) {
		addStatus(Component.translatable(titleKey, args), Component.translatable(descKey, args));
	}

	public void addStatus(Component title, Component description) {
		Minecraft.getInstance().getToastManager().addToast(AxolotlClientToast.multiline(Minecraft.getInstance(), title, description));
	}
}
