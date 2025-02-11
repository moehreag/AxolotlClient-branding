package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeystrokeKeyScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokeKeyScreen keyBindsScreen, List<KeystrokeHud.Keystroke> keys) {
		super(Minecraft.getInstance(), keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), 20);
		this.keyBindsScreen = keyBindsScreen;

		for (KeystrokeHud.Keystroke keyMapping : keys) {

			Component component = Component.translatable(keyMapping.getKey().getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}

		addEntry(new NewEntry());
	}

	public void refreshEntries() {
		this.children().forEach(Entry::refreshEntry);
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
		abstract void refreshEntry();

	}
	private static final Component CONFIGURE_BUTTON_TITLE = Component.translatable("keystrokes.stroke.configure");
	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final Component REMOVE_BUTTON_TITLE = Component.translatable("keystrokes.stroke.remove");
		private final KeystrokeHud.Keystroke key;
		private Component name;
		private final Button configureButton, removeButton;

		KeyEntry(final KeystrokeHud.Keystroke key, final Component name) {
			this.key = key;
			this.name = name;
			this.configureButton = Button.builder(CONFIGURE_BUTTON_TITLE, button -> minecraft.setScreen(new KeyBindSelectionScreen(keyBindsScreen, key)))
				.bounds(0, 0, 75, 20)
				.build();
			this.removeButton = Button.builder(REMOVE_BUTTON_TITLE, b -> {
				removeEntry(this);
				keyBindsScreen.removeKey(key);
			}).bounds(0, 0, 50, 20)
					.build();
			this.refreshEntry();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.scrollBarX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.setPosition(i, j);
			this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.setPosition(k, j);
			this.configureButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawString(KeyBindsList.this.minecraft.font, this.name, left, top + height / 2 - 9 / 2, -1);
			guiGraphics.drawString(minecraft.font, key.getKey().getTranslatedKeyMessage(), left+k/2, top+height/2 - 9/2, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.configureButton, removeButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.configureButton, removeButton);
		}

		@Override
		protected void refreshEntry() {
			this.name = Component.translatable(key.getKey().getName());
		}
	}

	public class NewEntry extends Entry {

		private static final Component NOT_BOUND = Component.translatable("keystrokes.stroke.not_bound").withStyle(Style.EMPTY.withItalic(true));
		private final Button addButton;
		private final Button configureButton;
		private final EditBox nameInput;
		private KeystrokeHud.Keystroke key = keyBindsScreen.hud.newStroke();

		public NewEntry() {
			this.nameInput = new EditBox(minecraft.font, 120, 20, Component.empty());
			this.addButton = Button.builder(Component.translatable("keystrokes.stroke.add"), b -> {
				removeEntry(this);
				addEntry(new KeyEntry(key, nameInput.getValue().isBlank() ? Component.translatable("Empty name") : Component.literal(nameInput.getValue())));
				keyBindsScreen.hud.keystrokes.add(key);
				addEntry(this);
				key = keyBindsScreen.hud.newStroke();
				refreshEntry();
			}).bounds(0, 0, 50, 20).build();
			addButton.active = false;
			this.configureButton = Button.builder(CONFIGURE_BUTTON_TITLE, button -> minecraft.setScreen(new KeyBindSelectionScreen(keyBindsScreen, key)))
				.bounds(0, 0, 75, 20)
				.build();
		}

		@Override
		void refreshEntry() {
			if (key.getKey() != null) {
				addButton.active = true;
			}
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(nameInput, configureButton, addButton);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.scrollBarX() - addButton.getWidth() - 10;
			int j = top - 2;
			this.addButton.setPosition(i, j);
			this.addButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.setPosition(k, j);
			this.configureButton.render(guiGraphics, mouseX, mouseY, partialTick);
			this.nameInput.setPosition(left, top);
			this.nameInput.setSize(Math.min(200, k/2 - 4), height);
			this.nameInput.render(guiGraphics, mouseX, mouseY, partialTick);
			if (key.getKey() != null) {
				guiGraphics.drawString(minecraft.font, key.getKey().getTranslatedKeyMessage(), left + k / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
			} else {
				guiGraphics.drawString(minecraft.font, NOT_BOUND, left + k / 2, top + height / 2 - 9 / 2, Colors.GRAY.toInt());
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(nameInput, configureButton, addButton);
		}
	}
}
