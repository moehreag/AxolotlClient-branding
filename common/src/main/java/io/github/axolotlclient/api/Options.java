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

import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.KeyBindOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.util.ThreadExecuter;

public abstract class Options implements Module {

	protected Consumer<Consumer<Boolean>> openPrivacyNoteScreen = v -> {
	};

	public EnumOption privacyAccepted = new EnumOption("privacyPolicyAccepted", new String[]{"unset", "accepted", "denied"}, "unset");

	public final BooleanOption enabled = new BooleanOption("enabled", value -> {
		if (value) {
			if (!privacyAccepted.get().equals("accepted")) {
				openPrivacyNoteScreen.accept(v -> {
					if (v) ThreadExecuter.scheduleTask(() -> API.getInstance().restart());
				});
			} else {
				ThreadExecuter.scheduleTask(() -> API.getInstance().restart());
			}
		} else {
			ThreadExecuter.scheduleTask(() -> API.getInstance().shutdown());
		}
	}, true);
	public final BooleanOption statusUpdateNotifs = new BooleanOption("statusUpdateNotifs", true);
	public final BooleanOption friendRequestsEnabled = new BooleanOption("friendRequestsEnabled", true);
	public final BooleanOption detailedLogging = new BooleanOption("detailedLogging", false);
	public final BooleanOption updateNotifications = new BooleanOption("api.update_notifications", true);
	public final BooleanOption displayNotes = new BooleanOption("api.display_notes", true);
	protected final OptionCategory category = new OptionCategory("api.category");
	public KeyBindOption openSidebar;

	@Override
	public void init() {
		category.add(enabled, friendRequestsEnabled, statusUpdateNotifs, detailedLogging, updateNotifications, displayNotes);
	}
}
