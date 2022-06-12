package io.github.moehreag.axolotlclient.config.screen.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.moehreag.axolotlclient.config.options.StringOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class StringOptionWidget extends TextFieldWidget {

	public final StringOption option;
	public StringOptionWidget(int x, int y, int width, StringOption option) {
		super(MinecraftClient.getInstance().textRenderer, x, y, width, 20, Text.of(option.get()));
		this.option=option;
	}

    /*public TextFieldWidget textField;

    public StringOptionWidget(int x, int y, StringOption option){
        super(MinecraftClient.getInstance().textRenderer, x, y, 150, 40, option.get());
        textField = new TextFieldWidget(0, MinecraftClient.getInstance().textRenderer, x, y, 150, 20){
            @Override
            public void mouseClicked(int mouseX, int mouseY, int button) {
                if(isMouseOver(MinecraftClient.getInstance(), mouseX, mouseY)) {
                    super.mouseClicked(mouseX, mouseY, button);
                } else {
                    this.setFocused(false);
                }
            }
        };
        this.option=option;
        textField.setText(option.get());
        textField.setVisible(true);
        textField.setEditable(true);
        textField.setMaxLength(512);
    }

    @Override
    public void render(MinecraftClient client, int mouseX, int mouseY) {
        GlStateManager.disableDepthTest();
        //MinecraftClient.getInstance().textRenderer.draw(I18n.translate(option.getName()), x, y, -1);
        textField.y = y;
        textField.x = x;
        textField.render();
        GlStateManager.enableDepthTest();
    }


    public void keyPressed(char c, int code){
        this.textField.keyPressed(c, code);
        this.option.set(textField.getText());
    }*/



}
