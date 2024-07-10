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

package io.github.axolotlclient.modules.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class DeviceFlowData {
	@Getter
	private final String message;
	@Getter
	private final String verificationUri;
	@Getter
	private final String deviceCode;
	@Getter
	private final String userCode;
	@Getter
	private final int expiresIn;
	@Getter
	private final int interval;

	@Setter
	private StatusConsumer statusConsumer;

	public void setStatus(String status) {
		if (statusConsumer != null) {
			statusConsumer.emit(status);
		}
	}

	public interface StatusConsumer {
		void emit(String status);
	}
}
