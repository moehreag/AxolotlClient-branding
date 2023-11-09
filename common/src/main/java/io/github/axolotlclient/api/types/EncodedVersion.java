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

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class EncodedVersion {

	public static final EncodedVersion EMPTY = new EncodedVersion((byte) 0, (byte) 0, (byte) 0, (byte) 0);

	private final int major;
	private final int minor;
	private final byte patch;
	private final Flag flag;


	public EncodedVersion(byte major, byte minor, byte patch, byte flag){
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.flag = Flag.get(flag);
	}

	@Override
	public String toString(){
		return major +"."+ minor +"."+ patch + "-" + flag.getName();
	}

	public boolean isNewerThan(String other){
		String[] parts = other.split("\\.");
		int major = Integer.parseInt(parts[0]);

		if(this.major > major){
			return true;
		} else if (this.major < major){
			return false;
		}

		int minor = Integer.parseInt(parts[1]);

		if(this.minor > minor){
			return true;
		} else if (this.minor < minor){
			return false;
		}

		int patch = Integer.parseInt(parts[2].split("-")[0].split("\\+")[0]);

		if (this.patch > patch){
			return true;
		} else if (this.patch < patch){
			return false;
		}

		return false;
	}
	@RequiredArgsConstructor
	@Getter
	public enum Flag {
		SNAPSHOT(0x01, "SNAPSHOT"),
		PRERELEASE(0x02, "PRERELEASE"),
		RELEASE(0x04, ""),
		DEBUG(0x08, "DEBUG"),
		EXPERIMENTAL(0x10, "EXPERIMENTAL"),
		CUSTOM(0x20, "CUSTOM")
		;

		private final int id;
		private final String name;

		private static final Map<Integer, Flag> map = Arrays.stream(values()).collect(Collectors.toMap(Flag::getId, f -> f));

		public static Flag get(byte b){
			return map.get((int)b);
		}
	}
}
