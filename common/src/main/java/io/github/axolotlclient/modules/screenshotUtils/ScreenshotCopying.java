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

package io.github.axolotlclient.modules.screenshotUtils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.github.axolotlclient.AxolotlClientCommon;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

@UtilityClass
public class ScreenshotCopying {

	static {
		boolean wayland = false;
		try {
			wayland = GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
		} catch (Throwable ignored) {
		}
		IS_WAYLAND = wayland;
	}

	private static final boolean IS_WAYLAND;

	public void copy(File file) {
		if (IS_WAYLAND) {
			copyWayland(file);
		} else {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new FileTransferable(file), null);
		}
	}

	private void copyWayland(File f) {
		try {
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "wl-copy -t image/png < " + f.getAbsolutePath());
			Process p = builder.start();
			p.waitFor();
		} catch (IOException | InterruptedException ignored) {
			AxolotlClientCommon.getInstance().getLogger().error("Failed to invoke 'wl-copy'!\nMake sure 'wl-clipboard' is installed and accessible!");
		}
	}

	@AllArgsConstructor
	protected static class FileTransferable implements Transferable {

		private final File file;

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) {
			final ArrayList<File> files = new ArrayList<>();
			files.add(file);
			return files;
		}
	}
}
