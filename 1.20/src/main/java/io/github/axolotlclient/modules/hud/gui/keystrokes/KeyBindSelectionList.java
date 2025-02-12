package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ElementPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigationEvent;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class KeyBindSelectionList extends ElementListWidget<KeyBindSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeyBindSelectionScreen keyBindsScreen;
	private final Consumer<KeyBind> selectionConsumer;
	private int maxNameWidth;

	public KeyBindSelectionList(KeyBindSelectionScreen keyBindsScreen, MinecraftClient minecraft, Consumer<KeyBind> selectionConsumer) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height-33, ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		this.selectionConsumer = selectionConsumer;
		KeyBind[] keyMappings = ArrayUtils.clone(minecraft.options.allKeys);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyBind keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new CategoryEntry(Text.translatable(string2)));
			}

			Text component = Text.translatable(keyMapping.getTranslationKey());
			int i = minecraft.textRenderer.getWidth(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft()+getRowWidth()+10;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends Entry {
		final Text name;
		private final int width;

		public CategoryEntry(final Text name) {
			this.name = name;
			this.width = KeyBindSelectionList.this.client.textRenderer.getWidth(this.name);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			guiGraphics.drawShadowedText(KeyBindSelectionList.this.client.textRenderer, this.name, KeyBindSelectionList.this.width / 2 - this.width / 2, top + height - 9 - 1, -1);
		}

		@Override
		public @Nullable ElementPath nextFocusPath(GuiNavigationEvent event) {
			return null;
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(new Selectable() {
				@Override
				public SelectionType getType() {
					return SelectionType.HOVERED;
				}

				@Override
				public void appendNarrations(NarrationMessageBuilder narrationElementOutput) {
					narrationElementOutput.put(NarrationPart.TITLE, CategoryEntry.this.name);
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private final Text name, boundKey;
		private final ButtonWidget changeButton;

		KeyEntry(final KeyBind key, final Text name) {
			this.name = name;
			this.boundKey = key.getKeyName();
			this.changeButton = ButtonWidget.builder(Text.translatable("keystrokes.key.select"), button -> {
					selectionConsumer.accept(key);
					keyBindsScreen.closeScreen();
				})
				.positionAndSize(0, 0, 75, 20)
				.build();
			changeButton.active = !(keyBindsScreen.stroke.getKey() != null && key == keyBindsScreen.stroke.getKey());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindSelectionList.this.getScrollbarPositionX() - 10;
			int j = top - 2;
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.setPosition(k, j);
			this.changeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawShadowedText(client.textRenderer, this.name, left, top + height / 2 - 9 / 2, -1);
			guiGraphics.drawShadowedText(client.textRenderer, boundKey, left + width / 2 - client.textRenderer.getWidth(boundKey) / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.changeButton);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return ImmutableList.of(this.changeButton);
		}
	}
}
