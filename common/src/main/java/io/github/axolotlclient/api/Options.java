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

package io.github.axolotlclient.api;

import java.util.function.Consumer;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.api.requests.AccountSettingsRequest;
import io.github.axolotlclient.api.types.AccountSettings;
import io.github.axolotlclient.api.types.PkSystem;
import io.github.axolotlclient.modules.Module;
import io.github.axolotlclient.util.ThreadExecuter;

public abstract class Options implements Module {

	protected Consumer<Consumer<Boolean>> openPrivacyNoteScreen = v -> {
	};
	public EnumOption<PrivacyPolicyState> privacyAccepted = new EnumOption<>("privacyPolicyAccepted", PrivacyPolicyState.class, PrivacyPolicyState.UNSET, val -> AxolotlClientCommon.getInstance().saveConfig());
	public final BooleanOption statusUpdateNotifs = new BooleanOption("statusUpdateNotifs", true);
	public final BooleanOption friendRequestsEnabled = new BooleanOption("friendRequestsEnabled", true);
	public final BooleanOption channelInvitesEnabled = new BooleanOption("api.channels.invites.enabled", false);
	public final BooleanOption detailedLogging = new BooleanOption("detailedLogging", false);
	public final BooleanOption enabled = new BooleanOption("enabled", true, value -> {
		if (value) {
			if (!privacyAccepted.get().isAccepted()) {
				openPrivacyNoteScreen.accept(v -> {
					if (v) ThreadExecuter.scheduleTask(() -> API.getInstance().restart());
				});
			} else {
				ThreadExecuter.scheduleTask(() -> API.getInstance().restart());
			}
		} else {
			ThreadExecuter.scheduleTask(() -> API.getInstance().shutdown());
		}
	});
	public final BooleanOption updateNotifications = new BooleanOption("api.update_notifications", false);
	public final BooleanOption displayNotes = new BooleanOption("api.display_notes", true);
	public final BooleanOption addShortcutButtons = new BooleanOption("api.add_shortcut_buttons", true);
	public final StringOption pkToken = new StringOption("api.pk_token", "", s ->
		PkSystem.fromToken(s).thenAccept(sys -> {
			if (sys != null) {
				API.getInstance().getSelf().setSystem(sys);
			}
		}));
	public final BooleanOption autoproxy = new BooleanOption("api.pk_autoproxy", false);
	public final EnumOption<PkSystem.ProxyMode> autoproxyMode = new EnumOption<>("api.pk_proxymode", PkSystem.ProxyMode.class, PkSystem.ProxyMode.PROXY_OFF);
	public final StringOption autoproxyMember = new StringOption("api.pk_autoproxy_member", "", s -> {
		if (API.getInstance().getSelf() != null && API.getInstance().getSelf().getSystem() != null) {
			API.getInstance().getSelf().getSystem().updateAutoproxyMember(s);
		}
	});
	Consumer<Boolean> settingUpdated = b -> {
	};
	public final BooleanOption showRegistered = new BooleanOption("api.account.settings.show_registered", true, settingUpdated::accept);
	public final BooleanOption retainUsernames = new BooleanOption("api.account.settings.retain_usernames", true, settingUpdated::accept);
	public final BooleanOption showLastOnline = new BooleanOption("api.account.settings.show_last_online", true, settingUpdated::accept);
	public final BooleanOption showActivity = new BooleanOption("api.account.settings.show_activity", true, settingUpdated::accept);
	public final BooleanOption allowFriendsImageAccess = new BooleanOption("api.account.settings.allow_friends_image_access", true, settingUpdated::accept);

	protected final OptionCategory category = OptionCategory.create("api.category");
	protected final OptionCategory pluralkit = OptionCategory.create("api.pluralkit");
	protected final OptionCategory account = OptionCategory.create("api.account").includeInParentTree(false);

	@Override
	public void init() {
		settingUpdated = b -> AccountSettingsRequest.update(new AccountSettings(
			showRegistered.get(),
			retainUsernames.get(),
			showLastOnline.get(),
			showActivity.get(),
			allowFriendsImageAccess.get()
		));
		pkToken.setMaxLength(65);
		pluralkit.add(pkToken, autoproxy, autoproxyMode, autoproxyMember);
		account.add(showRegistered, retainUsernames, showLastOnline, showActivity, allowFriendsImageAccess);
		category.add(pluralkit, account);
		category.add(enabled, friendRequestsEnabled, statusUpdateNotifs, channelInvitesEnabled, detailedLogging, updateNotifications, displayNotes, addShortcutButtons);
	}

	public enum PrivacyPolicyState {
		UNSET,
		ACCEPTED(){
			@Override
			public boolean isAccepted() {
				return true;
			}
		},
		DENIED
		;
		public boolean isAccepted() {
			return false;
		}
	}
}
