package io.github.axolotlclient.util.options.rounded;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.RoundedButtonWidget;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.client.resource.language.I18n;

public class GenericWidget extends RoundedButtonWidget {
	public GenericWidget(int x, int y, int width, int height, GenericOption option) {
		super(x, y, width, height, I18n.translate(option.getLabel()), w -> option.get().onClick());
	}
}
