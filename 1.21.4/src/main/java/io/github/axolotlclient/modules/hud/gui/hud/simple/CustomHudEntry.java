package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class CustomHudEntry extends SimpleTextHudEntry {

	private final ResourceLocation id;
	private static int index;
	public final StringOption value = new StringOption("customhud.value", "Text");
	private final GenericOption removeEntry;
	private final int num;

	public CustomHudEntry() {
		this.num = index++;
		this.id = ResourceLocation.fromNamespaceAndPath("axolotlclient", "custom_hud/"+num);
		removeEntry = new GenericOption("customhud.remove", "customhud.remove.label", () -> {
			HudManager.getInstance().removeEntry(this.id);
			HudManager.getInstance().saveCustomEntries();
		});
	}

	@Override
	public String getName() {
		return I18n.get(getNameKey(), num);
	}

	@Override
	public String getNameKey() {
		return "custom_hud";
	}

	@Override
	public String getPlaceholder() {
		return value.get();
	}

	@Override
	public String getValue() {
		return value.get();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		var options = super.getConfigurationOptions();
		options.add(value);
		options.add(removeEntry);
		return options;
	}
}
