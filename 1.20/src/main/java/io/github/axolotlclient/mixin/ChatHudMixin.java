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

package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatMessageTag;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignature;Lnet/minecraft/client/gui/hud/ChatMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignature;ILnet/minecraft/client/gui/hud/ChatMessageTag;Z)V"), cancellable = true)
	public void axolotlclient$autoThings(Text message, MessageSignature signature, ChatMessageTag tag, CallbackInfo ci) {
		if (message == null) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignature;Lnet/minecraft/client/gui/hud/ChatMessageTag;)V", at = @At("HEAD"), argsOnly = true)
	private Text axolotlclient$onChatMessage(Text message) {
		ReceiveChatMessageEvent event = new ReceiveChatMessageEvent(false, message.getString(), message);
		Events.RECEIVE_CHAT_MESSAGE_EVENT.invoker().invoke(event);
		if (event.isCancelled()) {
			return null;
		} else if (event.getNewMessage() != null) {
			return event.getNewMessage();
		}
		return message;
	}

	@ModifyArg(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignature;Lnet/minecraft/client/gui/hud/ChatMessageTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignature;ILnet/minecraft/client/gui/hud/ChatMessageTag;Z)V"), index = 0)
	private Text axolotlclient$editChat(Text message) {
		return NickHider.getInstance().editMessage(message);
	}
}
