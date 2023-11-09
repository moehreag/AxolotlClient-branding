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
import java.util.Locale;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.Serializer;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Status {

	public static Status UNKNOWN = new Status(false, "", "", "", Instant.EPOCH);

	private boolean online;
	@Serializer.Length(64)
	private String title;
	@Serializer.Length(64)
	private String description;
	@Serializer.Length(32)
	private String icon;
	private Instant startTime;

	public String getDescription() {
		return description.isEmpty() ? "" : API.getInstance().getTranslationProvider().translate("api.status.description." + description.toLowerCase(Locale.ROOT));
	}

	public String getTitle() {
		return title.isEmpty() ? "" : API.getInstance().getTranslationProvider().translate("api.status.title." + title.toLowerCase(Locale.ROOT));
	}
}
