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
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.axolotlclient.AxolotlClientCommon;
import org.jetbrains.annotations.Nullable;


public class Watcher implements AutoCloseable {
	private static final ScheduledExecutorService thread = Executors.newSingleThreadScheduledExecutor();
	private final WatchService watcher;
	private final Path path;

	public Watcher(Path root) throws IOException {
		this.path = root;
		this.watcher = path.getFileSystem().newWatchService();

		try {
			this.watchDir(path);
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {

				for (Path path : directoryStream) {
					if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
						this.watchDir(path);
					}
				}
			}
		} catch (Exception e) {
			this.watcher.close();
			throw e;
		}
	}

	@Nullable
	public static Watcher create(Path path) {
		try {
			Files.createDirectories(path);
			return new Watcher(path);
		} catch (IOException var2) {
			AxolotlClientCommon.getInstance().getLogger().warn("Failed to initialize directory {} monitoring", path, var2);
			return null;
		}
	}

	public static Watcher createSelfTicking(Path path, Runnable onUpdate) {
		var watcher = create(path);
		if (watcher != null) {
			thread.scheduleAtFixedRate(() -> {
				try {
					if (watcher.pollForChanges()) {
						onUpdate.run();
					}
				} catch (IOException e) {
					try {
						watcher.close();
					} catch (IOException ignored) {
					}
				}
			}, 100, 100, TimeUnit.MILLISECONDS);
		}
		return watcher;
	}

	private void watchDir(Path path) throws IOException {
		path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	}

	public boolean pollForChanges() throws IOException {
		boolean bl = false;

		WatchKey watchKey;
		while ((watchKey = this.watcher.poll()) != null) {
			for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
				bl = true;
				if (watchKey.watchable() == this.path && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					Path path = this.path.resolve((Path) watchEvent.context());
					if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
						this.watchDir(path);
					}
				}
			}

			watchKey.reset();
		}

		return bl;
	}

	public void close() throws IOException {
		this.watcher.close();
	}

	public void safeClose() {
		try {
			close();
		} catch (IOException ignored) {
		}
	}

	public static void close(Watcher watcher) {
		if (watcher != null) {
			watcher.safeClose();
		}
	}
}
