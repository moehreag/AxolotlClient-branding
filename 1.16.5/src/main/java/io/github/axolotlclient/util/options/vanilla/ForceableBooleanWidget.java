package io.github.axolotlclient.util.options.vanilla;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.BooleanWidget;
import io.github.axolotlclient.util.options.ForceableBooleanOption;
import net.minecraft.client.util.math.MatrixStack;

public class ForceableBooleanWidget extends BooleanWidget {
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
	public void onPress() {
		if (!option.isForceOff()) {
			super.onPress();
		}
	}
}
