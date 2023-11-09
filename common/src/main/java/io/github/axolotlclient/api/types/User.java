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

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.util.Serializer;
import io.github.axolotlclient.api.util.UUIDHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class User {

	@Serializer.Exclude
	private String name;
	@Serializer.Length(16)
	private String uuid;
	private Status status;

	public User(String uuid, Status status) {
		this.uuid = API.getInstance().sanitizeUUID(uuid);
		this.status = status;
		this.name = UUIDHelper.getUsername(uuid);
	}

	@Override
	public int hashCode() {
		int result = 1;
		String $name = this.getName();
		result = result * 59 + ($name == null ? 43 : $name.hashCode());
		String $uuid = this.getUuid();
		result = result * 59 + ($uuid == null ? 43 : $uuid.hashCode());
		Status $status = this.getStatus();
		result = result * 59 + ($status == null ? 43 : $status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof User) {
			return uuid.equals(((User) obj).getUuid());
		}
		return false;
	}
}
