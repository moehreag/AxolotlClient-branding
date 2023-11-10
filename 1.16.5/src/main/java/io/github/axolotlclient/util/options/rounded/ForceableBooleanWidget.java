package io.github.axolotlclient.util.options.rounded;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.PillBooleanWidget;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.client.util.math.MatrixStack;

public class ForceableBooleanWidget extends PillBooleanWidget {
	private final ForceableBooleanOption option;

	public ForceableBooleanWidget(int x, int y, int width, int height, ForceableBooleanOption option) {
		super(x, y, width, height, option);
		this.option = option;
	}

	@Override
	public void renderButton(MatrixStack stack, int mouseX, int mouseY, float delta) {
		this.active = !option.isForceOff();
		super.renderButton(stack, mouseX, mouseY, delta);
	}

	@Override
	protected void drawHandle(long ctx, float x, float y, float width) {
		if (option.isForceOff()) {
			super.drawHandle(ctx, getX() + OFF_POSITION, y, width);
		} else {
			super.drawHandle(ctx, x, y, width);
		}
	}

	@Override
	public void onPress() {
		if (!option.isForceOff()) {
			super.onPress();
		}
	}
}
