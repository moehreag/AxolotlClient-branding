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

package io.github.axolotlclient.api;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.client.options.KeyBinding;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class APIOptions extends Options {

	@Getter
	private static final Options Instance = new APIOptions();

	@Override
	public void init() {
		super.init();
		Minecraft client = Minecraft.getInstance();

		openPrivacyNoteScreen = n ->
			client.openScreen(new PrivacyNoticeScreen(client.screen, n));
		KeyBinding openSidebar = new KeyBinding("api.friends.sidebar.open", Keyboard.KEY_O, "axolotlclient");
		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> registry.register(openSidebar));
		MinecraftClientEvents.TICK_END.register(minecraft -> {
			if (openSidebar.consumeClick()){
				minecraft.openScreen(new FriendsSidebar(client.screen));
			}
		});
		category.add(new GenericOption("viewFriends", "clickToOpen",
			() -> client.openScreen(new FriendsScreen(Minecraft.getInstance().screen))));
		category.add(new GenericOption("viewChats", "clickToOpen",
			() -> client.openScreen(new ChatListScreen(Minecraft.getInstance().screen))));
		AxolotlClient.CONFIG.addCategory(category);
		AxolotlClient.config.add(privacyAccepted);
	}
}
