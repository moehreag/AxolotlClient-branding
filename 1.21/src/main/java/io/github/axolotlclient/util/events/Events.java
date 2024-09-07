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

package io.github.axolotlclient.util.events;

import java.util.Arrays;

import io.github.axolotlclient.util.events.impl.*;
import org.quiltmc.qsl.base.api.event.Event;

public class Events {

	public static final Event<EventCallback<MouseInputEvent>> MOUSE_INPUT = createEvent();
	public static final Event<EventCallback<KeyBindChangeEvent>> KEYBIND_CHANGE = createEvent();
	public static final Event<EventCallback<KeyPressEvent>> KEY_PRESS = createEvent();
	public static final Event<EventCallback<PlayerDirectionChangeEvent>> PLAYER_DIRECTION_CHANGE = createEvent();
	public static final Event<EventCallback<ScoreboardRenderEvent>> SCOREBOARD_RENDER_EVENT = createEvent();
	public static final Event<EventCallback<ReceiveChatMessageEvent>> RECEIVE_CHAT_MESSAGE_EVENT = createEvent();
	public static final Event<EventCallback<WorldLoadEvent>> WORLD_LOAD_EVENT = createEvent();

	private static <T> Event<EventCallback<T>> createEvent() {
		return Event
			.create(EventCallback.class, listeners -> (event) ->
				Arrays.stream(listeners).forEach(l -> l.invoke(event)));
	}

	public interface EventCallback<T> {
		void invoke(T parameters);
	}
}
