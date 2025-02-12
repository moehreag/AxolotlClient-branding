package io.github.axolotlclient.modules.hud.gui.keystrokes;

import io.github.axolotlclient.AxolotlClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.util.Colors;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.IntegerWidget;
import io.github.axolotlclient.modules.hud.gui.hud.KeystrokeHud;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ConfigureKeyBindScreen extends io.github.axolotlclient.AxolotlClientConfig.impl.ui.Screen {

	private final Screen parent;
	private final KeystrokeHud hud;
	public final KeystrokeHud.Keystroke stroke;
	private final IntegerOption width;
	private final IntegerOption height;
	private final boolean isAddScreen;
	private ButtonWidget addButton, synchronizeButton;
	private AbstractButtonWidget currentKey;

	public ConfigureKeyBindScreen(Screen parent, KeystrokeHud hud, KeystrokeHud.Keystroke stroke, boolean isAddScreen) {
		super("keystrokes.stroke.configure_stroke");
		this.parent = parent;
		this.hud = hud;
		this.stroke = stroke;

		width = new IntegerOption("", stroke.getBounds().width(), v -> {
			stroke.getBounds().width(v);
		}, 1, 100);
		height = new IntegerOption("", stroke.getBounds().height(), v -> {
			stroke.getBounds().height(v);
		}, 1, 100);
		this.isAddScreen = isAddScreen;
	}

	@Override
	public void init() {
		super.init();

		int leftColX = super.width / 2 - 4 - 150;
		int leftColY = 36 + 5;
		int rightColX = super.width / 2 + 4;
		int rightColY = 36;

		addDrawableChild(new AbstractButtonWidget(super.width / 2 - 100, rightColY, 200, 40, LiteralText.EMPTY) {
			@Override
			public void renderButton(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
				var rect = stroke.getRenderPosition();
				guiGraphics.push();
				guiGraphics.translate(x, y, 0);
				float scale = Math.min((float) getHeight() / rect.height(), (float) getWidth() / rect.width());
				guiGraphics.translate(getWidth() / 2f - (rect.width() * scale) / 2f, 0, 0);
				guiGraphics.scale(scale, scale, 1);
				guiGraphics.translate(-rect.x(), -rect.y(), 0);
				DrawUtil.fillRect(guiGraphics, rect, Colors.WHITE.withAlpha(128));
				stroke.render(guiGraphics);
				guiGraphics.pop();
			}
		}).active = false;
		leftColY += 48;
		rightColY += 48;

		currentKey = addDrawableChild(textWidget(0, rightColY, super.width, 9, LiteralText.EMPTY, textRenderer));
		if (stroke.getKey() != null) {
			currentKey.setMessage(new TranslatableText("keystrokes.stroke.key", stroke.getKey().getBoundKeyLocalizedText()));
		} else {
			currentKey.setMessage(LiteralText.EMPTY);
		}
		leftColY += 9 + 8;
		rightColY += 9 + 8;

		if (stroke.isLabelEditable()) {
			addDrawableChild(textWidget(leftColX, leftColY, 150, 20, new TranslatableText("keystrokes.stroke.label"), textRenderer));
			leftColY += 28;
			boolean supportsSynchronization = stroke instanceof KeystrokeHud.LabelKeystroke;

			var label = addDrawableChild(new TextFieldWidget(textRenderer, rightColX, rightColY, supportsSynchronization ? 73 : 150, 20, LiteralText.EMPTY));

			label.setText(stroke.getLabel());
			label.setChangedListener(stroke::setLabel);
			if (supportsSynchronization) {
				var s = (KeystrokeHud.LabelKeystroke) stroke;
				synchronizeButton = addDrawableChild(new ButtonWidget(rightColX + 75 + 2, rightColY, 73, 20,
					new TranslatableText("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? ScreenTexts.ON : ScreenTexts.OFF), b -> {
					s.setSynchronizeLabel(!s.isSynchronizeLabel());
					b.setMessage(new TranslatableText("keystrokes.stroke.label.synchronize_with_key", s.isSynchronizeLabel() ? ScreenTexts.ON : ScreenTexts.OFF));
					label.setEditable(!s.isSynchronizeLabel());
					if (s.isSynchronizeLabel()) {
						label.setText(stroke.getLabel());
					}
				}));
				synchronizeButton.active = s.getKey() != null;
				label.setEditable(!s.isSynchronizeLabel());
			}
			rightColY += 28;
		}
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, new TranslatableText("keystrokes.stroke.width"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, width));
		rightColY += 28;
		addDrawableChild(textWidget(leftColX, leftColY, 150, 20, new TranslatableText("keystrokes.stroke.height"), textRenderer));
		leftColY += 28;
		addDrawableChild(new IntegerWidget(rightColX, rightColY, 150, 20, height));

		rightColY += 28;

		addDrawableChild(new ButtonWidget(super.width / 2 - 150 - 4, rightColY, 150, 20,
			new TranslatableText("keystrokes.stroke.configure_key"), b -> {
			client.openScreen(new KeyBindSelectionScreen(this, stroke));
		}));
		addDrawableChild(new ButtonWidget(super.width / 2 + 4, rightColY, 150, 20,
			new TranslatableText("keystrokes.stroke.configure_position"), b -> {
			client.openScreen(new KeystrokePositioningScreen(this, hud, stroke));
		}));


		if (isAddScreen) {
			addButton = addDrawableChild(new ButtonWidget(super.width / 2 - 150 - 4, super.height - 33 / 2 - 10, 150, 20,
				new TranslatableText("keystrokes.stroke.add"), b -> {
				hud.keystrokes.add(stroke);
				onClose();
			}));
			addButton.active = stroke.getKey() != null;
		}
		addDrawableChild(new ButtonWidget(isAddScreen ? super.width / 2 + 4 : super.width / 2 - 75, super.height - 33 / 2 - 10, 150, 20,
			isAddScreen ? ScreenTexts.CANCEL : ScreenTexts.BACK, b -> onClose()));
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), super.width / 2, 33 / 2 - textRenderer.fontHeight / 2, -1);
	}

	@Override
	public void onClose() {
		client.openScreen(parent);
		AxolotlClientCommon.getInstance().saveConfig();
	}

	private static AbstractButtonWidget textWidget(int x, int y, int width, int height, Text text, TextRenderer renderer) {
		var widget = new AbstractButtonWidget(x, y, width, height, text) {
			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				drawCenteredText(matrices, renderer, getMessage(), x + width / 2, y, -1);
			}
		};
		widget.active = false;
		return widget;
	}
}
