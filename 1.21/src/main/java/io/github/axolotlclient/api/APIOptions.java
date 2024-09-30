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

import com.mojang.blaze3d.platform.InputUtil;
import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.screen.ConfigScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.User;
import io.github.axolotlclient.util.keybinds.KeyBinds;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.Text;

public class APIOptions extends Options {

	@Getter
	private static final Options Instance = new APIOptions();

	@Override
	public void init() {
		super.init();
		MinecraftClient client = MinecraftClient.getInstance();

		openPrivacyNoteScreen = n ->
			client.execute(() -> client.setScreen(new PrivacyNoticeScreen(client.currentScreen, n)));
		KeyBinds.getInstance().registerWithSimpleAction(new KeyBind("api.friends.sidebar.open",
				InputUtil.KEY_O_CODE, "category.axolotlclient"),
			() -> client.setScreen(new FriendsSidebar(client.currentScreen)));
		category.add(new GenericOption("viewFriends", "clickToOpen",
			() -> client.setScreen(new FriendsScreen(client.currentScreen))));
		category.add(new GenericOption("viewChats", "clickToOpen",
			() -> client.setScreen(new ChatListScreen(client.currentScreen))));
		account.add(new GenericOption("api.account.delete", "api.account.delete_account", () -> {
			Screen previous = client.currentScreen;
			client.setScreen(new ConfirmScreen(b -> {
				if (b) {
					User.delete().thenAccept(r -> {
						if (r) {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.success", "api.account.deletion.success.desc");
						} else {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.failure", "api.account.deletion.failure.desc");
						}
						enabled.set(false);
					});
				}
				client.setScreen(previous);
			}, Text.translatable("api.account.confirm_deletion"), Text.translatable("api.account.confirm_deletion.desc")));
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
