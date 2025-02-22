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

package io.github.axolotlclient.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.multiplayer.report.chat.ChatReportingContext;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.util.PlayerKeyPairManager;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

	@Accessor("currentFps")
	static int axolotlclient$getCurrentFps() {
		return 0;
	}

	@Accessor("session")
	@Mutable
	void axolotlclient$setSession(Session session);

	@Accessor("socialInteractionsManager")
	@Mutable
	void axolotlclient$setSocialInteractionsManager(SocialInteractionsManager manager);

	@Accessor("playerKeyPairManager")
	@Mutable
	void axolotlclient$setPlayerKeyPairManager(PlayerKeyPairManager manager);

	@Accessor("chatReportingContext")
	@Mutable
	void axolotlclient$setChatReportingContext(ChatReportingContext context);

	@Accessor("userApiService")
	@Mutable
	void axolotlclient$setUserApiService(UserApiService service);

	@Accessor("authService")
	YggdrasilAuthenticationService getAuthService();
}
