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

public class Keyword {
	public static String get(String formatString) {
		StringBuilder result = new StringBuilder();

		StringBuilder keyword = new StringBuilder();

		StringBuilder replacement = new StringBuilder();
		List<String> replacements = new ArrayList<>();

		boolean inSection = false;
		boolean escape = false;
		boolean inKeywords = false;
		for (char c : formatString.toCharArray()) {

			if (c == '\\' && !escape) {
				escape = true;
				continue;
			} else if (inSection) {
				if (c == ']' && !escape) {
					result.append(
						API.getInstance().getTranslationProvider()
							.translate(keyword.toString(), (Object[]) replacements.toArray(new String[0])));
					inSection = false;
				} else if (c == ':' && !escape) {
					inKeywords = true;
					if (replacement.length() != 0) {
						replacements.add(replacement.toString());
						replacement = new StringBuilder();
					}
				} else if (!inKeywords) {
					keyword.append(c);
				} else {
					replacement.append(c);
				}
			} else {
				if (c == '[' && !escape) {
					inSection = true;
				}
				result.append(c);
			}
			escape = false;
		}
		return result.toString();
	}
}
