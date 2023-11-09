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

package io.github.axolotlclient.api.util;

import lombok.Data;

@Data
public class BiContainer<A, B> {

	private A left;
	private B right;

	private BiContainer(A left, B right){
		this.left = left;
		this.right = right;
	}

	public void set(A left, B right) {
		this.left = left;
		this.right = right;
	}

	public static <A, B> BiContainer<A, B> empty(){
		return of(null, null);
	}

	public static <A, B> BiContainer<A, B> of(A left, B right){
		return new BiContainer<>(left, right);
	}
}
