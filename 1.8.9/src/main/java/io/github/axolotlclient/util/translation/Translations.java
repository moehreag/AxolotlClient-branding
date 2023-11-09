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

package io.github.axolotlclient.util.translation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/**
 * This file is derived from <a href="https://github.com/Sol-Client/Client">Sol Client</a>.
 * <p>
 * License: GPL-3.0
 *
 * @author TheKodeToad
 */

public class Translations implements TranslationProvider {

	@Getter
	private static final TranslationProvider Instance = new Translations();

	private static final JsonParser parser = new JsonParser();

	private static final Map<String, String> TRANSLATIONS = new HashMap<>();

	public static boolean hasTranslation(String key) {
		return TRANSLATIONS.containsKey(key);
	}

	public static Identifier getLanguageId(String id) {
		return new Identifier("axolotlclient", "lang/" + id + ".json");
	}

	public static void load() {
		clear();

		try {
			MinecraftClient mc = MinecraftClient.getInstance();
			ResourceManager resourceManager = mc.getResourceManager();

			List<Resource> resources = new ArrayList<>();

			resources.addAll(resourceManager.getAllResources(new Identifier("axolotlclient", "lang/en_us.json")));
			resources.addAll(resourceManager.getAllResources(
				new Identifier("axolotlclient", "lang/" + mc.options.language.toLowerCase(Locale.ROOT) + ".json")));

			for (Resource resource : resources) {
				try (InputStream in = resource.getInputStream()) {
					accept(in);
				}
			}
		} catch (IOException ignored) {
		}
	}

	public static void clear() {
		TRANSLATIONS.clear();
	}

	public static void accept(InputStream json) {
		JsonObject obj = parser.parse(new InputStreamReader(json)).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			TRANSLATIONS.put(key, value);
		}
	}

	/**
	 * A version of {@link String#format(String, Object...)} that doesn't allocate an object if there are no arguments passed.
	 *
	 * @param fmt  The format.
	 * @param args The args.
	 * @return The formatted string.
	 */
	public static String format(String fmt, Object... args) {
		return args.length == 0 ? fmt : String.format(fmt, args);
	}

	public String translate(String key, Object... args) {
		String translated = TRANSLATIONS.getOrDefault(key, "axolotlclient." + key);
		if (translated.contains("%s")) {
			for (Object arg : args) {
				translated = translated.replaceFirst("%s", arg.toString());
			}
		}
		return translated;
	}
}
