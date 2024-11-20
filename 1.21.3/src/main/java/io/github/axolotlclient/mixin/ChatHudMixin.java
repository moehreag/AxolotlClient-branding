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

package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {

	@WrapMethod(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
	private void onChatMessage(Component chatComponent, MessageSignature headerSignature, GuiMessageTag tag, Operation<Void> original) {
		ReceiveChatMessageEvent event = new ReceiveChatMessageEvent(false, chatComponent.getString(), chatComponent);
		Events.RECEIVE_CHAT_MESSAGE_EVENT.invoker().invoke(event);
		if (event.isCancelled()) {
			return;
		} else if (event.getNewMessage() != null) {
			chatComponent = event.getNewMessage();
		}
		original.call(chatComponent, headerSignature, tag);
	}

	/*@ModifyVariable(
		method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
		at = @At("HEAD"), index = 1, argsOnly = true)
	private Component axolotlclient$onChatMessage(Component message, @Cancellable CallbackInfo ci) {
		ReceiveChatMessageEvent event = new ReceiveChatMessageEvent(false, message.getString(), message);
		Events.RECEIVE_CHAT_MESSAGE_EVENT.invoker().invoke(event);
		if (event.isCancelled()) {
			ci.cancel();
			return null;
		} else if (event.getNewMessage() != null) {
			return event.getNewMessage();
		}
		return message;
	}*/

	/*@ModifyArg(
		method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"),
		index = 1)
	private Component axolotlclient$editChat(Component content) {
		return NickHider.getInstance().editMessage(content);
	}*/
}
