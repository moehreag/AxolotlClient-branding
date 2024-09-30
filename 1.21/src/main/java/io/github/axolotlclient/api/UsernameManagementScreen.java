package io.github.axolotlclient.api;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.axolotlclient.api.requests.AccountUsernameRequest;
import io.github.axolotlclient.api.types.User;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.list.ElementListWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class UsernameManagementScreen extends Screen {

	private final HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
	private final Screen parent;
	private UsernameListWidget widget;

	public UsernameManagementScreen(Screen parent) {
		super(Text.translatable("api.account.usernames"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.setHeaderHeight(45);
		layout.setFooterHeight(55);
		layout.addToHeader(getTitle(), this.textRenderer);


		layout.addToFooter(ButtonWidget.builder(CommonTexts.BACK, b -> closeScreen()).build());
		if (API.getInstance().isAuthenticated()) {
			widget = new UsernameListWidget(API.getInstance().getSelf().getPreviousUsernames());
		} else {
			widget = new UsernameListWidget(Collections.emptyList());
		}
		layout.addToContents(widget);
		layout.arrangeElements();

		layout.visitWidgets(this::addDrawableSelectableElement);
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
		widget.setDimensionsWithLayout(width, layout);
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
	}

	private class UsernameListWidget extends ElementListWidget<UsernameListWidget.UsernameListEntry> {

		public UsernameListWidget(List<User.OldUsername> names) {
			super(UsernameManagementScreen.this.client, UsernameManagementScreen.this.width, UsernameManagementScreen.this.layout.getContentsHeight(), UsernameManagementScreen.this.layout.getHeaderHeight(), 20);

			names.forEach(n -> addEntry(new UsernameListEntry(n)));
		}

		@Override
		public int getRowWidth() {
			return 310;
		}

		private class UsernameListEntry extends ElementListWidget.Entry<UsernameListEntry> {

			private final ButtonWidget visibility;
			private final ButtonWidget delete;
			private final String name;

			public UsernameListEntry(User.OldUsername name) {
				visibility = ButtonWidget.builder(Text.translatable("api.account.usernames.public", name.isPub()), w -> {
					name.setPub(!name.isPub());
					w.setMessage(Text.translatable("api.account.usernames.public", name.isPub()));
					AccountUsernameRequest.post(name.getName(), name.isPub());
				}).width(100).build();
				delete = ButtonWidget.builder(Text.translatable("api.account.usernames.delete"), w ->
					client.setScreen(new ConfirmScreen(b -> {
						if (b) {
							AccountUsernameRequest.delete(name.getName()).thenRun(() ->
								UsernameListWidget.this.removeEntry(this));
						}
						client.setScreen(UsernameManagementScreen.this);
					}, Text.translatable("api.account.confirm_deletion"),
						Text.translatable("api.account.usernames.delete.desc")))).width(100).build();
				this.name = name.getName();
			}

			@Override
			public List<? extends Selectable> selectableChildren() {
				return ImmutableList.of(visibility, delete);
			}

			@Override
			public List<? extends Element> children() {
				return List.of(visibility, delete);
			}

			@Override
			public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
				int deleteX = UsernameListWidget.this.getScrollbarPositionX() - delete.getWidth() - 10;
				delete.setPosition(deleteX, y - 2);
				visibility.setPosition(deleteX - visibility.getWidth() - 5, y - 2);
				delete.render(graphics, mouseX, mouseY, tickDelta);
				visibility.render(graphics, mouseX, mouseY, tickDelta);
				graphics.drawShadowedText(textRenderer, name, x, y + entryHeight / 2 - 9 / 2, -1);
			}
		}
	}
}
