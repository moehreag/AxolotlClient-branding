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

import com.mojang.math.Axis;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PlayerHud extends BoxHudEntry {

	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "playerhud");
	@Getter
	private static boolean currentlyRendering;
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
		if (client.player != null && client.player.isVisuallySwimming()) {
			float rawPitch = client.player.isInWater() ? -90.0F - client.player.getXRot() : -90.0F;
			float pitch = Mth.lerp(client.player.getSwimAmount(1), 0.0F, rawPitch);
			float height = client.player.getBbHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height);
			yOffset = Math.abs(offset) + 35;
		} else if (client.player != null && client.player.isFallFlying()) {
			// Elytra!

			float j = (float) client.player.getFallFlyingTicks() + 1;
			float k = Mth.clamp(j * j / 100.0F, 0.0F, 1.0F);

			float pitch = k * (-90.0F - client.player.getXRot()) + 90;
			float height = client.player.getBbHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height) * 50;
			yOffset = 35 - offset;
			if (pitch < 0) {
				yOffset -= (float) (((1 / (1 + Math.exp(-pitch / 4))) - .5) * 20);
			}
		} else {
			yOffset *= .8f;
		}
	}

	@Override
	public ResourceLocation getId() {
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
	public void renderComponent(GuiGraphics graphics, float delta) {
		renderPlayer(graphics, false, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(), delta);
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		renderPlayer(graphics, true, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(),
			0
		); // If delta was delta, it would start jittering
	}

	public void renderPlayer(GuiGraphics graphics, boolean placeholder, double x, double y, float delta) {
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

		float scale = getScale() * 40;

		Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);

		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.getYRot(delta);
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}
		Quaternionf quaternionf2 = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), deltaYaw - 180 + rotation.get().floatValue());
		quaternion.mul(quaternionf2);

		// Save these to set them back later
		float pastYaw = client.player.getYRot();
		float pastPrevYaw = client.player.yRotO;
		currentlyRendering = true;
		InventoryScreen.renderEntityInInventory(graphics, (float) x, (float) y - lerpY, scale, new Vector3f(), quaternion, quaternionf2, client.player);
		currentlyRendering = false;

		client.player.setYRot(pastYaw);
		client.player.yRotO = pastPrevYaw;
	}

	private void renderPlayer(
		GuiGraphics guiGraphics, double x, double y, float delta
	) {
		var entity = client.player;

		float deltaYaw = entity.getYRot(delta);
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf2 = new Quaternionf().rotateX(20.0F * (float) (Math.PI / 180.0));
		//quaternionf2.rotateY(deltaYaw/40);
		quaternionf.mul(quaternionf2);
		float j = entity.yBodyRot;
		float k = entity.getYRot();
		float l = entity.getXRot();
		float m = entity.yHeadRotO;
		float n = entity.yHeadRot;
		float lerpY = (lastYOffset + ((yOffset - lastYOffset) * delta));
		deltaYaw /= 40;
		entity.yBodyRot = 180.0F + deltaYaw * 20.0F;
		entity.setYRot(180.0F + deltaYaw * 40.0F);
		entity.setXRot(20.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		float o = entity.getScale();
		Vector3f vector3f = new Vector3f(0.0F, yOffset * o, 0.0F);
		float scale = getScale() * 40;
		float p = scale / o;
		InventoryScreen.renderEntityInInventory(guiGraphics, (float) x, (float) y - lerpY, p, vector3f, quaternionf, quaternionf2, entity);
		entity.yBodyRot = j;
		entity.setYRot(k);
		entity.setXRot(l);
		entity.yHeadRotO = m;
		entity.yHeadRot = n;
	}

	private boolean isPerformingAction() {
		// inspired by tr7zw's mod
		LocalPlayer player = client.player;
		return player.isCrouching() || player.isSprinting() || player.isFallFlying() || player.getAbilities().flying ||
			player.isUnderWater() || player.isVisuallySwimming() || player.isPassenger() || player.isUsingItem() ||
			player.isHandsBusy() || player.hurtTime > 0 || player.isOnFire();
	}
}
