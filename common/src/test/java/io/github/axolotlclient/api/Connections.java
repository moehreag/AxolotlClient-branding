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

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.Channel;

public class Connections {
	private static final Map<String, Channel> uuidToChannel = new HashMap<>();
	private static final Map<Channel, String> channelToUuid = new HashMap<>();

	public static void put(String uuid, Channel channel){
		uuidToChannel.put(uuid, channel);
		channelToUuid.put(channel, uuid);
	}

	public static Channel get(String uuid){
		return uuidToChannel.get(uuid);
	}

	public static String get(Channel channel){
		return channelToUuid.get(channel);
	}

	public static void remove(String uuid){
		channelToUuid.remove(get(uuid));
		uuidToChannel.remove(uuid);
	}

	public static void remove(Channel channel){
		uuidToChannel.remove(get(channel));
		channelToUuid.remove(channel);
	}
}
