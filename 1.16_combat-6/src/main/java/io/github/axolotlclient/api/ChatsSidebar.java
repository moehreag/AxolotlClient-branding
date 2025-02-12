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

package io.github.axolotlclient.api;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.impl.ui.Selectable;
import io.github.axolotlclient.AxolotlClientConfig.impl.util.DrawUtil;
import io.github.axolotlclient.api.chat.ChatWidget;
import io.github.axolotlclient.api.handlers.ChatHandler;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.Channel;
import io.github.axolotlclient.api.types.User;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class ChatsSidebar extends Screen implements ContextMenuScreen {

	private static final int ANIM_STEP = 8;
	private final Screen parent;
	private int sidebarAnimX;
	private int sidebarWidth;
	private boolean remove;
	private boolean hasChat;
	private ListWidget list;
	private TextFieldWidget input;
	private Channel channel;

	private ChatWidget chatWidget;

	private ContextMenuContainer contextMenu;

	public ChatsSidebar(Screen parent) {
		super(new TranslatableText("api.chats.sidebar"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
		if (parent != null) {
			parent.render(graphics, mouseX, mouseY, delta);
		}
		graphics.push();
		graphics.translate(0, 0, 1000);
		fill(graphics, sidebarAnimX, 0, sidebarWidth + sidebarAnimX, height, 0x99000000);

		RenderSystem.color3f(1, 1, 1);

		textRenderer.drawWithShadow(graphics, I18n.translate("api.chats"), 10 + sidebarAnimX, 10, -1);

		if (hasChat) {
			fill(graphics, 70 + sidebarAnimX, 0, 70 + sidebarAnimX + 1, height, 0xFF000000);
			textRenderer.drawWithShadow(graphics, channel.getName(), sidebarAnimX + 75, 20, -1);
			if (channel.isDM() && ((Channel.DM) channel).getReceiver().getStatus().isOnline()) {
				textRenderer.drawWithShadow(graphics, Formatting.ITALIC + ((Channel.DM) channel).getReceiver().getStatus().getTitle() + ": " + ((Channel.DM) channel).getReceiver().getStatus().getDescription(),
					sidebarAnimX + 80, 30, 8421504);
			}
			chatWidget.render(graphics, mouseX, mouseY, delta);
		}

		if (list != null) {
			list.render(graphics, mouseX, mouseY, delta);
		}

		super.render(graphics, mouseX, mouseY, delta);

		contextMenu.render(graphics, mouseX, mouseY, delta);
		animate();
		graphics.pop();
	}

	@Override
	protected void init() {
		removeChat();
		sidebarWidth = 70;
		sidebarAnimX = -sidebarWidth;

		if (parent != null) {
			parent.children().stream().filter(element -> element instanceof AbstractButtonWidget)
				.map(e -> (AbstractButtonWidget) e).filter(e -> e.getMessage().equals(new TranslatableText("api.chats"))).forEach(e -> e.visible = false);
		}

		ChannelRequest.getChannelList().whenCompleteAsync((list, t) ->
			addChild(this.list = new ListWidget(list, 10, 30, 50, height - 60))
		);

		addButton(new ButtonWidget(10 - sidebarWidth, height - 30, 50, 20, new TranslatableText("gui.back"), buttonWidget -> remove()));
		addChild(contextMenu = new ContextMenuContainer());
	}

	public void remove() {
		remove = true;

	}

	@Override
	public void tick() {
		if (input != null) {
			input.tick();
		}
	}

	@Override
	public void removed() {
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return parent != null && parent.isPauseScreen();
	}

	private void animate() {
		if (sidebarAnimX < 0 && !remove) {
			if (sidebarAnimX > -ANIM_STEP) {
				sidebarAnimX = -ANIM_STEP;
			}
			sidebarAnimX += ANIM_STEP;
			if (list != null) {
				list.visible = false;
			}
			getButtons().forEach(button -> button.x = (button.x + ANIM_STEP));
		} else if (remove) {
			if (sidebarAnimX < -sidebarWidth) {
				close();
			}
			sidebarAnimX -= ANIM_STEP;
			if (list != null) {
				list.setX(list.getX() - ANIM_STEP);
			}
			getButtons().forEach(button -> button.x = (button.x - ANIM_STEP));
			if (chatWidget != null) {
				chatWidget.setLeftPos(chatWidget.getX() - ANIM_STEP);
			}
		} else {
			if (list != null && !list.visible) {
				list.visible = true;
			}
		}
	}

	public List<AbstractButtonWidget> getButtons() {
		return children().stream().filter(element -> element instanceof AbstractButtonWidget).map(element -> (AbstractButtonWidget) element).collect(Collectors.toList());
	}

	private void close() {
		client.openScreen(parent);
		if (chatWidget != null) {
			chatWidget.remove();
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (contextMenu.getMenu() != null) {
			if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			contextMenu.removeMenu();
		}
		if (mouseX > sidebarWidth) {
			remove();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void removeChat() {
		hasChat = false;
		children().remove(chatWidget);
		children().remove(input);
	}

	private void addChat(Channel channel) {
		if (hasChat) {
			removeChat();
		}
		hasChat = true;
		list.elements.forEach(b -> b.active = true);
		this.channel = channel;
		int w;
		if (channel.isDM()) {
			User chatUser = ((Channel.DM) channel).getReceiver();
			w = Math.max(client.textRenderer.getWidth(chatUser.getStatus().getTitle() + ": " + chatUser.getStatus().getDescription()) + 5,
				client.textRenderer.getWidth(channel.getName()));
		} else {
			w = client.textRenderer.getWidth(channel.getName());
		}
		sidebarWidth = Math.min(Math.max(width * 5 / 12, w + 5), width/2);
		chatWidget = new ChatWidget(channel, 75, 50, sidebarWidth - 80, height - 100, this);
		addChild(chatWidget);
		addButton(input = new TextFieldWidget(textRenderer, 75, height - 30, sidebarWidth - 80, 20, new TranslatableText("api.friends.chat.input")) {
			@Override
			public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if (keyCode == GLFW.GLFW_KEY_ENTER) {
					ChatHandler.getInstance().sendMessage(channel, input.getText());
					input.setText("");
					return true;
				}
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		});
		input.setSuggestion(input.getMessage().getString());
		input.setChangedListener(s -> {
			if (s.isEmpty()) {
				input.setSuggestion(input.getMessage().getString());
			} else {
				input.setSuggestion("");
			}
		});
		input.setMaxLength(1024);
	}

	@Override
	public ContextMenuContainer getMenuContainer() {
		return contextMenu;
	}

	@Override
	public Screen getSelf() {
		return this;
	}

	@Override
	public Screen getParent() {
		return parent;
	}

	private class ListWidget extends AbstractParentElement implements Drawable, Element, Selectable {
		private final List<AbstractButtonWidget> elements;
		private final int y;
		private final int width;
		private final int height;
		private final int entryHeight = 25;
		protected boolean hovered;
		private int x;
		private int scrollAmount;
		private boolean visible;

		public ListWidget(List<Channel> list, int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			AtomicInteger buttonY = new AtomicInteger(y);
			elements = list.stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
				.map(channel -> new ButtonWidget(x, buttonY.getAndAdd(entryHeight), width, entryHeight - 5,
					Text.of(channel.getName()), buttonWidget -> {
					addChat(channel);
					buttonWidget.active = false;
				})).collect(Collectors.toList());
		}

		@Override
		public void render(MatrixStack graphics, int mouseX, int mouseY, float delta) {
			if (visible) {
				graphics.push();
				DrawUtil.pushScissor(x, y, width, height);

				AtomicInteger buttonY = new AtomicInteger(y);
				elements.forEach(e -> {
					e.y = buttonY.get() - scrollAmount;
					e.render(graphics, mouseX, mouseY, delta);
					buttonY.getAndAdd(entryHeight);
				});

				DrawUtil.popScissor();
				graphics.pop();
			}
		}

		@Override
		public List<? extends Element> children() {
			return elements;
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amountY) {
			if (this.isMouseOver(mouseX, mouseY)) {
				if (elements.size() * entryHeight > height) {
					int a = scrollAmount;
					a -= (int) (amountY * (entryHeight / 2f));
					scrollAmount = MathHelper.clamp(a, 0, elements.size() * entryHeight - height);
					return true;
				}
			}
			return super.mouseScrolled(mouseX, mouseY, amountY);
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return hovered = visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
			elements.forEach(e -> e.x = x);
		}

		@Override
		public SelectionType getType() {
			return this.hovered ? SelectionType.HOVERED : SelectionType.NONE;
		}
	}
}
