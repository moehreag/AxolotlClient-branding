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

package io.github.axolotlclient.api.types;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import io.github.axolotlclient.api.API;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Status {
	private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");

	public static final Status UNKNOWN = new Status(false, null, null);

	private boolean online;
	private final Instant lastOnline;
	private final Activity activity;

	public String getDescription() {
		return activity.description.isEmpty() ? "" :
			API.getInstance().getTranslationProvider()
				.translate("api.status.description." + activity.description.toLowerCase(Locale.ROOT));
	}

	public String getTitle() {
		return activity.title.isEmpty() ? "" :
			API.getInstance().getTranslationProvider()
				.translate("api.status.title." + activity.title.toLowerCase(Locale.ROOT));
	}

	public String getLastOnline() {
		return lastOnline == null ? null :
			API.getInstance().getTranslationProvider()
				.translate("api.status.last_online", lastOnline.atZone(ZoneId.systemDefault()).format(format));
	}

	@AllArgsConstructor
	public static class Activity {
		private final String title;
		private final String description;
		private final Instant started;
	}
}
