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

package io.github.axolotlclient.modules.hypixel.autoboop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringArrayOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.ThreadExecuter;
import lombok.Getter;

public abstract class AutoBoopCommon implements AbstractHypixelMod {

	private static final Pattern FRIEND_JOINED = Pattern.compile("^Friend > (\\b[A-Za-z0-9_§]{3,16}\\b) joined\\.$");

	public void handleMessage(String message) {
		ThreadExecuter.scheduleTask(() -> { // execute off-thread since the string manipulation for the filter list could potentially take a bit
			Matcher matcher;
			if (enabled.get() && (matcher = FRIEND_JOINED.matcher(message)).matches()) {
				String player = matcher.group(1);
				if (FilterListMode.fromId(filterListMode.get())
					.getFunc().apply(player, Arrays.stream(filterList.get().split(",")).map(String::trim).toList())) {
					CompletableFuture.runAsync(() -> {
						sendChatMessage("/boop " + player);
						AxolotlClientCommon.getInstance().getLogger().info("Booped " + player);
					}, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS, ThreadExecuter.service()));
				}
			}
		});
	}

	protected final OptionCategory cat = OptionCategory.create("autoboop");
	protected final BooleanOption enabled = new BooleanOption("enabled", "autoboop.enabled.tooltip", false);
	protected final StringOption filterList = new StringOption("autoboop.filterlist", "autoboop.filterlist.tooltip", "");
	protected final StringArrayOption filterListMode = new StringArrayOption("autoboop.filterlist.mode", Arrays.stream(FilterListMode.values()).map(FilterListMode::getId).toArray(String[]::new));

	@Override
	public void init() {
		cat.add(enabled);
		cat.add(filterListMode);
		cat.add(filterList);
	}

	@Override
	public OptionCategory getCategory() {
		return cat;
	}

	protected abstract void sendChatMessage(String message);

	@Getter
	private enum FilterListMode {
		DISABLED("disabled", (s, list) -> true),
		WHITELIST("whitelist", (s, list) -> list.contains(s)),
		BLACKLIST("blacklist", (s, list) -> !list.contains(s));

		private final String id;
		private final BiFunction<String, List<String>, Boolean> func;

		FilterListMode(String id, BiFunction<String, List<String>, Boolean> func) {
			this.id = "autoboop.filterlist.mode." + id;
			this.func = func;
		}

		private static final Map<String, FilterListMode> idMap = Arrays.stream(values()).collect(Collectors.toMap(m -> m.id, Function.identity()));

		public static FilterListMode fromId(String id) {
			FilterListMode mode = idMap.get(id);
			if (mode == null) {
				throw new IllegalArgumentException("Could not resolve mode: " + id);
			}
			return mode;
		}
	}
}
