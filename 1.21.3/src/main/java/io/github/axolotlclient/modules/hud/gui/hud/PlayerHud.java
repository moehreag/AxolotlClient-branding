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

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.DoubleOption;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
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
	@Getter private static boolean currentlyRendering;
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

		//InventoryScreen.renderEntityInInventory(graphics, getX(), getY(), -getHeight(), new Vector3f(), new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), deltaYaw - 180 + rotation.get().floatValue()), null, client.player);

		Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.pushMatrix();
		//PoseStack matrixStack = graphics.pose();
		matrixStack.translate((float) x, (float) (y - lerpY), 1050);
		matrixStack.scale(1, 1, -1);

		// TODO check
		//RenderSystem.applyModelViewMatrix();
		PoseStack nextStack = new PoseStack();
		nextStack.translate(0, 0, 1000);
		float scale = getScale() * 40;
		nextStack.scale(scale, scale, scale);

		Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);

		nextStack.mulPose(quaternion.get(new Matrix4f()));
		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.getYRot(delta);
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}
		nextStack.mulPose(
			new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), deltaYaw - 180 + rotation.get().floatValue())
				.get(new Matrix4f()));

		// Save these to set them back later
		float pastYaw = client.player.getYRot();
		float pastPrevYaw = client.player.yRotO;

		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher renderer = client.getEntityRenderDispatcher();
		renderer.overrideCameraOrientation(quaternion);
		renderer.setRenderShadow(false);

		currentlyRendering = true;
		graphics.drawSpecial(v -> renderer.render(client.player, 0, 0, 0, delta, nextStack, v, 15728880));
		currentlyRendering = false;
		renderer.setRenderShadow(true);
		//matrixStack.translate((float) -x, (float) -(y - lerpY), -1050);
		matrixStack.popMatrix();

		client.player.setYRot(pastYaw);
		client.player.yRotO = pastPrevYaw;

		Lighting.setupFor3DItems();
	}

	private boolean isPerformingAction() {
		// inspired by tr7zw's mod
		LocalPlayer player = client.player;
		return player.isCrouching() || player.isSprinting() || player.isFallFlying() || player.getAbilities().flying ||
			   player.isUnderWater() || player.isVisuallySwimming() || player.isPassenger() || player.isUsingItem() ||
			   player.isHandsBusy() || player.hurtTime > 0 || player.isOnFire();
	}
}
