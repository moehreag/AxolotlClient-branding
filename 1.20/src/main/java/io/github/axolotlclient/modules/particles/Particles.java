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

package io.github.axolotlclient.modules.particles;

import java.util.*;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.*;
import io.github.axolotlclient.mixin.ParticleAccessor;
import io.github.axolotlclient.modules.AbstractModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

public class Particles extends AbstractModule {

	private static final Particles Instance = new Particles();

	public final HashMap<ParticleType<?>, HashMap<String, Option<?>>> particleOptions = new HashMap<>();
	public final HashMap<Particle, ParticleType<?>> particleMap = new HashMap<>();

	private final OptionCategory cat = new OptionCategory("particles");
	private final BooleanOption enabled = new BooleanOption("enabled", false);

	@Override
	public void init() {
		cat.add(enabled);

		addParticleOptions();
		AxolotlClient.CONFIG.rendering.addSubCategory(cat);
	}

	private void addParticleOptions() {
		for (ParticleType<?> type : Registries.PARTICLE_TYPE.stream().sorted(new AlphabeticalComparator()).toList()) {
			if (Registries.PARTICLE_TYPE.getId(type) != null) {
				OptionCategory category = new OptionCategory(
					Arrays.stream(Registries.PARTICLE_TYPE.getId(type).getPath().split("_"))
						.map(StringUtils::capitalize).collect(Collectors.joining(" ")),
					false);
				HashMap<String, Option<?>> optionsByKey = new LinkedHashMap<>();

				populateMap(optionsByKey, new BooleanOption("showParticle", true), new IntegerOption("count", 1, 1, 20),
					new BooleanOption("customColor", false), new ColorOption("color", "particles", Color.WHITE));

				if (type == ParticleTypes.CRIT || type == ParticleTypes.ENCHANTED_HIT) {
					populateMap(optionsByKey, new BooleanOption("alwaysCrit", false));
				}

				category.add(optionsByKey.values());
				particleOptions.put(type, optionsByKey);

				cat.addSubCategory(category);
			}
		}
	}

	private void populateMap(HashMap<String, Option<?>> map, Option<?>... options) {
		for (Option<?> option : options) {
			map.put(option.getName(), option);
		}
	}

	public void applyOptions(Particle particle) {
		if (enabled.get() && particleMap.containsKey(particle)) {
			ParticleType<?> type = particleMap.get(particle);
			if (particleOptions.containsKey(type)) {
				HashMap<String, Option<?>> options = particleOptions.get(type);
				if (((BooleanOption) options.get("customColor")).get()) {
					Color color = ((ColorOption) options.get("color")).get();
					particle.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
					((ParticleAccessor) particle).axolotlclient$setColorAlpha(color.getAlpha() / 255F);
				}
			}
		}
	}

	public int getMultiplier(ParticleType<?> type) {
		if (enabled.get() && particleOptions.containsKey(type)) {
			HashMap<String, Option<?>> options = particleOptions.get(type);

			return ((IntegerOption) options.get("count")).get();
		}
		return 1;
	}

	public boolean getAlwaysOn(ParticleType<?> type) {
		return enabled.get() && particleOptions.containsKey(type)
			&& ((BooleanOption) Particles.getInstance().particleOptions.get(type).get("alwaysCrit")).get();
	}

	public static Particles getInstance() {
		return Instance;
	}

	public boolean getShowParticle(ParticleType<?> type) {
		return enabled.get() && particleOptions.containsKey(type)
			? ((BooleanOption) Particles.getInstance().particleOptions.get(type).get("showParticle")).get()
			: true;
	}

	protected static class AlphabeticalComparator implements Comparator<ParticleType<?>> {

		// Function to compare
		public int compare(ParticleType<?> s1, ParticleType<?> s2) {
			if (getName(s1).equals(getName(s2)))
				return 0;
			String[] strings = {getName(s1), getName(s2)};
			Arrays.sort(strings, Collections.reverseOrder());

			if (strings[0].equals(getName(s1)))
				return 1;
			else
				return -1;
		}

		private String getName(ParticleType<?> type) {
			if (Registries.PARTICLE_TYPE.getId(type) != null) {
				return Registries.PARTICLE_TYPE.getId(type).getPath();
			}
			return "";
		}
	}
}
