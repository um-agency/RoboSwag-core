/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Ilia Kurtov on 25/05/2017 with a help from https://stackoverflow.com/a/21187003/4312184
 * ThreadFactory that can change default thread priority. Suitable for creating Schedulers fo RxJava like this:
 * final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor(new ProcessPriorityThreadFactory(Thread.MIN_PRIORITY)));
 */
public final class ProcessPriorityThreadFactory implements ThreadFactory {

    private final int threadPriority;

    /**
     * threadPriority can be in a range from {@link Thread#MIN_PRIORITY} to {@link Thread#MAX_PRIORITY}
     *
     * @param threadPriority priority for the Thread.
     */
    public ProcessPriorityThreadFactory(final int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    @NonNull
    public Thread newThread(@NonNull final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setPriority(threadPriority);
        return thread;
    }

}