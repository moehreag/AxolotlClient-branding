/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.lwjgl.opengl.Display;

public class GLFWUtil {

	private static MethodHandle getHandle;

	static {
		try {
			getHandle = MethodHandles.lookup().findStatic(Class.forName("org.lwjgl.opengl.Display"), "getHandle", MethodType.methodType(long.class));
		} catch (Throwable ignored) {
		}
	}

	private static long windowHandle = -1;

	public static long getWindowHandle() {
		if (windowHandle == -1) {
			try {
				windowHandle = (long) getHandle.invoke();
			} catch (Throwable ignored) {
			}
		}
		return windowHandle;
	}

	// Since the reflection used for this only works with legacy-lwjgl3 it's possible for us not being able to access the window handle.
	// This however should not lead to a crash despite compiling against glfw.
	public static boolean isHandleAvailable() {
		return getWindowHandle() != -1;
	}

	public static void runUsingGlfwHandle(Consumer<Long> action) {
		if (!Display.isCreated()) {
			MinecraftClientEvents.READY.register(mc -> {
				if (isHandleAvailable()) {
					action.accept(getWindowHandle());
				}
			});
		} else if (isHandleAvailable()) {
			action.accept(getWindowHandle());
		}
	}
}
