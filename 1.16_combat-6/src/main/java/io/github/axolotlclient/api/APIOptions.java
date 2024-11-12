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

import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.screen.ConfigScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.UserRequest;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

public class APIOptions extends Options {

	@Getter
	private static final Options Instance = new APIOptions();

	@Override
	public void init() {
		super.init();
		MinecraftClient client = MinecraftClient.getInstance();

		openPrivacyNoteScreen = n ->
			client.execute(() -> client.openScreen(new PrivacyNoticeScreen(client.currentScreen, n)));
		KeyBinding binding = new KeyBinding("api.friends.sidebar.open", GLFW.GLFW_KEY_O, "category.axolotlclient");
		KeyBindingHelper.registerKeyBinding(binding);
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (binding.wasPressed() && API.getInstance().isAuthenticated()) {
				c.openScreen(new FriendsSidebar(c.currentScreen));
			}
		});
		category.add(new GenericOption("viewFriends", "clickToOpen",
			() -> client.openScreen(new FriendsScreen(client.currentScreen))));
		category.add(new GenericOption("viewChats", "clickToOpen",
			() -> client.openScreen(new ChatListScreen(client.currentScreen))));
		account.add(new GenericOption("api.account.usernames", "clickToOpen",
			() -> client.openScreen(new UsernameManagementScreen(client.currentScreen))));
		account.add(new GenericOption("api.account.delete", "api.account.delete_account", () -> {
			Screen previous = client.currentScreen;
			client.openScreen(new ConfirmScreen(b -> {
				if (b) {
					UserRequest.delete().thenAccept(r -> {
						if (r) {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.success", "api.account.deletion.success.desc");
						} else {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.failure", "api.account.deletion.failure.desc");
						}
						enabled.set(false);
					});
				}
				client.openScreen(previous);
			}, new TranslatableText("api.account.confirm_deletion"), new TranslatableText("api.account.confirm_deletion.desc")));
		}));
		Consumer<Boolean> consumer = settingUpdated;
		settingUpdated = b -> {
			if (client.currentScreen instanceof ConfigScreen) {
				consumer.accept(b);
			}
		};
		if (Constants.ENABLED) {
			AxolotlClient.CONFIG.addCategory(category);
			AxolotlClient.config.add(privacyAccepted);
		}
	}
}
