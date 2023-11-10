package io.github.axolotlclient.mixin;

import io.github.axolotlclient.util.keybinds.KeyBinds;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBind;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

	@Mutable
	@Shadow
	@Final
	public KeyBind[] allKeys;

	@Inject(method = "load", at = @At("HEAD"))
	private void axolotlclient$onLoad(CallbackInfo ci){
		this.allKeys = KeyBinds.getInstance().process(this.allKeys);
	}
}
