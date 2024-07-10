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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class GsonHelper {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static JsonObject fromJson(String s) {
		return GSON.fromJson(s, JsonObject.class);
	}

	public static Object read(JsonReader reader) throws IOException {
		switch (reader.peek()) {
			case BEGIN_ARRAY:
				List<Object> list = new ArrayList<>();

				reader.beginArray();

				while (reader.hasNext()) {
					list.add(read(reader));
				}

				reader.endArray();

				return list;
			case BEGIN_OBJECT:
				Map<String, Object> object = new LinkedHashMap<>();

				reader.beginObject();

				while (reader.hasNext()) {
					String key = reader.nextName();
					object.put(key, read(reader));
				}

				reader.endObject();

				return object;
			case STRING:
				return reader.nextString();
			case NUMBER:
				return reader.nextDouble();
			case BOOLEAN:
				return reader.nextBoolean();
			case NULL:
				return null;
			// Unused, probably a sign of malformed json
			case NAME:
			case END_DOCUMENT:
			case END_ARRAY:
			case END_OBJECT:
			default:
				throw new IllegalStateException();
		}
	}
}
