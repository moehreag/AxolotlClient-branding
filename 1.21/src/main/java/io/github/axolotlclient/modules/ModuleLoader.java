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

package io.github.axolotlclient.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.config.screen.CreditsScreen;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

public class ModuleLoader {

	public static List<AbstractModule> loadExternalModules() {
		ArrayList<AbstractModule> modules = new ArrayList<>();
		QuiltLoader.getEntrypointContainers("axolotlclient.module", AbstractModule.class).forEach(entrypoint -> {
			try {
				AbstractModule module = entrypoint.getEntrypoint();
				if (module != null) {
					modules.add(module);
					String modName = entrypoint.getProvider().metadata().name();
					ModMetadata data = entrypoint.getProvider().metadata();
					List<String> authorsNContributors = new ArrayList<>();

					List<String> authors = data.contributors().stream()
						.filter(contributor -> contributor.roles().contains("Author")
							|| contributor.roles().contains("Owner"))
						.map(ModContributor::name).collect(Collectors.toList());

					List<String> contributors = data.contributors().stream().map(ModContributor::name)
						.filter(name -> !authors.contains(name)).toList();
					if (authors.isEmpty()) {
						data.contributors().stream().findFirst()
							.ifPresent(modContributor -> authors.add(modContributor.name()));
					}
					authorsNContributors.add("Author(s):");
					authorsNContributors.addAll(authors);
					authorsNContributors.add("");

					authorsNContributors.add("Contributor(s):");
					authorsNContributors.addAll(contributors);
					CreditsScreen.externalModuleCredits.put(modName, authorsNContributors.toArray(new String[0]));
				}
			} catch (Exception e) {
				AxolotlClient.LOGGER.warn("Skipping module: " + entrypoint.getProvider().metadata().name() + " because of error:");
				e.printStackTrace();
			}
		});
		return modules;
	}
}
