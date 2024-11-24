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

package io.github.axolotlclient.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class ThreadExecuter {
	private static final Executor POOL = new ForkJoinPool(5, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

	/*private static final ScheduledThreadPoolExecutor EXECUTER_SERVICE = new ScheduledThreadPoolExecutor(5,
		new ThreadFactoryBuilder().setNameFormat("ExecutionService Thread #%d").setDaemon(true).build());*/

	public static void scheduleTask(Runnable runnable) {
		//EXECUTER_SERVICE.execute(runnable);
		POOL.execute(runnable);
	}

	public static Executor service() {
		return POOL;
	}
}
