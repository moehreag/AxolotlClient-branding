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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import io.github.axolotlclient.AxolotlClientCommon;

public class OSUtil {

	private static OperatingSystem OS;

	public static OperatingSystem getOS() {
		if (OS == null) {
			String s = System.getProperty("os.name");

			for (OperatingSystem o : OperatingSystem.values()) {
				for (String v : o.getStrings()) {
					if (s.trim().toLowerCase(Locale.ROOT).contains(v)) {
						OS = o;
						return OS;
					}
				}
			}

			OS = OperatingSystem.OTHER;
		}
		return OS;
	}

	public enum OperatingSystem {

		WINDOWS("win") {
			@Override
			protected String[] getURLOpenCommand(URL url) {
				return new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
			}
		},
		LINUX("nix", "nux", "aix"),
		MAC("mac") {
			@Override
			protected String[] getURLOpenCommand(URL url) {
				return new String[]{"open", url.toString()};
			}
		},
		OTHER();

		final String[] s;

		OperatingSystem(String... detection) {
			this.s = detection;
		}

		public String[] getStrings() {
			return s;
		}

		public void open(URI uri) {
			try {
				this.open(uri.toURL());
			} catch (MalformedURLException var3) {
				AxolotlClientCommon.getInstance().getLogger().error("Couldn't open uri '{}'", uri, var3);
			}
		}

		private void open(URL url) {
			try {
				Process process = Runtime.getRuntime().exec(this.getURLOpenCommand(url));
				process.getInputStream().close();
				process.getErrorStream().close();
				process.getOutputStream().close();
			} catch (IOException var3) {
				AxolotlClientCommon.getInstance().getLogger().error("Couldn't open url '{}'", url, var3);
			}
		}

		public void open(String uri) {
			try {
				this.open(new URI(uri));
			} catch (IllegalArgumentException | URISyntaxException var3) {
				AxolotlClientCommon.getInstance().getLogger().error("Couldn't open uri '{}'", uri, var3);
			}
		}

		protected String[] getURLOpenCommand(URL url) {
			String string = url.toString();
			if ("file".equals(url.getProtocol())) {
				string = string.replace("file:", "file://");
			}

			return new String[]{"xdg-open", string};
		}
	}
}
