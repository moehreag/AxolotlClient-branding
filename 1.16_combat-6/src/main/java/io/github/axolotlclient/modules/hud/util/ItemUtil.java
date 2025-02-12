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

package io.github.axolotlclient.modules.hud.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.util.ClientColors;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@UtilityClass
public class ItemUtil {

	public static ArrayList<ItemUtil.TimedItemStorage> removeOld(List<ItemUtil.TimedItemStorage> list, int time) {
		ArrayList<ItemUtil.TimedItemStorage> stored = new ArrayList<>();
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.getPassedTime() <= time) {
				stored.add(storage);
			}
		}
		return stored;
	}

	public static List<ItemUtil.TimedItemStorage> untimedToTimed(List<ItemStorage> list) {
		ArrayList<TimedItemStorage> timed = new ArrayList<>();
		for (ItemStorage stack : list) {
			timed.add(stack.timed());
		}
		return timed;
	}

	public static Optional<ItemUtil.TimedItemStorage> getTimedItemFromItem(ItemStack item,
																		   List<ItemUtil.TimedItemStorage> list) {
		ItemStack compare = item.copy();
		compare.setCount(1);
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.stack.isItemEqualIgnoreDamage(compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	public static int getTotal(MinecraftClient client, ItemStack stack) {
		List<ItemStack> item = ItemUtil.getItems(client);
		if (item == null || item.isEmpty()) {
			return 0;
		}
		List<ItemUtil.ItemStorage> items = ItemUtil.storageFromItem(item);
		Optional<ItemUtil.ItemStorage> stor = ItemUtil.getItemFromItem(stack, items);
		return stor.map(itemStorage -> itemStorage.times).orElse(0);
	}

	public static List<ItemStack> getItems(MinecraftClient client) {
		ArrayList<ItemStack> items = new ArrayList<>();
		if (client.player == null) {
			return null;
		}
		items.addAll(client.player.inventory.armor);
		items.addAll(client.player.inventory.offHand);
		items.addAll(client.player.inventory.main);
		return items;
	}

	public static List<ItemStorage> storageFromItem(List<ItemStack> items) {
		ArrayList<ItemStorage> storage = new ArrayList<>();
		for (ItemStack item : items) {
			if (item.isEmpty()) {
				continue;
			}
			Optional<ItemStorage> s = getItemFromItem(item, storage);
			if (s.isPresent()) {
				ItemUtil.ItemStorage store = s.get();
				store.incrementTimes(item.getCount());
			} else {
				storage.add(new ItemUtil.ItemStorage(item, item.getCount()));
			}
		}
		return storage;
	}

	public static Optional<ItemUtil.ItemStorage> getItemFromItem(ItemStack item, List<ItemUtil.ItemStorage> list) {
		ItemStack compare = item.copy();
		compare.setCount(1);
		for (ItemUtil.ItemStorage storage : list) {
			if (storage.stack.isItemEqualIgnoreDamage(compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	/**
	 * Compares two ItemStorage Lists.
	 * If list1.get(1) is 10, and list2 is 5, it will return 5.
	 * Will return nothing if negative...
	 *
	 * @param list1 one to be based off of
	 * @param list2 one to compare to
	 * @return the compared list
	 */
	public static List<ItemStorage> compare(List<ItemStorage> list1, List<ItemStorage> list2) {
		ArrayList<ItemStorage> list = new ArrayList<>();
		for (ItemStorage current : list1) {
			Optional<ItemStorage> optional = getItemFromItem(current.stack, list2);
			if (optional.isPresent()) {
				ItemStorage other = optional.get();
				if (current.times - other.times <= 0) {
					continue;
				}
				list.add(new ItemStorage(other.stack.copy(), current.times - other.times));
			} else {
				list.add(current.copy());
			}
		}
		return list;
	}

	public void renderGuiItemModel(float scale, ItemStack stack, float x, float y) {
		DiffuseLighting.disable();
		MinecraftClient client = MinecraftClient.getInstance();
		BakedModel model = client.getItemRenderer().getHeldItemModel(stack, null, null);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(scale, scale, 1);
		client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.translatef(x, y, 100.0F + client.getItemRenderer().zOffset);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		MatrixStack matrixStack = new MatrixStack();
		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		boolean bl = !model.isSideLit();
		if (bl) {
			DiffuseLighting.disableGuiDepthLighting();
		}

		MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
		immediate.draw();
		RenderSystem.enableDepthTest();
		if (bl) {
			DiffuseLighting.enableGuiDepthLighting();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
		DiffuseLighting.disable();
	}

	public static void renderGuiItemOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y,
											String countLabel, int textColor, boolean shadow) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (stack.isEmpty()) {
			return;
		}

		if (stack.getCount() != 1 || countLabel != null) {
			String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
			matrices.translate(0.0, 0.0, client.getItemRenderer().zOffset + 200.0F);
			DrawUtil.drawString(matrices, string, (x + 19 - 2 - renderer.getWidth(string)), (y + 6 + 3), textColor,
				shadow);
		}

		if (stack.isDamaged()) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.disableBlend();
			float f = (float) stack.getDamage();
			float g = (float) stack.getMaxDamage();
			float h = Math.max(0.0F, (g - f) / g);
			int i = Math.round(13.0F - f * 13.0F / g);
			int j = MathHelper.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
			DrawUtil.fillRect(matrices, x + 2, y + 13, 13, 2, ClientColors.BLACK.toInt());
			DrawUtil.fillRect(matrices, x + 2, y + 13, i, 1,
				(((255 << 8) + (j >> 16 & 255) << 8) + (j >> 8 & 255) << 8) + (j & 255));
			RenderSystem.enableBlend();
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}

		ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
		float f = clientPlayerEntity == null ? 0.0F
			: clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(),
			MinecraftClient.getInstance().getTickDelta());
		if (f > 0.0F) {
			RenderSystem.disableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			DrawUtil.fillRect(matrices, x, y + MathHelper.floor(16.0F * (1.0F - f)), 16, MathHelper.ceil(16.0F * f),
				ClientColors.WHITE.withAlpha(127).toInt());
			RenderSystem.enableTexture();
			RenderSystem.enableDepthTest();
		}
	}

	public static class ItemStorage {

		public final ItemStack stack;
		public int times;

		public ItemStorage(ItemStack stack, int times) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			this.stack = copy;
			this.times = times;
		}

		public void incrementTimes(int num) {
			times = times + num;
		}

		public ItemStorage copy() {
			return new ItemStorage(stack.copy(), times);
		}

		public TimedItemStorage timed() {
			return new TimedItemStorage(stack, times);
		}
	}

	public static class TimedItemStorage extends ItemStorage {

		public float start;

		public TimedItemStorage(ItemStack stack, int times) {
			super(stack, times);
			this.start = Util.getMeasuringTimeMs();
		}

		public float getPassedTime() {
			return Util.getMeasuringTimeMs() - start;
		}

		@Override
		public void incrementTimes(int num) {
			super.incrementTimes(num);
			refresh();
		}

		public void refresh() {
			start = Util.getMeasuringTimeMs();
		}
	}
}
