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

package io.github.axolotlclient.api;

import java.nio.file.Path;
import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.api.ui.screen.ConfigScreen;
import io.github.axolotlclient.api.chat.ChannelInvitesScreen;
import io.github.axolotlclient.api.chat.ChatListScreen;
import io.github.axolotlclient.api.requests.AccountDataRequest;
import io.github.axolotlclient.api.requests.AccountSettingsRequest;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.options.GenericOption;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.lwjgl.input.Keyboard;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class APIOptions extends Options {

	@Getter
	private static final Options Instance = new APIOptions();

	@Override
	public void init() {
		super.init();
		Minecraft client = Minecraft.getInstance();

		openPrivacyNoteScreen = n ->
			client.openScreen(new PrivacyNoticeScreen(client.screen, n));
		KeyBinding openSidebar = new KeyBinding("api.chats.sidebar.open", Keyboard.KEY_O, "category.axolotlclient");
		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> registry.register(openSidebar));
		MinecraftClientEvents.TICK_END.register(minecraft -> {
			if (openSidebar.consumeClick() && API.getInstance().isAuthenticated()) {
				minecraft.openScreen(new ChatsSidebar(client.screen));
			}
		});
		category.add(new GenericOption("viewFriends", "clickToOpen",
			() -> client.openScreen(new FriendsScreen(client.screen))));
		category.add(new GenericOption("viewChats", "clickToOpen",
			() -> client.openScreen(new ChatListScreen(client.screen))));
		category.add(new GenericOption("api.channels.invites.view", "clickToOpen",
			() -> client.openScreen(new ChannelInvitesScreen(client.screen))));
		account.add(new GenericOption("api.account.usernames", "clickToOpen",
			() -> client.openScreen(new UsernameManagementScreen(client.screen))));
		account.add(new GenericOption("api.account.export", "api.account.export_data", () -> ThreadExecuter.scheduleTask(() -> {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				var pointers = stack.pointers(stack.UTF8("*.json"));
				var result = TinyFileDialogs.tinyfd_saveFileDialog("Choose export destination", FabricLoader.getInstance().getGameDir().toString(), pointers, null);
				if (result != null) {
					AccountDataRequest.get(Path.of(result));
				}
			}
		})));
		account.add(new GenericOption("api.account.delete", "api.account.delete_account", () -> {
			Screen previous = client.screen;
			client.openScreen(new ConfirmScreen((b, i) -> {
				if (b) {
					AccountSettingsRequest.deleteAccount().thenAccept(r -> {
						if (r) {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.success", "api.account.deletion.success.desc");
						} else {
							API.getInstance().getNotificationProvider().addStatus("api.account.deletion.failure", "api.account.deletion.failure.desc");
						}
						enabled.set(false);
					});
				}
				client.openScreen(previous);
			}, I18n.translate("api.account.confirm_deletion"),
				I18n.translate("api.account.confirm_deletion.desc"), 0));
		}));
		Consumer<Boolean> consumer = settingUpdated;
		settingUpdated = b -> {
			if (client.screen instanceof ConfigScreen) {
				consumer.accept(b);
			}
		};
		if (Constants.ENABLED) {
			AxolotlClient.CONFIG.addCategory(category);
			AxolotlClient.config.add(privacyAccepted);
		}
	}
}
