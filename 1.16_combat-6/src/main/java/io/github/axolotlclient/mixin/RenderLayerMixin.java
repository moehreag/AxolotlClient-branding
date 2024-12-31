/*
 * Copyright © 2023 moehreag <moehreag@gmail.com> & Contributors
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

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {

    /*@Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/OptionalDouble;empty()Ljava/util/OptionalDouble;"))
    private static OptionalDouble axolotlclient$customOutlineWidth(){
        // The game needs to be restarted for changes to have effects.
        // Yes, it would need very large quirks to change that, and no, I won't do those.
        return OptionalDouble.of(AxolotlClient.CONFIG.outlineWidth.get());
    }*/
}
