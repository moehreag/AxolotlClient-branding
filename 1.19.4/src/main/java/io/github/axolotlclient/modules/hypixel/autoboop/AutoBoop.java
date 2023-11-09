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

package io.github.axolotlclient.modules.hypixel.autoboop;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import lombok.Getter;

// Based on https://github.com/VeryHolyCheeeese/AutoBoop/blob/main/src/main/java/autoboop/AutoBoop.java
public class AutoBoop implements AbstractHypixelMod {

	@Getter
	private final static AutoBoop Instance = new AutoBoop();

	protected final OptionCategory cat = new OptionCategory("autoBoop");
	protected final BooleanOption enabled = new BooleanOption("enabled", "autoBoop", false);

	@Override
	public void init() {
		cat.add(enabled);
		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(this::onMessage);
	}

	@Override
	public OptionCategory getCategory() {
		return cat;
	}

	public void onMessage(ReceiveChatMessageEvent event) {
		String message = event.getOriginalMessage();
		if (enabled.get() && message.contains("Friend >") && message.contains("joined.")) {
			String player = message.substring(message.indexOf(">"),
				message.lastIndexOf(" "));
			Util.sendChatMessage("/boop " + player);
		}
	}
}
