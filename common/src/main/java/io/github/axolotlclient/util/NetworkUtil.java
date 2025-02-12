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

package io.github.axolotlclient.util;

import java.net.http.HttpClient;
import java.time.Duration;

import com.github.mizosoft.methanol.Methanol;
import io.github.axolotlclient.AxolotlClientCommon;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NetworkUtil {

	public HttpClient createHttpClient(String id) {
		return Methanol.newBuilder().userAgent("AxolotlClient/" + id + " (" + AxolotlClientCommon.getUAVersionString() + ") contact: moehreag<at>gmail.com")
			.followRedirects(HttpClient.Redirect.NORMAL)
			.requestTimeout(Duration.ofMinutes(1))
			.executor(ThreadExecuter.service()).build();
	}
}
