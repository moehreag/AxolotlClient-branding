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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public interface ImageInstance {

	ResourceLocation id();

	NativeImage image();

	String filename();

	interface Local extends ImageInstance {
		Path location();

		default ImageInstance toShared(String url, String uploader, Instant sharedAt) {
			return new SharedImpl(id(), image(), filename(), location(), url, uploader, sharedAt);
		}
	}

	interface Remote extends ImageInstance {
		String url();
		String uploader();
		Instant sharedAt();

		default ImageInstance toShared(Path saved) {
			return new SharedImpl(id(), image(), filename(), saved, url(), uploader(), sharedAt());
		}
	}

	private static void register(ResourceLocation id, NativeImage img) {
		Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(img));
	}

	record LocalImpl(ResourceLocation id, NativeImage image, String filename, Path location) implements Local {
		public LocalImpl(NativeImage image, String filename, Path location) {
			this(ResourceLocation.fromNamespaceAndPath("axolotlclient", "gallery_local_" + Hashing.sha256().hashUnencodedChars(location.toString().toLowerCase(Locale.ROOT).replaceAll("[./]", "_"))),
				image, filename, location);
			register(id(), image());
		}

		public LocalImpl(Path p) throws IOException {
			this(NativeImage.read(Files.newInputStream(p)), p.getFileName().toString(), p);
		}
	}

	record SharedImpl(ResourceLocation id, NativeImage image, String filename, Path location, String url, String uploader, Instant sharedAt) implements Local, Remote{

	}

	record RemoteImpl(ResourceLocation id, NativeImage image, String filename, String uploader, Instant sharedAt,
				  String url) implements Remote {
		public RemoteImpl(NativeImage image, String filename, String uploader, Instant sharedAt, String url) {
			this(ResourceLocation.fromNamespaceAndPath("axolotlclient", "gallery_remote_" + Hashing.sha256().hashUnencodedChars(url.toLowerCase(Locale.ROOT).replaceAll("[./]", "_"))),
				image, filename, uploader, sharedAt, url
			);
			register(id(), image());
		}
	}

}
