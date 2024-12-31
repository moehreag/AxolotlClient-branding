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

import java.util.List;
import java.util.Queue;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {

	@Unique private ParticleType<?> cachedType;

	@Inject(
		method = "makeParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At(value = "HEAD"), cancellable = true)
	private void axolotlclient$afterCreation(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
		cachedType = parameters.getType();

		if (!Particles.getInstance().getShowParticle(cachedType)) {
			cir.setReturnValue(null);
			cir.cancel();
		}
	}

	@Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V", at = @At(value = "HEAD"))
	private void axolotlclient$afterCreation(Particle particle, CallbackInfo ci) {
		if (cachedType != null) {
			Particles.getInstance().particleMap.put(particle, cachedType);
			cachedType = null;
		}
	}

	@Inject(method = "tickParticleList", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/particle/ParticleEngine;tickParticle(Lnet/minecraft/client/particle/Particle;)V"))
	private void axolotlclient$removeParticlesWhenRemoved(CallbackInfo ci, @Local Particle particle) {
		if (!particle.isAlive()) {
			Particles.getInstance().particleMap.remove(particle);
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Queue;removeAll(Ljava/util/Collection;)Z"))
	private void axolotlclient$removeEmitterParticlesWhenRemoved(CallbackInfo ci, @Local List<Particle> collection) {
		collection.forEach(particle -> Particles.getInstance().particleMap.remove(particle));
	}

	@Inject(method = "renderParticleType", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"))
	private static void axolotlclient$applyOptions(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleRenderType, Queue<Particle> queue, CallbackInfo ci, @Local Particle particle) {
		if (Particles.getInstance().particleMap.containsKey(particle)) {
			Particles.getInstance().applyOptions(particle);
		}
	}
}
