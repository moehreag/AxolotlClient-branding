package io.github.axolotlclient.util.options;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.OptionBase;
import lombok.Getter;

@Getter
public class GenericOption extends OptionBase<GenericOption.ActionListener> {
	private final String label;
	public GenericOption(String name, String label, ActionListener listener) {
		super(name, listener);
		this.label =  label;
	}

	@Override
	public void set(ActionListener value) {

	}

	@Override
	public ActionListener get() {
		return getDefault();
	}

	@Override
	public String toSerializedValue() {
		return "";
	}

	@Override
	public void fromSerializedValue(String s) {

	}

	@Override
	public String getWidgetIdentifier() {
		return "generic";
	}

	public interface ActionListener {
		void onClick();
	}
}
