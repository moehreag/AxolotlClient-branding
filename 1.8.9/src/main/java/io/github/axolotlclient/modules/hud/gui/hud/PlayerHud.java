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

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PlayerHud extends BoxHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "playerhud");
	@Getter
	private static boolean currentlyRendering = false;
	private final DoubleOption rotation = new DoubleOption("rotation", 0d, 0d, 360d);
	private final BooleanOption dynamicRotation = new BooleanOption("dynamicrotation", true);
	private final BooleanOption autoHide = new BooleanOption("autoHide", false);
	private float lastYawOffset = 0;
	private float yawOffset = 0;
	private float lastYOffset = 0;
	private float yOffset = 0;
	private long hide;

	public PlayerHud() {
		super(62, 94, true);
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void onPlayerDirectionChange(PlayerDirectionChangeEvent event) {
		yawOffset += (event.getYaw() - event.getPrevYaw()) / 2;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		lastYawOffset = yawOffset;
		yawOffset *= .93f;
		lastYOffset = yOffset;
		yOffset *= .8;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(dynamicRotation);
		options.add(rotation);
		options.add(autoHide);
		return options;
	}

	@Override
	public void renderComponent(float delta) {
		renderPlayer(false, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(), delta);
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		renderPlayer(true, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(), 0); // If delta was delta, it would start jittering
	}

	@Override
	public boolean movable() {
		return true;
	}

	public void renderPlayer(boolean placeholder, double x, double y, float delta) {
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();

		if (client.player == null) {
			return;
		}

		if (!placeholder && autoHide.get()) {
			if (isPerformingAction()) {
				hide = -1;
			} else if (hide == -1) {
				hide = System.currentTimeMillis();
			}

			if (hide != -1 && System.currentTimeMillis() - hide > 500) {
				return;
			}
		}

		float lerpY = (lastYOffset + ((yOffset - lastYOffset) * delta));

		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y - lerpY, 1050);
		GlStateManager.scalef(1, 1, -1);

		GlStateManager.translatef(0, 0, 1000);
		float scale = getScale() * 40;
		GlStateManager.scalef(scale, scale, scale);

		GlStateManager.rotatef(180, 0, 0, 1);

		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.yaw;
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}

		// Save these to set them back later
		float pastYaw = client.player.yaw;
		float pastBodyYaw = client.player.bodyYaw;
		float pastHeadYaw = client.player.headYaw;
		float pastPrevHeadYaw = client.player.prevHeadYaw;
		float pastPrevYaw = client.player.prevYaw;

		client.player.headYaw = client.player.yaw;
		client.player.prevHeadYaw = client.player.yaw;

		GlStateManager.rotatef(deltaYaw - 180 + rotation.get().floatValue(), 0, 1, 0);
		Lighting.turnOn();
		EntityRenderDispatcher renderer = client.getEntityRenderDispatcher();
		renderer.setCameraYaw(180);
		renderer.cameraPitch = 0;
		renderer.setRenderShadow(false);

		//VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

		currentlyRendering = true;
		renderer.render(client.player, 0.0, 0.0, 0.0, 0, delta);
		currentlyRendering = false;
		//renderer.render(client.player, 0, 0, 0, delta, 15728880);

		renderer.setRenderShadow(true);
		GlStateManager.popMatrix();

		client.player.yaw = pastYaw;
		client.player.headYaw = pastHeadYaw;
		client.player.prevHeadYaw = pastPrevHeadYaw;
		client.player.prevYaw = pastPrevYaw;
		client.player.bodyYaw = pastBodyYaw;

		Lighting.turnOff();
		GlStateManager.disableRescaleNormal();
		GlStateManager.activeTexture(GLX.GL_TEXTURE1);
		GlStateManager.disableTexture();
		GlStateManager.activeTexture(GLX.GL_TEXTURE0);
		//DiffuseLighting.setup3DGuiLighting();
	}

	private boolean isPerformingAction() {
		// inspired by tr7zw's mod
		LocalClientPlayerEntity player = client.player;
		return player.isSneaking() || player.isSprinting() || player.abilities.flying
			|| client.player.isSubmergedIn(Material.WATER) || player.hasVehicle() || player.isUsingItem()
			|| player.handSwinging || player.hurtTime > 0 || player.isOnFire();
	}
}
