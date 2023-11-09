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

package io.github.axolotlclient.api;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class NewsScreen extends Screen {

	private final Screen parent;

	private int scrollAmount;
	private static final int SCROLL_STEP = 5;

	private List<String> lines;

	public NewsScreen(Screen parent) {
		super();

		this.parent = parent;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		renderBackground();

		client.textRenderer.drawWithShadow(I18n.translate("api.notes.title"), width/2F, 20, -1);

		GlStateManager.enableBlend();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, scrollAmount, 0);

		DrawUtil.enableScissor(0, 35, width, height-65);
		int y = 35;
		for (String t : lines) {
			client.textRenderer.drawWithShadow(t, 25, y, -1);
			y+=client.textRenderer.fontHeight;
		}
		DrawUtil.disableScissor();
		GlStateManager.popMatrix();


		int scrollbarY = 35 + ((height - 65) - 35)/(lines.size()) * -(scrollAmount/SCROLL_STEP);
		int scrollbarHeight = (height-65 - 35) / SCROLL_STEP;
		fill(width-15, 35, width-9, height-65, -16777216);
		fill(width-15, scrollbarY, width-9, scrollbarY + scrollbarHeight, -8355712);
		fill(width-15, scrollbarY, width-10, scrollbarY + scrollbarHeight-1, -4144960);

		super.render(mouseX, mouseY, delta);


	}

	@Override
	public void init() {
		lines = client.textRenderer.wrapLines(GlobalDataRequest.get().getNotes(), width-50);

		buttons.add(new ButtonWidget(0, width/2-100, height-45, 200, 20,
			I18n.translate("gui.back")));
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if(buttonWidget.id == 0){
			client.setScreen(parent);
		}
	}

	@Override
	public void handleMouse() {
		super.handleMouse();

		int i = Mouse.getDWheel();
		if (i != 0) {

			scrollAmount = (int) MathHelper.clamp(scrollAmount + (Math.signum(i)) * SCROLL_STEP,
				Math.min(0, - ((lines.size()+3)*client.textRenderer.fontHeight-(height-65))),
				0);
		}
	}
}
