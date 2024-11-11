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

import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public record SemVer(int major, int minor, int patch, String prerelease, String build) {
	public static final SemVer EMPTY = new SemVer(0, 0, 0, null, null);
	// Adapted from https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
	private static final Pattern SEMVER_PATTERN = Pattern.compile("^(?<major>0|[1-9]\\d*)\\." +
																  "(?<minor>0|[1-9]\\d*)" +
																  "(\\.(?<patch>0|[1-9]\\d*))?" +
																  "(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
																  "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
																  "(?:\\+(?<buildmetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

	public static SemVer parse(String version) {
		Matcher matcher = SEMVER_PATTERN.matcher(version);
		if (!matcher.find()) {
			return EMPTY;
		}

		int major = Integer.parseInt(matcher.group("major"));
		int minor = Integer.parseInt(matcher.group("minor"));
		// minecraft treats the `patch` component as optional...
		@Nullable String patchString = matcher.group("patch");
		int patch = patchString == null ? 0 : Integer.parseInt(patchString);
		String prerelease = matcher.group("prerelease");
		String buildmetadata = matcher.group("buildmetadata");
		return new SemVer(major, minor, patch, prerelease, buildmetadata);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(major).append(".").append(minor).append(".").append(patch);
		if (prerelease != null) {
			b.append("-").append(prerelease);
		}
		if (build != null) {
			b.append("+").append(build);
		}
		return b.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SemVer s) {
			return compareTo(s) == 0;
		}
		return toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch, prerelease);
	}

	public int compareTo(@NonNull SemVer o) {
		int i;
		List<IntSupplier> suppliers = List.of(
			() -> Integer.compare(major, o.major()),
			() -> Integer.compare(minor, o.minor()),
			() -> Integer.compare(patch, o.patch()),
			() -> prerelease != null ? o.prerelease() != null ? 0 : -1 : o.prerelease() != null ? 1 : 0
		);
		for (IntSupplier comparison : suppliers) {
			if ((i = comparison.getAsInt()) != 0) {
				return i;
			}
		}

		if (prerelease == null) {
			return 0;
		}

		String[] self = prerelease.split("\\.");
		String[] other = o.prerelease().split("\\.");

		for (int index = 0; index < Math.min(self.length, other.length); index++) {
			boolean selfNumeric = self[index].matches("\\d+");
			boolean otherNumeric = other[index].matches("\\d+");
			if (selfNumeric != otherNumeric) {
				return selfNumeric ? -1 : 1;
			} else if (!selfNumeric) {
				if ((i = self[index].compareTo(other[index])) != 0) {
					return i;
				}
			}
		}
		return Integer.compare(self.length, other.length);
	}

	public boolean isNewerThan(String other) {
		String[] parts = other.split("\\.");
		int major = Integer.parseInt(parts[0]);

		if (this.major > major) {
			return true;
		} else if (this.major < major) {
			return false;
		}

		int minor = Integer.parseInt(parts[1]);

		if (this.minor > minor) {
			return true;
		} else if (this.minor < minor) {
			return false;
		}

		int patch = Integer.parseInt(parts[2].split("-")[0].split("\\+")[0]);

		if (this.patch > patch) {
			return true;
		} else if (this.patch < patch) {
			return false;
		}

		return false;
	}
}
