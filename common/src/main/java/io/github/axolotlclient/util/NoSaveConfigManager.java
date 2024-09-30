package io.github.axolotlclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.manager.ConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;

public class NoSaveConfigManager implements ConfigManager {

	private final OptionCategory root;
	private final List<String> suppressed = new ArrayList<>();
	public NoSaveConfigManager(OptionCategory root) {
		this.root = root;
	}

	@Override
	public void save() {

	}

	@Override
	public void load() {

	}

	@Override
	public OptionCategory getRoot() {
		return root;
	}

	@Override
	public Collection<String> getSuppressedNames() {
		return suppressed;
	}

	@Override
	public void suppressName(String s) {
		suppressed.add(s);
	}
}
