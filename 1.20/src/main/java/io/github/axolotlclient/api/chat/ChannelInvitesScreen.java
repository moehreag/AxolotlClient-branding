package io.github.axolotlclient.api.chat;

import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.ChannelInvite;
import io.github.axolotlclient.api.util.UUIDHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.HeaderAndFooterWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ChannelInvitesScreen extends Screen {
	private final Screen parent;
	private ButtonWidget acceptButton;
	private ButtonWidget denyButton;
	private InvitesListWidget invites;

	public ChannelInvitesScreen(Screen parent) {
		super(Text.translatable("api.channels.invites"));
		this.parent = parent;
	}

	@Override
	protected void init() {

		addDrawableChild(new TextWidget(width/2, 33/2, 0, 0,  title, textRenderer));

		invites = addDrawableChild(new InvitesListWidget(client, height, 33, width, height-88, 25));


		acceptButton = addDrawableChild(ButtonWidget.builder(Text.translatable("api.channels.invite.accept"), w -> {
			if (invites.getSelectedOrNull() != null) {
				ChannelRequest.acceptChannelInvite(invites.getSelectedOrNull().invite);
				clearAndInit();
			}
		}).positionAndSize(width/2-75, height-55/2 - 2 - 20, 73, 20).build());
		denyButton = addDrawableChild(ButtonWidget.builder(Text.translatable("api.channels.invite.ignore"), w -> {
			if (invites.getSelectedOrNull() != null) {
				ChannelRequest.ignoreChannelInvite(invites.getSelectedOrNull().invite);
				clearAndInit();
			}
		}).positionAndSize(width/2 + 2, height-55/2 - 2 - 20, 73, 20).build());
		addDrawableChild(ButtonWidget.builder(CommonTexts.BACK, w -> closeScreen()).positionAndSize(width/2-75, height-55/2 + 2, 150, 20).build());

		updateButtons();
	}

	@Override
	public void closeScreen() {
		client.setScreen(parent);
	}

	private void updateButtons() {
		denyButton.active = acceptButton.active = invites.getSelectedOrNull() != null;
	}

	private class InvitesListWidget extends AlwaysSelectedEntryListWidget<InvitesListWidget.InvitesListEntry> {

		public InvitesListWidget(MinecraftClient client, int screenHeight, int y, int width, int height, int entryHeight) {
			super(client, width, screenHeight, y, y+height, entryHeight);
			ChannelRequest.getChannelInvites().thenAccept(list ->
				list.stream().map(InvitesListEntry::new).forEach(this::addEntry));
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean bl = super.mouseClicked(mouseX, mouseY, button);
			updateButtons();
			return bl;
		}

		private class InvitesListEntry extends Entry<InvitesListEntry> {

			private final ChannelInvite invite;

			public InvitesListEntry(ChannelInvite invite) {
				this.invite = invite;
			}

			@Override
			public @NotNull Text getNarration() {
				return Text.translatable("api.channels.invite.desc", invite.fromUuid(), invite.channelName());
			}

			@Override
			public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
				graphics.drawShadowedText(textRenderer, Text.translatable("api.channels.invite.name", invite.channelName()), left + 2, top + 2, -1);
				graphics.drawShadowedText(textRenderer, Text.translatable("api.channels.invite.from", UUIDHelper.getUsername(invite.fromUuid())).setStyle(Style.EMPTY.withItalic(true)), left + 15, top + height - textRenderer.fontHeight - 1, 0x808080);

			}
		}
	}
}
