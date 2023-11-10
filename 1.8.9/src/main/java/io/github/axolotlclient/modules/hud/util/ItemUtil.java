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

package io.github.axolotlclient.modules.hud.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Color;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.item.BlockEntityItemRenderer;
import net.minecraft.client.render.model.block.ModelTransformations;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.resource.model.BakedModel;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@UtilityClass
public class ItemUtil {

	private static final Identifier ITEM_GLINT_TEXTURE = new Identifier("textures/misc/enchanted_item_glint.png");

	public static int getTotal(Minecraft client, ItemStack stack) {
		List<ItemStack> item = ItemUtil.getItems(client);
		if (item == null) {
			return 0;
		}
		AtomicInteger count = new AtomicInteger();
		item.forEach(itemStack -> {
			if (itemStack != null && stack != null && itemStack.getItem() == stack.getItem()) {
				count.addAndGet(itemStack.size);
			}
		});
		return count.get();
	}

	public static List<ItemStack> getItems(Minecraft client) {
		ArrayList<ItemStack> items = new ArrayList<>();
		if (client.player == null) {
			return null;
		}
		items.addAll(Arrays.asList(client.player.inventory.armorSlots));
		items.addAll(Arrays.asList(client.player.inventory.inventorySlots));
		return items;
	}

	/**
	 * Compares two ItemStorage Lists.
	 * If list1.get(1) is 10, and list2 is 5, it will return 5.
	 * Will return nothing if negative...
	 *
	 * @param list1 one to be based off of
	 * @param list2 one to compare to
	 * @return the item storage
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

	public static Optional<ItemUtil.ItemStorage> getItemFromItem(ItemStack item, List<ItemUtil.ItemStorage> list) {
		ItemStack compare = item.copy();
		compare.size = 1;
		for (ItemUtil.ItemStorage storage : list) {
			if (isEqual(storage.stack, compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	private static boolean isEqual(ItemStack stack, ItemStack compare) {
		return stack != null && compare != null && stack.getItem() == compare.getItem();
	}

	public static ArrayList<ItemUtil.TimedItemStorage> removeOld(List<ItemUtil.TimedItemStorage> list, int time) {
		ArrayList<ItemUtil.TimedItemStorage> stored = new ArrayList<>();
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.getPassedTime() <= time) {
				stored.add(storage);
			}
		}
		return stored;
	}

	public static Optional<ItemUtil.TimedItemStorage> getTimedItemFromItem(ItemStack item,
																		   List<ItemUtil.TimedItemStorage> list) {
		ItemStack compare = item.copy();
		compare.size = 1;
		for (ItemUtil.TimedItemStorage storage : list) {
			if (isEqual(storage.stack, compare)) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	public static List<ItemStorage> storageFromItem(List<ItemStack> items) {
		ArrayList<ItemStorage> storage = new ArrayList<>();
		for (ItemStack item : items) {
			if (item == null) {
				continue;
			}
			Optional<ItemStorage> s = getItemFromItem(item, storage);
			if (s.isPresent()) {
				ItemUtil.ItemStorage store = s.get();
				store.incrementTimes(item.size);
			} else {
				storage.add(new ItemUtil.ItemStorage(item, item.size));
			}
		}
		return storage;
	}

	public static List<ItemUtil.TimedItemStorage> untimedToTimed(List<ItemStorage> list) {
		ArrayList<TimedItemStorage> timed = new ArrayList<>();
		for (ItemStorage stack : list) {
			timed.add(stack.timed());
		}
		return timed;
	}

	// The scaling stuff wasn't a problem on 1.8.9 so no need to create more complicated stuff

	public static void renderGuiItemModel(ItemStack stack, int x, int y) {
		Lighting.turnOnGui();
		GlStateManager.pushMatrix();
		Minecraft.getInstance().getItemRenderer().renderGuiItemModel(stack, x, y);
		GlStateManager.popMatrix();
		Lighting.turnOff();
	}

	public static void renderColoredGuiItemModel(ItemStack stack, int x, int y, Color color) {
		Lighting.turnOnGui();
		GlStateManager.pushMatrix();

		Minecraft client = Minecraft.getInstance();

		if (stack != null && stack.getItem() != null) {
			client.getItemRenderer().zOffset += 50.0F;


			BakedModel bakedModel = client.getItemRenderer().getModelShaper().getModel(stack);
			GlStateManager.pushMatrix();
			client.getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
			client.getTextureManager().get(TextureAtlas.BLOCKS_LOCATION).pushFilter(false, false);
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);


			GlStateManager.translatef((float) x, (float) y, 100.0F + client.getItemRenderer().zOffset);
			GlStateManager.translatef(8.0F, 8.0F, 0.0F);
			GlStateManager.scalef(1.0F, 1.0F, -1.0F);
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			if (bakedModel.isGui3d()) {
				GlStateManager.scalef(40.0F, 40.0F, 40.0F);
				GlStateManager.rotatef(210.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(-135.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.enableLighting();
			} else {
				GlStateManager.scalef(64.0F, 64.0F, 64.0F);
				GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.disableLighting();
			}


			bakedModel.getTransformations().apply(ModelTransformations.Type.GUI);


			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			if (bakedModel.isCustomRenderer()) {
				GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableRescaleNormal();
				BlockEntityItemRenderer.INSTANCE.render(stack);
			} else {
				GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
				renderBakedItemModel(bakedModel, color.toInt(), stack);
				if (stack.hasEnchantmentGlint()) {
					renderGlint(bakedModel);
				}
			}

			GlStateManager.popMatrix();


			GlStateManager.disableAlphaTest();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableLighting();
			GlStateManager.popMatrix();
			client.getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
			client.getTextureManager().get(TextureAtlas.BLOCKS_LOCATION).popFilter();


			client.getItemRenderer().zOffset -= 50.0F;
		}

		GlStateManager.popMatrix();
		Lighting.turnOff();
	}

	private void renderBakedItemModel(BakedModel bakedModel, int color, ItemStack itemStack) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.BLOCK_NORMALS);

		for (Direction direction : Direction.values()) {
			renderBakedItemQuads(bufferBuilder, bakedModel.getQuads(direction), color, itemStack);
		}

		renderBakedItemQuads(bufferBuilder, bakedModel.getQuads(), color, itemStack);
		tessellator.end();
	}

	private void renderBakedItemQuads(BufferBuilder bufferBuilder, List<BakedQuad> list, int color, ItemStack itemStack) {
		boolean bl = color == -1 && itemStack != null;
		int j = 0;

		for (int k = list.size(); j < k; ++j) {
			BakedQuad bakedQuad = list.get(j);
			int l = color;
			if (bl && bakedQuad.hasTint()) {
				l = itemStack.getItem().getDisplayColor(itemStack, bakedQuad.getTintIndex());
				if (GameRenderer.anaglyphEnabled) {
					l = TextureUtil.getAnaglyphColor(l);
				}

				l |= -16777216;
			}

			renderQuad(bufferBuilder, bakedQuad, l);
		}
	}

	private void renderGlint(BakedModel bakedModel) {
		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(768, 1);
		Minecraft.getInstance().getTextureManager().bind(ITEM_GLINT_TEXTURE);
		GlStateManager.matrixMode(5890);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translatef(f, 0.0F, 0.0F);
		GlStateManager.rotatef(-50.0F, 0.0F, 0.0F, 1.0F);
		renderBakedItemModel(bakedModel, -8372020, null);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		GlStateManager.scalef(8.0F, 8.0F, 8.0F);
		float g = (float) (Minecraft.getTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translatef(-g, 0.0F, 0.0F);
		GlStateManager.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
		renderBakedItemModel(bakedModel, -8372020, null);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableLighting();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		Minecraft.getInstance().getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
	}

	private void renderQuad(BufferBuilder bufferBuilder, BakedQuad bakedQuad, int color) {
		bufferBuilder.vertices(bakedQuad.getVertices());
		bufferBuilder.setQuadColor(color);
		Vec3i vec3i = bakedQuad.getFace().getNormal();
		bufferBuilder.postNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
	}


	public static void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel,
											int textColor, boolean shadow) {
		Lighting.turnOnGui();
		GlStateManager.pushMatrix();
		GlStateManager.color4f(textColor >> 24 & 0xff, textColor >> 16 & 0xff, textColor >> 8 & 0xff, textColor & 0xff);
		if (stack != null) {
			if (stack.size != 1 || countLabel != null) {
				String string = countLabel == null ? String.valueOf(stack.size) : countLabel;
				if (countLabel == null && stack.size < 1) {
					string = Formatting.RED + String.valueOf(stack.size);
				}

				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableBlend();
				renderer.draw(string, (float) (x + 19 - 2 - renderer.getWidth(string)), (float) (y + 6 + 3),
					16777215, shadow);
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}

			if (stack.isDamaged()) {
				int i = (int) Math.round(13.0 - (double) stack.getDamage() * 13.0 / (double) stack.getMaxDamage());
				int j = (int) Math.round(255.0 - (double) stack.getDamage() * 255.0 / (double) stack.getMaxDamage());
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableTexture();
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferBuilder = tessellator.getBuilder();
				renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
				renderGuiQuad(bufferBuilder, x + 2, y + 13, 12, 1, (255 - j) / 4, 64, 0, 255);
				renderGuiQuad(bufferBuilder, x + 2, y + 13, i, 1, 255 - j, j, 0, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableTexture();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}
		}

		Lighting.turnOff();
		GlStateManager.popMatrix();
	}

	private static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int red, int green,
									  int blue, int alpha) {
		buffer.begin(7, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).nextVertex();
		buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).nextVertex();
		Tessellator.getInstance().end();
	}

	public static class ItemStorage {

		public final ItemStack stack;
		public int times;

		public ItemStorage(ItemStack stack, int times) {
			ItemStack copy = stack.copy();
			copy.size = 1;
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
			this.start = Minecraft.getTime();
		}

		public float getPassedTime() {
			return Minecraft.getTime() - start;
		}

		@Override
		public void incrementTimes(int num) {
			super.incrementTimes(num);
			refresh();
		}

		public void refresh() {
			start = Minecraft.getTime();
		}
	}
}
