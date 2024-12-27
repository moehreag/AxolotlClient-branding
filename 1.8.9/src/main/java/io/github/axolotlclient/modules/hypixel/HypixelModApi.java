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

package io.github.axolotlclient.modules.hypixel;

import io.github.axolotlclient.api.Request;
import io.github.axolotlclient.api.requests.StatusUpdate;
import net.hypixel.data.type.ServerType;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;


public class HypixelModApi {
    private ClientboundLocationPacket current;

    public void init() {
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, packet -> current = packet);
        ClientConnectionEvents.PLAY_READY.register(mc -> HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class));
    }

    public Request getStatus() {
		if (current == null) return null;
        return StatusUpdate.inGame(StatusUpdate.SupportedServer.HYPIXEL, current.getServerType().map(ServerType::getName).orElse(""), current.getMode().orElse(""), current.getMap().orElse(""));
    }
}
