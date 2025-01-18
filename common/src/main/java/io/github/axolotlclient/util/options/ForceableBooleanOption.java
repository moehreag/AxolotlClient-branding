/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

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

	@Override
	public Boolean get() {
		return !isForceOff() && super.get();
	}

	public void setForceOff(boolean value, String reason) {
		this.forceOff = value;
		this.reason = reason;
	}

	@Override
	public String getTooltip() {
		return isForceOff() ? reason : super.getTooltip();
	}

	@Override
	public String getWidgetIdentifier() {
		return "forceableboolean";
	}
}
