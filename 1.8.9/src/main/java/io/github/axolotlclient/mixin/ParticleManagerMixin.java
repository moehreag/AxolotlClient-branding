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

import java.util.Collection;
import java.util.List;

import io.github.axolotlclient.modules.particles.Particles;
import net.minecraft.client.entity.particle.Particle;
import net.minecraft.client.entity.particle.ParticleManager;
import net.minecraft.entity.particle.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

	private ParticleType cachedType;

	@Inject(method = "addParticle(IDDDDDD[I)Lnet/minecraft/client/entity/particle/Particle;", at = @At(value = "HEAD"), cancellable = true)
	public void axolotlclient$afterCreation(int i, double d, double e, double f, double g, double h, double j, int[] is,
											CallbackInfoReturnable<Particle> cir) {
		cachedType = ParticleType.byId(i);

		if (!Particles.getInstance().getShowParticle(cachedType)) {
			cir.setReturnValue(null);
			cir.cancel();
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/client/entity/particle/Particle;)V", at = @At(value = "HEAD"))
	public void axolotlclient$afterCreation(Particle particle, CallbackInfo ci) {
		if (cachedType != null) {
			Particles.getInstance().particleMap.put(particle, cachedType);
			cachedType = null;
		}
	}

	@Inject(method = "addParticle(Lnet/minecraft/client/entity/particle/Particle;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"))
	public void axolotlclient$removeParticlesWhenTooMany(Particle particle, CallbackInfo ci) {
		Particles.getInstance().particleMap.remove(particle);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
	public boolean axolotlclient$removeEmitterParticlesWhenRemoved(List<Particle> instance, Collection<Particle> objects) {
		return axolotlclient$removeParticlesWhenRemoved(instance, objects);
	}

	@Redirect(method = "tickParticles(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
	public boolean axolotlclient$removeParticlesWhenRemoved(List<Particle> instance, Collection<Particle> objects) {
		objects.forEach(particle -> Particles.getInstance().particleMap.remove(particle));

		return instance.removeAll(objects);
	}

	@Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
	public <E> E axolotlclient$applyOptions(List<E> instance, int i) {
		E particle = instance.get(i);
		if (Particles.getInstance().particleMap.containsKey(((Particle) particle))) {
			Particles.getInstance().applyOptions((Particle) particle);
		}
		return particle;
	}
}
