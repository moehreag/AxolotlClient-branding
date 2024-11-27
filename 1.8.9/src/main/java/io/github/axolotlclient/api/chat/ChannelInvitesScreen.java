package io.github.axolotlclient.api.chat;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.ChannelInvite;
import io.github.axolotlclient.api.util.UUIDHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;

public class ChannelInvitesScreen extends Screen {
	private final Screen parent;
	private ButtonWidget acceptButton;
	private ButtonWidget denyButton;
	private InvitesListWidget invites;
	private final String title;

	public ChannelInvitesScreen(Screen parent) {
		super();
		this.title = I18n.translate("api.channels.invites");
		this.parent = parent;
	}

	@Override
	public void init() {
		invites = new InvitesListWidget(minecraft, height, 33, width, height - 88, 25);


		buttons.add(acceptButton = new ButtonWidget(1, width / 2 - 75, height - 55 / 2 - 2 - 20, 73, 20, I18n.translate("api.channels.invite.accept")));
		buttons.add(denyButton = new ButtonWidget(2, width / 2 + 2, height - 55 / 2 - 2 - 20, 73, 20, I18n.translate("api.channels.invite.ignore")));
		buttons.add(new ButtonWidget(0, width / 2 - 75, height - 55 / 2 + 2, 150, 20, I18n.translate("gui.back")));

		updateButtons();
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		invites.render(mouseX, mouseY, delta);
		super.render(mouseX, mouseY, delta);
		drawCenteredString(textRenderer, title, width / 2, 33 / 2, -1);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		invites.mouseClicked(i, j, k);
		super.mouseClicked(i, j, k);
	}

	@Override
	public void handleMouse() {
		invites.handleMouse();
		super.handleMouse();
	}

	@Override
	protected void buttonClicked(ButtonWidget buttonWidget) {
		if (buttonWidget.id == 0) {
			minecraft.openScreen(parent);
		} else if (buttonWidget.id == 1) {
			if (invites.getSelected() != null) {
				ChannelRequest.acceptChannelInvite(invites.getSelected().invite);
				init(minecraft, width, height);
			}
		} else if (buttonWidget.id == 2) {
			if (invites.getSelected() != null) {
				ChannelRequest.ignoreChannelInvite(invites.getSelected().invite);
				init(minecraft, width, height);
			}
		}
	}

	private void updateButtons() {
		denyButton.active = acceptButton.active = invites.getSelected() != null;
	}

	private class InvitesListWidget extends EntryListWidget {

		private final List<InvitesListEntry> entries = new ArrayList<>();
		private int selectedEntry = -1;

		public InvitesListWidget(Minecraft client, int screenHeight, int y, int width, int height, int entryHeight) {
			super(client, width, screenHeight, y, y + height, entryHeight);
			ChannelRequest.getChannelInvites().thenAccept(list ->
				list.stream().map(InvitesListEntry::new).forEach(entries::add));
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			boolean bl = super.mouseClicked(mouseX, mouseY, button);
			updateButtons();
			return bl;
		}

		@Override
		public Entry getEntry(int i) {
			return entries.get(i);
		}

		@Override
		protected int size() {
			return entries.size();
		}

		public InvitesListEntry getSelected() {
			if (selectedEntry >= 0) {
				return entries.get(selectedEntry);
			}
			return null;
		}

		private class InvitesListEntry implements Entry {

			private final ChannelInvite invite;

			public InvitesListEntry(ChannelInvite invite) {
				this.invite = invite;
			}

			@Override
			public void renderOutOfBounds(int i, int j, int k) {

			}

			@Override
			public void render(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovere) {
				drawString(textRenderer, I18n.translate("api.channels.invite.name", invite.channelName()), x + 2, y + 2, -1);
				drawString(textRenderer, ChatFormatting.ITALIC + I18n.translate("api.channels.invite.from", UUIDHelper.getUsername(invite.fromUuid())), x + 15, y + entryHeight - textRenderer.fontHeight - 1, 0x808080);
			}

			@Override
			public boolean mouseClicked(int i, int j, int k, int l, int m, int n) {
				selectedEntry = i;
				return true;
			}

			@Override
			public void mouseReleased(int i, int j, int k, int l, int m, int n) {

			}
		}
	}
}
