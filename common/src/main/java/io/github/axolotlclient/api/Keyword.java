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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Keyword {
	private static final Pattern PATTERN = Pattern.compile("\\[(\\w*):(.*)]");

	public static String get(String formatString) {
		if (formatString.contains(" | ")) {
			StringBuilder builder = new StringBuilder();
			for (String s : formatString.split(" \\| ")) {
				if (builder.length() != 0) {
					builder.append(apply(s));
				}
			}
			return builder.toString();
		}
		return apply(formatString);
	}

	private static String apply(String formatString) {
		Matcher matcher = PATTERN.matcher(formatString);
		if (!matcher.find()) {
			return formatString;
		}
		String keyword = matcher.group(1);
		String[] replacements = matcher.group(2).split(":");
		List<String> replaceParts = new ArrayList<>();
		for (String s : replacements) {
			int size = replaceParts.size();
			if (size > 0) {
				String prev = replaceParts.get(size - 1);
				int prevLength = prev.length();
				if (prev.charAt(prevLength - 1) == '\\') {
					replaceParts.set(size - 1, prev.substring(0, prevLength - 1) + ":" + s);
				} else {
					replaceParts.add(s);
				}
			} else {
				replaceParts.add(s);
			}
		}
		return API.getInstance().getTranslationProvider().translate(keyword, replaceParts.toArray());
	}
}
