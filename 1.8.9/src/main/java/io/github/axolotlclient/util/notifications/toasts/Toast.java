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

package io.github.axolotlclient.util.notifications.toasts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.sound.instance.SimpleSoundInstance;
import net.minecraft.client.sound.system.SoundManager;
import net.minecraft.resource.Identifier;

@Environment(EnvType.CLIENT)
public interface Toast {
	Object NO_TOKEN = new Object();
	int DEFAULT_WIDTH = 160;
	int SLOT_HEIGHT = 32;

	Visibility getWantedVisibility();

	void update(ToastManager toastManager, long l);

	void render(TextRenderer font, long l);

	default Object getToken() {
		return NO_TOKEN;
	}

	default int width() {
		return DEFAULT_WIDTH;
	}

	default int height() {
		return SLOT_HEIGHT;
	}

	default int occupiedSlotCount() {
		return -Math.floorDiv(-this.height(), SLOT_HEIGHT);
	}

	@Environment(EnvType.CLIENT)
	enum Visibility {
		SHOW(new Identifier("axolotlclient", "gui.toast.in")),
		HIDE(new Identifier("axolotlclient", "gui.toast.out"));

		private final Identifier soundEvent;

		Visibility(final Identifier soundEvent) {
			this.soundEvent = soundEvent;
		}

		public void playSound(SoundManager handler) {
			handler.play(SimpleSoundInstance.of(this.soundEvent));
		}
	}
}
