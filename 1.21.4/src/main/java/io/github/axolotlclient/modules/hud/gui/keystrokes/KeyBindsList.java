package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
	private static final int ITEM_HEIGHT = 20;
	final KeystrokeKeyScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokeKeyScreen keyBindsScreen, List<KeystrokeHud.Keystroke> keys) {
		super(Minecraft.getInstance(), keyBindsScreen.width, keyBindsScreen.layout.getContentHeight(), keyBindsScreen.layout.getHeaderHeight(), 20);
		this.keyBindsScreen = keyBindsScreen;
		String string = null;

		for (KeystrokeHud.Keystroke keyMapping : keys) {

			Component component = Component.translatable(keyMapping.getKey().getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}
	}

	public void resetMappingAndUpdateButtons() {
		KeyMapping.resetMapping();
		this.refreshEntries();
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

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final Component RESET_BUTTON_TITLE = Component.translatable("controls.reset");
		private static final int PADDING = 10;
		private final KeystrokeHud.Keystroke key;
		private final Component name;
		private final Button changeButton;
		private boolean hasCollision = false;

		KeyEntry(final KeystrokeHud.Keystroke key, final Component name) {
			this.key = key;
			this.name = name;
			this.changeButton = Button.builder(name, button -> {
					KeyBindsList.this.keyBindsScreen.selectedKey = key;
					KeyBindsList.this.resetMappingAndUpdateButtons();
				})
				.bounds(0, 0, 75, 20)
				.createNarration(
					supplier -> key.isUnbound()
							? Component.translatable("narrator.controls.unbound", name)
							: Component.translatable("narrator.controls.bound", name, supplier.get())
				)
				.build();
			this.refreshEntry();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.scrollBarX() - this.resetButton.getWidth() - 10;
			int j = top - 2;
			this.resetButton.setPosition(i, j);
			this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
			int k = i - 5 - this.changeButton.getWidth();
			this.changeButton.setPosition(k, j);
			this.changeButton.render(guiGraphics, mouseX, mouseY, partialTick);
			guiGraphics.drawString(KeyBindsList.this.minecraft.font, this.name, left, top + height / 2 - 9 / 2, -1);
			if (this.hasCollision) {
				int l = 3;
				int m = this.changeButton.getX() - 6;
				guiGraphics.fill(m, top - 1, m + 3, top + height, -65536);
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.changeButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.changeButton);
		}

		@Override
		protected void refreshEntry() {
			this.changeButton.setMessage(this.key.getKey().getTranslatedKeyMessage());
			this.hasCollision = false;
			MutableComponent mutableComponent = Component.empty();
			if (!this.key.getKey().isUnbound()) {
				for (KeyMapping keyMapping : KeyBindsList.this.minecraft.options.keyMappings) {
					if (keyMapping != this.key && this.key.same(keyMapping)) {
						if (this.hasCollision) {
							mutableComponent.append(", ");
						}

						this.hasCollision = true;
						mutableComponent.append(Component.translatable(keyMapping.getName()));
					}
				}
			}

			if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
				this.changeButton
					.setMessage(
						Component.literal("> ")
							.append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
							.append(" <")
							.withStyle(ChatFormatting.YELLOW)
					);
			}
		}
	}
}
