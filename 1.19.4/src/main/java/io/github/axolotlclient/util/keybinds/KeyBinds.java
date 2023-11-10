package io.github.axolotlclient.util.keybinds;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import io.github.axolotlclient.mixin.KeyBindAccessor;
import lombok.Getter;
import net.minecraft.client.option.KeyBind;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

public class KeyBinds {
	@Getter
	private final static KeyBinds instance = new KeyBinds();

	private final List<KeyBind> binds = new ArrayList<>();

	public KeyBind register(KeyBind bind) {
		binds.add(bind);

		if (!KeyBindAccessor.getOrderByCategories().containsKey(bind.getCategory())) {
			int index = KeyBindAccessor.getOrderByCategories().values().stream().max(Integer::compareTo).get() + 1;
			KeyBindAccessor.getOrderByCategories().put(bind.getCategory(), index);
		}

		return bind;
	}

	public KeyBind registerWithSimpleAction(KeyBind bind, Runnable action) {
		ClientTickEvents.END.register(c -> {
			if (bind.wasPressed()) {
				action.run();
			}
		});
		return register(bind);
	}

	public KeyBind[] process(KeyBind[] keys) {
		List<KeyBind> keyBinds = Lists.newArrayList(keys);
		keyBinds.addAll(binds);
		return keyBinds.toArray(KeyBind[]::new);
	}
}
