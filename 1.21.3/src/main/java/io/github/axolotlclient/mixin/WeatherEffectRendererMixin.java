package io.github.axolotlclient.mixin;

import io.github.axolotlclient.AxolotlClient;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	@Inject(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/LightTexture;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"),
		cancellable = true)
	private void noRain(Level world, LightTexture light, int ticks, float tickDelta, Vec3 camera, CallbackInfo ci){
		if (AxolotlClient.CONFIG.noRain.get()) {
			ci.cancel();
		}
	}
}
