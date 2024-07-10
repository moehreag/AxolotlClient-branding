/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.axolotlclient.util.options.rounded;

import io.github.axolotlclient.AxolotlClientConfig.impl.ui.rounded.widgets.PillBooleanWidget;
import io.github.axolotlclient.util.options.ForceableBooleanOption;

public class ForceableBooleanWidget extends PillBooleanWidget {
	private final ForceableBooleanOption option;

	public ForceableBooleanWidget(int x, int y, int width, int height, ForceableBooleanOption option) {
		super(x, y, width, height, option);
		this.option = option;
	}

	@Override
	public void drawWidget(int mouseX, int mouseY, float delta) {
		this.active = !option.isForceOff();
		super.drawWidget(mouseX, mouseY, delta);
	}

	@Override
	protected void drawHandle(long ctx, float x, float y, float width) {
		if (option.isForceOff()) {
			super.drawHandle(ctx, getX() + OFF_POSITION, y, width);
		} else {
			super.drawHandle(ctx, x, y, width);
		}
	}

	@Override
	public void onPress() {
		if (!option.isForceOff()) {
			super.onPress();
		}
	}
}
