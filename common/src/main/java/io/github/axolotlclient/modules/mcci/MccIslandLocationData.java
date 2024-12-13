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

package io.github.axolotlclient.modules.mcci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record MccIslandLocationData(String server, String proxy) {

	private static final Pattern PATTERN = Pattern.compile("^Server: ([^\n]+)\nProxy: ([^\n]+)\n.*$");
	public static MccIslandLocationData parse(String location) {
		Matcher matcher = PATTERN.matcher(location);
		if (!matcher.find()) {
			return null;
		}
		return new MccIslandLocationData(matcher.group(1), matcher.group(2));
	}
}
