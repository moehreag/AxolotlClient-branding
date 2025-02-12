package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class KeyBindSelectionList extends ElementListWidget<KeyBindSelectionList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeyBindSelectionScreen keyBindsScreen;
	private final Consumer<KeyBinding> selectionConsumer;
	private int maxNameWidth;

	public KeyBindSelectionList(KeyBindSelectionScreen keyBindsScreen, Minecraft minecraft, Consumer<KeyBinding> selectionConsumer) {
		super(minecraft, keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height - 33, ITEM_HEIGHT);
		this.keyBindsScreen = keyBindsScreen;
		this.selectionConsumer = selectionConsumer;
		KeyBinding[] keyMappings = ArrayUtils.clone(minecraft.options.keyBindings);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyBinding keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new CategoryEntry(I18n.translate(string2)));
			}

			String component = I18n.translate(keyMapping.getName());
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
		return getRowLeft() + getRowWidth() + 10;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends Entry {
		final String name;
		private final int width;

		public CategoryEntry(final String name) {
			this.name = name;
			this.width = KeyBindSelectionList.this.client.textRenderer.getWidth(this.name);
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			KeyBindSelectionList.this.client.textRenderer.drawWithShadow(this.name, KeyBindSelectionList.this.width / 2f - this.width / 2f, top + height - 9 - 1, -1);
		}

		@Override
		public List<? extends Element> children() {
			return Collections.emptyList();
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private final String name, boundKey;
		private final ButtonWidget changeButton;

		KeyEntry(final KeyBinding key, final String name) {
			this.name = name;
			this.boundKey = GameOptions.getKeyName(key.getKeyCode());
			this.changeButton = new VanillaButtonWidget(0, 0, 75, 20, I18n.translate("keystrokes.key.select"), button -> {
				selectionConsumer.accept(key);
				keyBindsScreen.closeScreen();
			});
			changeButton.active = !(keyBindsScreen.stroke.getKey() != null && key == keyBindsScreen.stroke.getKey());
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindSelectionList.this.getScrollbarPositionX() - 10;
			int j = top - 2;
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.setPosition(k, j);
			this.changeButton.render(mouseX, mouseY, partialTick);
			client.textRenderer.drawWithShadow(this.name, left, top + height / 2f - 9 / 2f, -1);
			client.textRenderer.drawWithShadow(boundKey, left + width / 2f - client.textRenderer.getWidth(boundKey) / 2f, top + height / 2f - 9 / 2f, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.changeButton);
		}
	}
}
