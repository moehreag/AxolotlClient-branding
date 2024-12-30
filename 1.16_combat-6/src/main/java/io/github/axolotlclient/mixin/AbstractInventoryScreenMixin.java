package io.github.axolotlclient.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractInventoryScreen.class)
public class AbstractInventoryScreenMixin {

	@WrapWithCondition(method = "applyStatusEffectOffset", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractInventoryScreen;x:I"))
	private boolean noInventoryShift(AbstractInventoryScreen<?> instance, int value) {

		return AxolotlClient.CONFIG.inventoryPotionEffectOffset.get();
	}
}
