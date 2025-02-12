package io.github.axolotlclient.modules.hud.gui.hud.simple;

import java.util.List;
import java.util.UUID;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;
import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import io.github.axolotlclient.util.options.GenericOption;
import net.minecraft.util.Identifier;

public class CustomHudEntry extends SimpleTextHudEntry {

	private final Identifier id;
	public final StringOption value = new StringOption("custom_hud.value", "Text");
	private final GenericOption removeEntry;

	public CustomHudEntry() {
		this.id = new Identifier("axolotlclient", "custom_hud/" + UUID.randomUUID());
		removeEntry = new GenericOption("custom_hud.remove", "custom_hud.remove.label", () -> {
			HudManager.getInstance().removeEntry(this.id);
			HudManager.getInstance().saveCustomEntries();
		});
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
	public Identifier getId() {
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
