package io.github.axolotlclient.util.options;

import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import lombok.Getter;

@Getter
public class ForceableBooleanOption extends BooleanOption {
	private boolean forceOff;
	private String reason;
	public ForceableBooleanOption(String name, Boolean defaultValue) {
		super(name, defaultValue);
	}

	public ForceableBooleanOption(String name, Boolean defaultValue, ChangeListener<Boolean> changeListener) {
		super(name, defaultValue, changeListener);
	}

	public void setForceOff(boolean value, String reason){
		this.forceOff = value;
		this.reason = reason;
	}


	@Override
	public String getWidgetIdentifier() {
		return "forceableboolean";
	}
}
