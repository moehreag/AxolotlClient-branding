package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class KeyBindSelectionList extends ContainerObjectSelectionList<KeyBindSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeyBindSelectionScreen keyBindsScreen;
	private final Consumer<KeyMapping> selectionConsumer;
	private int maxNameWidth;

	public KeyBindSelectionList(KeyBindSelectionScreen keyBindsScreen, Minecraft minecraft, Consumer<KeyMapping> selectionConsumer) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		this.selectionConsumer = selectionConsumer;
		KeyMapping[] keyMappings = ArrayUtils.clone(minecraft.options.keyMappings);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyMapping keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new KeyBindSelectionList.CategoryEntry(Component.translatable(string2)));
			}

			Component component = Component.translatable(keyMapping.getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyBindSelectionList.KeyEntry(keyMapping, component));
		}
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends KeyBindSelectionList.Entry {
		final Component name;
		private final int width;

		public CategoryEntry(final Component name) {
			this.name = name;
			this.width = KeyBindSelectionList.this.minecraft.font.width(this.name);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			guiGraphics.drawString(KeyBindSelectionList.this.minecraft.font, this.name, KeyBindSelectionList.this.width / 2 - this.width / 2, top + height - 9 - 1, -1);
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			return null;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(new NarratableEntry() {
				@Override
				public NarratableEntry.NarrationPriority narrationPriority() {
					return NarratableEntry.NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput narrationElementOutput) {
					narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindSelectionList.Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends KeyBindSelectionList.Entry {
		private final Component name, boundKey;
		private final Button changeButton;
		private boolean hasCollision = false;

		KeyEntry(final KeyMapping key, final Component name) {
			this.name = name;
			this.boundKey = key.getTranslatedKeyMessage();
			this.changeButton = Button.builder(Component.translatable("keystrokes.key.select"), button -> {
					selectionConsumer.accept(key);
					keyBindsScreen.onClose();
				})
				.bounds(0, 0, 75, 20)
				.createNarration(
					supplier -> key.isUnbound()
							? Component.translatable("narrator.controls.unbound", name)
							: Component.translatable("narrator.controls.bound", name, supplier.get())
				)
				.build();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindSelectionList.this.scrollBarX() - 10;
			int j = top - 2;
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.setPosition(k, j);
			this.changeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawString(KeyBindSelectionList.this.minecraft.font, this.name, left, top + height / 2 - 9 / 2, -1);
			if (this.hasCollision) {
				int l = 3;
				int m = this.changeButton.getX() - 6;
				guiGraphics.fill(m, top - 1, m + 3, top + height, -65536);
			}
			guiGraphics.drawString(minecraft.font, boundKey, left+k/2, top+height/2 - 9/2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.changeButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.changeButton);
		}
	}
}
