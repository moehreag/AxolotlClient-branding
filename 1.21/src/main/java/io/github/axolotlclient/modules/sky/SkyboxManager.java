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

package io.github.axolotlclient.modules.sky;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * This implementation of custom skies is based on the FabricSkyBoxes mod by AMereBagatelle
 * <a href="https://github.com/AMereBagatelle/FabricSkyBoxes">Github Link.</a>
 *
 * @license MIT
 **/

public class SkyboxManager {

	public static final double MINIMUM_ALPHA = 0.01;
	private static final SkyboxManager INSTANCE = new SkyboxManager();
	private final ArrayList<SkyboxInstance> skyboxes = new ArrayList<>();
	private final ArrayList<SkyboxInstance> active_skies = new ArrayList<>();
	private final Predicate<? super SkyboxInstance> renderPredicate = (skybox) -> !this.active_skies.contains(skybox)
		&& skybox.getAlpha() >= MINIMUM_ALPHA;

	public static SkyboxManager getInstance() {
		return INSTANCE;
	}

	public void addSkybox(SkyboxInstance skybox) {
		skyboxes.add(Objects.requireNonNull(skybox));
	}

	public void renderSkyboxes(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Runnable runnable) {
		this.skyboxes.stream().filter(this.renderPredicate).forEach(this.active_skies::add);
		this.active_skies.sort((skybox1, skybox2) -> skybox1.alpha >= skybox2.alpha ? 0 : 1);
		this.active_skies.forEach(skyboxInstance -> {
			skyboxInstance.render(matrices, projectionMatrix, tickDelta, runnable);
		});
		this.active_skies.removeIf((skybox) -> skybox.getAlpha() <= MINIMUM_ALPHA);
	}

	public void clearSkyboxes() {
		skyboxes.clear();
		active_skies.clear();
	}

	public void removeSkybox(SkyboxInstance skybox) {
		this.skyboxes.remove(skybox);
		if (this.active_skies.contains(skybox))
			active_skies.remove(skybox);
	}

	public boolean hasSkyBoxes() {
		this.skyboxes.stream().filter(this.renderPredicate).forEach(this.active_skies::add);
		if (active_skies.isEmpty())
			return false;
		this.active_skies.removeIf((skybox) -> skybox.getAlpha() <= MINIMUM_ALPHA);
		return !active_skies.isEmpty();
	}
}
