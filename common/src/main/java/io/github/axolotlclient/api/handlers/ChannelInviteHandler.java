package io.github.axolotlclient.api.handlers;

import io.github.axolotlclient.api.API;
import io.github.axolotlclient.api.Response;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.types.ChannelInvite;
import io.github.axolotlclient.api.util.SocketMessageHandler;
import io.github.axolotlclient.api.util.UUIDHelper;

public class ChannelInviteHandler implements SocketMessageHandler {
	@Override
	public boolean isApplicable(String target) {
		return "channel_invite".equals(target) || "channel_invite_reaction".equals(target);
	}

	@Override
	public void handle(Response response) {
		String id = response.getBody("channel", l -> Long.toUnsignedString((long) l));
		String target = response.getBody("target");
		String channelName = response.getBody("channel_name");
		switch (target) {
			case "channel_invite" -> {
				String from = response.getBody("from");
				if (API.getInstance().getApiOptions().channelInvitesEnabled.get()) {
					notification("api.channels.invite", "api.channels.invite.desc", UUIDHelper.getUsername(from), channelName);
				} else {
					ChannelRequest.ignoreChannelInvite(new ChannelInvite(id, channelName, from));
				}
			}
			case "channel_invite_reaction" -> {
				String player = response.getBody("player");
				boolean accepted = response.getBody("accepted");
				if (accepted) {
					notification("api.channels.invite.accepted", "api.channels.invite.accepted.desc", UUIDHelper.getUsername(player), channelName);
				} else {
					notification("api.channels.invite.denied", "api.channels.invite.denied.desc", UUIDHelper.getUsername(player), channelName);
				}
			}
		}
	}
}
