/*
 * Copyright © 2021-2024 moehreag <moehreag@gmail.com> & Contributors
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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.api.requests.AccountUsernameRequest;
import io.github.axolotlclient.api.types.User;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class UsernameManagementScreen extends Screen {

	private final Screen parent;
	private UsernameListWidget widget;

	public UsernameManagementScreen(Screen parent) {
		super(new TranslatableText("api.account.usernames"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (API.getInstance().isAuthenticated()) {
			widget = new UsernameListWidget(API.getInstance().getSelf().getPreviousUsernames());
		} else {
			widget = new UsernameListWidget(Collections.emptyList());
		}
		addChild(widget);
		addButton(new ButtonWidget(width / 2 - 75, height - 25 - 20, 150, 20, new TranslatableText("gui.back"), b -> client.openScreen(parent)));
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		widget.render(graphics, mouseX, mouseY, delta);
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredText(graphics, textRenderer, getTitle(), width / 2, 25, -1);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		widget.updateSize(width, height, 45, height-55);
		super.resize(client, width, height);
	}

	private class UsernameListWidget extends ElementListWidget<UsernameListWidget.UsernameListEntry> {

		public UsernameListWidget(List<User.OldUsername> names) {
			super(UsernameManagementScreen.this.client, UsernameManagementScreen.this.width, UsernameManagementScreen.this.height, 45, UsernameManagementScreen.this.height - 55, 20);
			names.forEach(n -> addEntry(new UsernameListEntry(n)));

		}

		@Override
		public int getRowWidth() {
			return 310;
		}

		private class UsernameListEntry extends Entry<UsernameListEntry> {

			private final ButtonWidget visibility;
			private final ButtonWidget delete;
			private final String name;

			public UsernameListEntry(User.OldUsername name) {
				visibility = new ButtonWidget(0, 0, 100, 20, new TranslatableText("api.account.usernames.public", name.isPub()), w -> {
					name.setPub(!name.isPub());
					w.setMessage(new TranslatableText("api.account.usernames.public", name.isPub()));
					AccountUsernameRequest.post(name.getName(), name.isPub());
				});
				delete = new ButtonWidget(0, 0, 100, 20, new TranslatableText("api.account.usernames.delete"), w -> {
					client.openScreen(new ConfirmScreen(b -> {
						if (b) {
							AccountUsernameRequest.delete(name.getName()).thenRun(() ->
								UsernameListWidget.this.removeEntry(this));
						}
						client.openScreen(UsernameManagementScreen.this);
					}, new TranslatableText("api.account.confirm_deletion"),
						new TranslatableText("api.account.usernames.delete.desc")));
				});

				this.name = name.getName();
			}

			@Override
			public List<? extends Element> children() {
				return ImmutableList.of(visibility, delete);
			}

			@Override
			public void render(MatrixStack graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
				int deleteX = UsernameListWidget.this.getScrollbarPositionX() - delete.getWidth() - 10;
				delete.x = deleteX;
				delete.y = y - 2;
				visibility.x = deleteX - visibility.getWidth() - 5;
				visibility.y = y - 2;
				delete.render(graphics, mouseX, mouseY, tickDelta);
				visibility.render(graphics, mouseX, mouseY, tickDelta);
				textRenderer.drawWithShadow(graphics, name, x, y + entryHeight / 2f - 9 / 2f, -1);
			}
		}
	}
}
