package io.github.axolotlclient.util.options.vanilla;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.vanilla.widgets.VanillaButtonWidget;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.text.TranslatableText;

public class GenericWidget extends VanillaButtonWidget {
	public GenericWidget(int x, int y, int width, int height, GenericOption option) {
		super(x, y, width, height, new TranslatableText(option.getLabel()), w -> option.get().onClick());
	}
}
