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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.util.Util;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ToastManager {
	private static final int SLOT_COUNT = 5;
	private static final int ALL_SLOTS_OCCUPIED = -1;
	@Getter
	final Minecraft minecraft;
	private final List<ToastInstance<?>> visibleToasts = new ArrayList<>();
	private final BitSet occupiedSlots = new BitSet(SLOT_COUNT);
	private final Deque<Toast> queued = Queues.newArrayDeque();

	public ToastManager(Minecraft minecraft) {
		this.minecraft = minecraft;
		MinecraftClientEvents.TICK_END.register(mc -> update());
	}

	public void update() {
		this.visibleToasts.removeIf(toastInstance -> {
			toastInstance.update();
			if (toastInstance.hasFinishedRendering()) {
				this.occupiedSlots.clear(toastInstance.firstSlotIndex, toastInstance.firstSlotIndex + toastInstance.occupiedSlotCount);
				return true;
			} else {
				return false;
			}
		});
		if (!this.queued.isEmpty() && this.freeSlotCount() > 0) {
			this.queued.removeIf(toast -> {
				int i = toast.occupiedSlotCount();
				int j = this.findFreeSlotsIndex(i);
				if (j == ALL_SLOTS_OCCUPIED) {
					return false;
				} else {
					this.visibleToasts.add(new ToastInstance<>(toast, j, i));
					this.occupiedSlots.set(j, j + i);
					return true;
				}
			});
		}
	}

	public void render() {
		if (!this.minecraft.options.hideGui) {
			int i = (int) Util.getWindow().getScaledWidth();

			for (ToastInstance<?> toastInstance : this.visibleToasts) {
				toastInstance.render(i);
			}
		}
	}

	private int findFreeSlotsIndex(int i) {
		if (this.freeSlotCount() >= i) {
			int j = 0;

			for (int k = 0; k < SLOT_COUNT; k++) {
				if (this.occupiedSlots.get(k)) {
					j = 0;
				} else if (++j == i) {
					return k + 1 - j;
				}
			}
		}

		return -1;
	}

	private int freeSlotCount() {
		return SLOT_COUNT - this.occupiedSlots.cardinality();
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Toast> T getToast(Class<? extends T> class_, Object object) {
		for (ToastInstance<?> toastInstance : this.visibleToasts) {
			if (toastInstance != null && class_.isAssignableFrom(toastInstance.getToast().getClass()) && toastInstance.getToast().getToken().equals(object)) {
				return (T) toastInstance.getToast();
			}
		}

		for (Toast toast : this.queued) {
			if (class_.isAssignableFrom(toast.getClass()) && toast.getToken().equals(object)) {
				return (T) toast;
			}
		}

		return null;
	}

	public void clear() {
		this.occupiedSlots.clear();
		this.visibleToasts.clear();
		this.queued.clear();
	}

	public void addToast(Toast toast) {
		this.queued.add(toast);
	}

	public double getNotificationDisplayTimeMultiplier() {
		return 1.0;//this.minecraft.options.notificationDisplayTime().get();
	}

	@Environment(EnvType.CLIENT)
	class ToastInstance<T extends Toast> {
		private static final long SLIDE_ANIMATION_DURATION_MS = 600L;
		@Getter
		private final T toast;
		final int firstSlotIndex;
		final int occupiedSlotCount;
		private long animationStartTime = -1L;
		private long becameFullyVisibleAt = -1L;
		private Toast.Visibility visibility = Toast.Visibility.SHOW;
		private long fullyVisibleFor;
		private float visiblePortion;
		private boolean hasFinishedRendering;

		ToastInstance(final T toast, final int i, final int j) {
			this.toast = toast;
			this.firstSlotIndex = i;
			this.occupiedSlotCount = j;
		}

		public boolean hasFinishedRendering() {
			return this.hasFinishedRendering;
		}

		private void calculateVisiblePortion(long l) {
			float f = MathHelper.clamp((float) (l - this.animationStartTime) / SLIDE_ANIMATION_DURATION_MS, 0.0F, 1.0F);
			f *= f;
			if (this.visibility == Toast.Visibility.HIDE) {
				this.visiblePortion = 1.0F - f;
			} else {
				this.visiblePortion = f;
			}
		}

		public void update() {
			long l = Minecraft.getTime();
			if (this.animationStartTime == -1L) {
				this.animationStartTime = l;
				this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
			}

			if (this.visibility == Toast.Visibility.SHOW && l - this.animationStartTime <= SLIDE_ANIMATION_DURATION_MS) {
				this.becameFullyVisibleAt = l;
			}

			this.fullyVisibleFor = l - this.becameFullyVisibleAt;
			this.calculateVisiblePortion(l);
			this.toast.update(ToastManager.this, this.fullyVisibleFor);
			Toast.Visibility visibility = this.toast.getWantedVisibility();
			if (visibility != this.visibility) {
				this.animationStartTime = l - (long) ((int) ((1.0F - this.visiblePortion) * SLIDE_ANIMATION_DURATION_MS));
				this.visibility = visibility;
				this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
			}

			this.hasFinishedRendering = this.visibility == Toast.Visibility.HIDE && l - this.animationStartTime > SLIDE_ANIMATION_DURATION_MS;
		}

		public void render(int i) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float) i - (float) this.toast.width() * this.visiblePortion, (float) (this.firstSlotIndex * Toast.SLOT_HEIGHT), 1000.0F);
			this.toast.render(ToastManager.this.minecraft.textRenderer, this.fullyVisibleFor);
			GlStateManager.popMatrix();
		}
	}
}
