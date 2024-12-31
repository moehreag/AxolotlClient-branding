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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import lombok.AllArgsConstructor;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.class_5512;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.options.AttackIndicator;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CrosshairHud extends AbstractHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "crosshairhud");

	private final EnumOption<Crosshair> type = new EnumOption<>("crosshair_type", Crosshair.class, Crosshair.CROSS);
	private final BooleanOption showInF5 = new BooleanOption("showInF5", false);
	private final ColorOption defaultColor = new ColorOption("defaultcolor", ClientColors.WHITE);
	private final ColorOption entityColor = new ColorOption("entitycolor", ClientColors.SELECTOR_RED);
	private final ColorOption containerColor = new ColorOption("blockcolor", ClientColors.SELECTOR_BLUE);
	private final ColorOption attackIndicatorBackgroundColor = new ColorOption("attackindicatorbg",
		new Color(0xFF141414));
	private final ColorOption attackIndicatorForegroundColor = new ColorOption("attackindicatorfg", ClientColors.WHITE);
	private final BooleanOption applyBlend = new BooleanOption("applyBlend", true);
	private final BooleanOption overrideF3 = new BooleanOption("overrideF3", false);

	private final GraphicsOption customTextureGraphics = new GraphicsOption("customTextureGraphics",
		new int[][]{
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		});

	public CrosshairHud() {
		super(15, 15);
	}

	@Override
	public boolean movable() {
		return false;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(type);
		options.add(customTextureGraphics);
		options.add(showInF5);
		options.add(overrideF3);
		options.add(applyBlend);
		options.add(defaultColor);
		options.add(entityColor);
		options.add(containerColor);
		options.add(attackIndicatorBackgroundColor);
		options.add(attackIndicatorForegroundColor);
		return options;
	}

	@Override
	public boolean overridesF3() {
		return overrideF3.get();
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.5;
	}

	@Override
	public void render(MatrixStack matrices, float delta) {
		if (!client.options.getPerspective().isFirstPerson() && !showInF5.get()) {
			return;
		}

		RenderSystem.color4f(1, 1, 1, 1);

		matrices.push();
		scale(matrices);

		int x = getPos().x;
		int y = getPos().y + 1;
		Color color = getColor();
		AttackIndicator indicator = this.client.options.attackIndicator;

		RenderSystem.enableBlend();

		// Need to not enable blend while the debug HUD is open because it does weird stuff. Why? no idea.
		if (color == defaultColor.get() && !type.get().equals(Crosshair.DIRECTION) && applyBlend.get()
			&& !client.options.debugEnabled) {
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE,
				GlStateManager.DstFactor.ZERO);
		} else {
			RenderSystem.disableBlend();
		}

		if (type.get().equals(Crosshair.DOT)) {
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 2, y + (getHeight() / 2) - 2, 3, 3, color);
		} else if (type.get().equals(Crosshair.CROSS)) {
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 6, y + (getHeight() / 2) - 1, 6, 1, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2), y + (getHeight() / 2) - 1, 5, 1, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 1, y + (getHeight() / 2) - 6, 1, 5, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 1, y + (getHeight() / 2), 1, 5, color);
		} else if (type.get().equals(Crosshair.DIRECTION)) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(client.getWindow().getScaledWidth() / 2F, client.getWindow().getScaledHeight() / 2F, (float) this.getZOffset());
			Camera camera = this.client.gameRenderer.getCamera();
			RenderSystem.rotatef(camera.getPitch(), -1.0F, 0.0F, 0.0F);
			RenderSystem.rotatef(camera.getYaw(), 0.0F, 1.0F, 0.0F);
			RenderSystem.scalef(-getScale(), -getScale(), -getScale());
			RenderSystem.renderCrosshair(10);
			RenderSystem.popMatrix();
		} else if (type.get().equals(Crosshair.TEXTURE) || type.get().equals(Crosshair.CUSTOM)) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			if (type.get().equals(Crosshair.TEXTURE)) {
				MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
				// Draw crosshair
				RenderSystem.color4f((float) color.getRed() / 255, (float) color.getGreen() / 255,
					(float) color.getBlue() / 255, (float) color.getAlpha() / 255);
				drawTexture(matrices,
					(int) (((client.getWindow().getScaledWidth() / getScale()) - 15) / 2),
					(int) (((client.getWindow().getScaledHeight() / getScale()) - 15) / 2), 0, 0, 15, 15);
			} else {
				Util.bindTexture(customTextureGraphics);
				// Draw crosshair
				RenderSystem.color4f((float) color.getRed() / 255, (float) color.getGreen() / 255,
					(float) color.getBlue() / 255, (float) color.getAlpha() / 255);

				drawTexture(matrices,
					(int) (((client.getWindow().getScaledWidth() / getScale()) - 15) / 2),
					(int) (((client.getWindow().getScaledHeight() / getScale()) - 15) / 2), 0, 0, 15, 15, 15, 15);

				MinecraftClient.getInstance().getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
			}

			RenderSystem.color4f(1, 1, 1, 1);

			// Draw attack indicator
			x = (int) ((client.getWindow().getScaledWidth() / getScale()) / 2 - 8);
			y = (int) ((client.getWindow().getScaledHeight() / getScale()) / 2 - 7 + 16);
			ItemStack itemStack = this.client.player.getStackInHand(Hand.OFF_HAND);
			boolean bl = this.client.options.field_26808 == class_5512.field_26811;
			if (bl && itemStack.getItem() == Items.SHIELD && this.client.player.method_31233(itemStack)) {
				this.drawTexture(matrices, x, y, 52, 112, 16, 16);
			} else if (bl && this.client.player.isBlocking()) {
				this.drawTexture(matrices, x, y, 36, 112, 16, 16);
			} else if (this.client.options.attackIndicator == AttackIndicator.CROSSHAIR) {
				float f = this.client.player.getAttackCooldownProgress(0.0F);
				boolean bl2 = false;
				if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity && f >= 2.0F) {
					bl2 = ((EntityHitResult) this.client.crosshairTarget).method_31252() <= this.client.player.method_31239(0.0F);
					bl2 &= this.client.targetedEntity.isAlive();
				}

				if (bl2) {
					this.drawTexture(matrices, x, y, 68, 94, 16, 16);
				} else if (f > 1.3F && f < 2.0F) {
					float h = (f - 1.0F);
					int l = (int) (h * 17.0F);
					this.drawTexture(matrices, x, y, 36, 94, 16, 4);
					this.drawTexture(matrices, x, y, 52, 94, l, 4);
				}
			}
		}
		if (indicator == AttackIndicator.CROSSHAIR && !type.get().equals(Crosshair.TEXTURE) && !type.get().equals(Crosshair.CUSTOM)) {
			float progress = this.client.player.getAttackCooldownProgress(0.0F) / 2;
			if (progress != 1.0F) {
				RenderUtil.drawRectangle(matrices, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					11, 1, attackIndicatorBackgroundColor.get());
				RenderUtil.drawRectangle(matrices, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					(int) (progress * 11), 1, attackIndicatorForegroundColor.get());
			}
		}
		RenderSystem.disableBlend();
		matrices.pop();
	}

	public Color getColor() {
		HitResult hit = client.crosshairTarget;
		if (hit == null || hit.getType() == null) {
			return defaultColor.get();
		} else if (hit.getType() == HitResult.Type.ENTITY && ((EntityHitResult) this.client.crosshairTarget).method_31252() <= this.client.player.method_31239(0.0F)) {
			return entityColor.get();
		} else if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
			World world = this.client.world;
			if (world.getBlockState(blockPos).createScreenHandlerFactory(world, blockPos) != null
				|| world.getBlockState(blockPos).getBlock() instanceof AbstractChestBlock<?>) {
				return containerColor.get();
			}
		}
		return defaultColor.get();
	}

	@Override
	public void renderPlaceholder(MatrixStack matrices, float delta) {
		// Shouldn't need this...
	}

	@Override
	public AnchorPoint getAnchor() {
		return AnchorPoint.MIDDLE_MIDDLE;
	}

	@AllArgsConstructor
	public enum Crosshair {
		CROSS, DOT, DIRECTION, TEXTURE, CUSTOM
	}
}
