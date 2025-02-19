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

package io.github.axolotlclient.modules.auth;

import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Graphics;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.GraphicsImpl;
import io.nayuki.qrcodegen.QrCode;
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

	public Graphics getQrCode() {
		QrCode qr = QrCode.encodeText(getVerificationUri() + "?code=" + getUserCode(), QrCode.Ecc.MEDIUM);
		int size = qr.size;
		int borderWidth = 1;
		Graphics gr = new GraphicsImpl(size + borderWidth * 2, size + borderWidth * 2);
		for (int x = 0; x < gr.getWidth(); x++) {
			for (int y = 0; y < gr.getHeight(); y++) {
				gr.setPixelColor(x, y, qr.getModule(x - borderWidth, y - borderWidth) ? Colors.BLACK : Colors.WHITE);
			}
		}
		return gr;
	}

	public interface StatusConsumer {
		void emit(String status);
	}
}
