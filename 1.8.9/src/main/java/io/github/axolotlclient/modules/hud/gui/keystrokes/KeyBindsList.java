package io.github.axolotlclient.modules.hud.gui.keystrokes;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.ButtonWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Element;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.ElementListWidget;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.language.I18n;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ElementListWidget<KeyBindsList.Entry> {
	final KeystrokesScreen keyBindsScreen;
	private int maxNameWidth;

	public KeyBindsList(KeystrokesScreen keyBindsScreen, List<KeystrokeHud.Keystroke> keys) {
		super(Minecraft.getInstance(), keyBindsScreen.width, keyBindsScreen.height, 33, keyBindsScreen.height - 33, 24);
		this.keyBindsScreen = keyBindsScreen;

		reload(keys);
	}

	public void reload(List<KeystrokeHud.Keystroke> keys) {
		clearEntries();
		for (KeystrokeHud.Keystroke keyMapping : keys) {

			String component = I18n.translate(keyMapping.getKey().getName());
			int i = client.textRenderer.getWidth(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyEntry(keyMapping, component));
		}

		addEntry(new SpacerEntry());
		addEntry(new NewEntry());
	}

	@Override
	protected int getScrollbarPositionX() {
		return getRowLeft() + getRowWidth() + 10;
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ElementListWidget.Entry<Entry> {

	}

	public static class SpacerEntry extends Entry {

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}

	private static final String CONFIGURE_BUTTON_TITLE = I18n.translate("keystrokes.stroke.configure");

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends Entry {
		private static final String REMOVE_BUTTON_TITLE = I18n.translate("keystrokes.stroke.remove");
		private final KeystrokeHud.Keystroke key;
		private String name;
		private final ButtonWidget configureButton, removeButton;

		KeyEntry(final KeystrokeHud.Keystroke key, final String name) {
			this.key = key;
			this.name = GameOptions.getKeyName(key.getKey().getKeyCode());
			this.configureButton = new VanillaButtonWidget(0, 0, 75, 20, CONFIGURE_BUTTON_TITLE, button -> client.openScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, false)));
			this.removeButton = new VanillaButtonWidget(0, 0, 50, 20, REMOVE_BUTTON_TITLE, b -> {
				removeEntry(this);
				keyBindsScreen.removeKey(key);
			});
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - removeButton.getWidth() - 10;
			int j = top - 2;
			this.removeButton.setPosition(i, j);
			this.removeButton.render(mouseX, mouseY, partialTick);
			int k = i - this.configureButton.getWidth();
			this.configureButton.setPosition(k, j);
			this.configureButton.render(mouseX, mouseY, partialTick);
			GlStateManager.pushMatrix();
			var rect = key.getRenderPosition();
			float scale = Math.min((float) height / rect.height(), (float) 100 / rect.width());
			GlStateManager.translatef(left, top, 0);
			GlStateManager.scalef(scale, scale, 1);
			GlStateManager.translatef(-rect.x(), -rect.y(), 0);
			key.render();
			GlStateManager.popMatrix();
			client.textRenderer.drawWithShadow(name, left + width / 2f - client.textRenderer.getWidth(name) / 2f, top + height / 2f - 9 / 2f, Colors.GRAY.toInt());
		}

		@Override
		public List<? extends Element> children() {
			return ImmutableList.of(this.configureButton, removeButton);
		}
	}

	public class NewEntry extends Entry {

		private final ButtonWidget addButton, addSpecialButton;
		private final KeystrokeHud.Keystroke key = keyBindsScreen.hud.newStroke();

		public NewEntry() {
			this.addButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("keystrokes.stroke.add"), button -> client.openScreen(new ConfigureKeyBindScreen(keyBindsScreen, keyBindsScreen.hud, key, true)));
			this.addSpecialButton = new VanillaButtonWidget(0, 0, 150, 20, I18n.translate("keystrokes.stroke.add.special"),
				button -> client.openScreen(new AddSpecialKeystrokeScreen(keyBindsScreen, keyBindsScreen.hud)));
		}

		@Override
		public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int i = KeyBindsList.this.getScrollbarPositionX() - width / 2 - 10 + 4;
			int j = top - 2;
			this.addButton.setPosition(i, j);
			this.addButton.render(mouseX, mouseY, partialTick);
			int k = i - addButton.getWidth() - 8;
			this.addSpecialButton.setPosition(k, j);
			this.addSpecialButton.render(mouseX, mouseY, partialTick);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(addSpecialButton, addButton);
		}
	}
}
