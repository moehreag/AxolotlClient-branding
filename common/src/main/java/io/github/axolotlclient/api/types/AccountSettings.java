package io.github.axolotlclient.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class AccountSettings {
	private boolean showRegistered;
	private boolean retainUsernames;
	private boolean showLastOnline;
	private boolean showActivity;
}
