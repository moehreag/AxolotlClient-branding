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
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.util.ClientColors;
import io.github.axolotlclient.util.Util;
import lombok.AllArgsConstructor;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4fStack;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CrosshairHud extends AbstractHudEntry implements DynamicallyPositionable {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kronhud", "crosshairhud");
	private static final ResourceLocation CROSSHAIR_TEXTURE = ResourceLocation.withDefaultNamespace("hud/crosshair");
	private static final ResourceLocation ATTACK_INDICATOR_FULL = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
	private static final ResourceLocation ATTACK_INDICATOR_BACKGROUND = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background");
	private static final ResourceLocation ATTACK_INDICATOR_PROGRESS = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
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
	public ResourceLocation getId() {
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

	private static final RenderType CROSSHAIR_NO_TEX = RenderType.create(
		"crosshair_no_tex",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		786432,
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
			.setTransparencyState(RenderStateShard.TransparencyStateShard.CROSSHAIR_TRANSPARENCY)
			.createCompositeState(false)
	);

	@Override
	public void render(GuiGraphics graphics, float delta) {
	}

	public void renderCrosshair(GuiGraphics graphics, float delta) {
		if (!client.options.getCameraType().isFirstPerson() && !showInF5.get()) {
			return;
		}

		graphics.pose().pushPose();
		scale(graphics);

		int x = getPos().x();
		int y = getPos().y() + 1;
		Color color = getColor();
		AttackIndicatorStatus indicator = this.client.options.attackIndicator().get();

		// Need to not enable blend while the debug HUD is open because it does weird stuff. Why? no idea.
		boolean blend = color == defaultColor.get() && !type.get().equals(Crosshair.DIRECTION) && applyBlend.get()
			&& !client.gui.getDebugOverlay().showDebugScreen();

		if (type.get().equals(Crosshair.DOT)) {
			fillRenderType(graphics, blend, x + (getWidth() / 2) - 2, y + (getHeight() / 2) - 2, 3, 3, color);
		} else if (type.get().equals(Crosshair.CROSS)) {
			fillRenderType(graphics, blend, x + (getWidth() / 2) - 6, y + (getHeight() / 2) - 1, 6, 1, color);
			fillRenderType(graphics, blend, x + (getWidth() / 2), y + (getHeight() / 2) - 1, 5, 1, color);
			fillRenderType(graphics, blend, x + (getWidth() / 2) - 1, y + (getHeight() / 2) - 6, 1, 5, color);
			fillRenderType(graphics, blend, x + (getWidth() / 2) - 1, y + (getHeight() / 2), 1, 5, color);
		} else if (type.get().equals(Crosshair.DIRECTION)) {
			Camera camera = this.client.gameRenderer.getMainCamera();
			Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.pushMatrix();
			matrixStack.translate(client.getWindow().getGuiScaledWidth() / 2F, client.getWindow().getGuiScaledHeight() / 2F,
				0);
			matrixStack.rotateX(-camera.getXRot() * 0.017453292F);
			matrixStack.rotateY(camera.getYRot() * 0.017453292F);
			matrixStack.scale(-getScale(), -getScale(), -getScale());
			RenderSystem.renderCrosshair(10);
			matrixStack.popMatrix();
		} else if (type.get().equals(Crosshair.TEXTURE) || type.get().equals(Crosshair.CUSTOM)) {
			if (type.get().equals(Crosshair.TEXTURE)) {
				// Draw crosshair
				graphics.blitSprite(renderType(blend), CROSSHAIR_TEXTURE,
					(int) (((graphics.guiWidth() / getScale()) - 15) / 2),
					(int) (((graphics.guiHeight() / getScale()) - 15) / 2), 15, 15, color.toInt());
			} else {
				// Draw crosshair
				graphics.blit(renderType(blend), Util.getTexture(customTextureGraphics), (int) (((graphics.guiWidth() / getScale()) - 15) / 2),
					(int) (((graphics.guiHeight() / getScale()) - 15) / 2), 0, 0, 15, 15, 15, 15, color.toInt());
			}

			// Draw attack indicator
			if (indicator == AttackIndicatorStatus.CROSSHAIR) {
				float progress = this.client.player.getAttackStrengthScale(0.0F);

				// Whether a cross should be displayed under the indicator
				boolean targetingEntity = false;
				if (this.client.crosshairPickEntity != null && this.client.crosshairPickEntity instanceof LivingEntity
					&& progress >= 1.0F) {
					targetingEntity = this.client.player.getCurrentItemAttackStrengthDelay() > 5.0F;
					targetingEntity &= this.client.crosshairPickEntity.isAlive();
				}

				x = (int) ((graphics.guiWidth() / getScale()) / 2 - 8);
				y = (int) ((graphics.guiHeight() / getScale()) / 2 - 7 + 16);

				if (targetingEntity) {
					graphics.blitSprite(renderType(blend), ATTACK_INDICATOR_FULL, x, y, 16, 16);
				} else if (progress < 1.0F) {
					int k = (int) (progress * 17.0F);
					graphics.blitSprite(renderType(blend), ATTACK_INDICATOR_BACKGROUND, x, y, 16, 4);
					graphics.blitSprite(renderType(blend), ATTACK_INDICATOR_PROGRESS, 16, 4, 0, 0, x, y, k, 4);
				}
			}
		}
		if (indicator == AttackIndicatorStatus.CROSSHAIR && !type.get().equals(Crosshair.TEXTURE)) {
			float progress = this.client.player.getAttackStrengthScale(0.0F);
			if (progress != 1.0F) {
				fillRenderType(graphics, blend, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					11, 1, attackIndicatorBackgroundColor.get());
				fillRenderType(graphics, blend, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					(int) (progress * 11), 1, attackIndicatorForegroundColor.get());
			}
		}
		graphics.pose().popPose();
	}

	private Function<ResourceLocation, RenderType> renderType(boolean blend) {
		return blend ? RenderType::crosshair : RenderType::guiTextured;
	}

	private void fillRenderType(GuiGraphics graphics, boolean blend, int x, int y, int width, int height, Color color) {
		if (blend) {
			graphics.fill(CROSSHAIR_NO_TEX, x, y, width + x, height + y, color.toInt());
		} else {
			graphics.fill(x, y, width + x, height + y, color.toInt());
		}
	}

	public Color getColor() {
		HitResult hit = client.hitResult;
		if (hit == null || hit.getType() == HitResult.Type.MISS) {
			return defaultColor.get();
		} else if (hit.getType() == HitResult.Type.ENTITY) {
			return entityColor.get();
		} else if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
			Level world = this.client.level;
			if (world.getBlockState(blockPos).getMenuProvider(world, blockPos) != null
				|| world.getBlockState(blockPos).getBlock() instanceof AbstractChestBlock<?>) {
				return containerColor.get();
			}
		}
		return defaultColor.get();
	}

	@Override
	public void renderPlaceholder(GuiGraphics graphics, float delta) {
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
