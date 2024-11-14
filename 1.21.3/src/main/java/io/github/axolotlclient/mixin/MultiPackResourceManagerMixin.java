package io.github.axolotlclient.mixin;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.AxolotlClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPackResourceManager.class)
public class MultiPackResourceManagerMixin {

	@Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
	private void injectResources(ResourceLocation resourceLocation, CallbackInfoReturnable<Optional<Resource>> cir) {
		if (AxolotlClient.runtimeResources.containsKey(resourceLocation)) {
			cir.setReturnValue(Optional.of(AxolotlClient.runtimeResources.get(resourceLocation)));
		}
	}

	@Inject(method = "listResources", at = @At(value = "TAIL"))
	private void injectResourcesList(String startingPath, Predicate<ResourceLocation> pathFilter, CallbackInfoReturnable<Map<ResourceLocation, Resource>> cir, @Local Map<ResourceLocation, Resource> map) {
		AxolotlClient.runtimeResources.forEach((resourceLocation, resource) -> {
			if (resourceLocation.getPath().startsWith(startingPath) && pathFilter.test(resourceLocation)) {
				map.put(resourceLocation, resource);
			}
		});
	}
}
