package io.github.axolotlclient.util.options.rounded;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.RoundedButtonWidget;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.text.Text;

public class GenericWidget extends RoundedButtonWidget {
	public GenericWidget(int x, int y, int width, int height, GenericOption option) {
		super(x, y, width, height, Text.translatable(option.getLabel()), w -> option.get().onClick());
	}
}
