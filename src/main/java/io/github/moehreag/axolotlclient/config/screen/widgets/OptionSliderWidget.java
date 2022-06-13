package io.github.moehreag.axolotlclient.config.screen.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moehreag.axolotlclient.config.options.DoubleOption;
import io.github.moehreag.axolotlclient.config.options.FloatOption;
import io.github.moehreag.axolotlclient.config.options.IntegerOption;
import io.github.moehreag.axolotlclient.config.options.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

public class OptionSliderWidget extends ButtonWidget {
	/*public OptionSliderWidget(int x, int y, int width, Text text, PressAction pressAction) {
		super(x, y, width, 20, text, pressAction);
	}*/
    private final DecimalFormat format = new DecimalFormat("##.#");
    private final DecimalFormat intformat = new DecimalFormat("##");

    private double value;
    public boolean dragging;
    private final Option option;
    private final double min;
    private final double max;

	protected Text message;

    public OptionSliderWidget(int x, int y, FloatOption option) {
        this(x, y, option, option.getMin(), option.getMax());
    }

    public OptionSliderWidget(int x, int y, FloatOption option, float min, float max) {
        super(x, y, 150, 20, Text.empty(), buttonWidget -> {});
        this.option = option;
        this.min = min;
        this.max = max;
        this.value = (option.get() - min) / (max - min);
        this.message = this.getMessage();
    }

    public OptionSliderWidget(int x, int y, IntegerOption option) {
        this(x, y, option, option.getMin(), option.getMax());
    }

    public OptionSliderWidget(int x, int y, IntegerOption option, float min, float max) {
        super(x, y, 150, 20, Text.empty(), buttonWidget -> {});
        this.option = option;
        this.min = min;
        this.max = max;
        this.value = (option.get() - min) / (max - min);
        this.message = this.getMessage();
    }

    public OptionSliderWidget(int x, int y, DoubleOption option) {
        this(x, y, option, option.getMin(), option.getMax());
    }

    public OptionSliderWidget(int x, int y, DoubleOption option, double min, double max) {
        super(x, y, 150, 20, Text.empty(), buttonWidget -> {});
        this.option = option;
        this.min = min;
        this.max = max;
        this.value = (option.get() - min) / (max - min);
        this.message = this.getMessage();
    }

    public float getSliderValue() {
        format.applyLocalizedPattern("###.#");
        return Float.parseFloat(format.format(this.min + (this.max - this.min) * this.value));
    }
    public int getSliderValueAsInt() {
        intformat.applyLocalizedPattern("##");
        return Integer.parseInt(intformat.format(this.min + (this.max - this.min) * this.value));
    }

    public Text getMessage() {
        return Text.of(""+ (option instanceof IntegerOption? getSliderValueAsInt(): this.getSliderValue()));
    }

    protected int getYImage(boolean isHovered) {
        return 0;
    }

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.visible) {
	        this.renderBackground(matrices, MinecraftClient.getInstance(), mouseX, mouseY);
            if (this.dragging) {
                this.value = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);

                if (this.value < 0.0F) {
                    this.value = 0.0F;
                }
                if (this.value > 1.0F) {
                    this.value = 1.0F;
                }

                if(option!=null) {
                    if (option instanceof FloatOption) ((FloatOption) option).set(getSliderValue());
                    else if (option instanceof DoubleOption) ((DoubleOption) option).set(getSliderValue());
                    else if (option instanceof IntegerOption) ((IntegerOption) option).set(getSliderValueAsInt());
                }

                this.message = this.getMessage();
            }

            RenderSystem.setShaderTexture(0, ClickableWidget.WIDGETS_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexture(matrices, this.x + (int)(this.value * (float)(this.width - 8)), this.y, 0, 66 + (hovered ? 20:0), 4, 20);
            this.drawTexture(matrices, this.x + (int)(this.value * (float)(this.width - 8)) + 4, this.y, 196, 66 + (hovered ? 20:0), 4, 20);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) {
            this.value = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            this.value = MathHelper.clamp(this.value, 0.0F, 1.0F);
            if (option instanceof FloatOption) ((FloatOption) option).set(getSliderValue());
            else if (option instanceof DoubleOption) ((DoubleOption) option).set(getSliderValue());
            else if (option instanceof IntegerOption) ((IntegerOption) option).set(getSliderValueAsInt());
            this.message = getMessage();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

}
