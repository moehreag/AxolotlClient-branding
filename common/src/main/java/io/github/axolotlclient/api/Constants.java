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

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
	public final String API_URL = "api.axolotlclient.xyz"; // The URL of the endpoint
	public final int PORT = 20400; // The port of the endpoint
	public final int STATUS_UPDATE_DELAY = 15; // The Delay between Status updates, in seconds. Discord uses 15 seconds so we will as well.
	public final boolean TESTING = false; // When set to true, no requests will be sent
}
