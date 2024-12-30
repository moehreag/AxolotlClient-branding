package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.gui.screen.inventory.menu.PlayerInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventoryScreen.class)
public class PlayerInventoryScreenMixin {

	@WrapWithCondition(method = "checkStatusEffects", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/inventory/menu/PlayerInventoryScreen;x:I"))
	private boolean noInventoryShift(PlayerInventoryScreen instance, int value) {

		return AxolotlClient.CONFIG.inventoryPotionEffectOffset.get();
	}
}
