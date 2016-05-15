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

package ru.touchin.roboswag.core.observables;

import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

import ru.touchin.roboswag.core.log.Lc;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Gavriil Sitnikov on 10/01/2016.
 * Some utilities to help work in Android with RXJava.
 */
public final class RxAndroidUtils {

    /**
     * Creating {@link Scheduler} that is scheduling work on specific thread with {@link Looper}.
     * @return Looper thread based {@link Scheduler}.
     */
    @NonNull
    public static Scheduler createLooperScheduler() {
        final LooperThread thread = new LooperThread();
        thread.start();
        try {
            thread.isHandlerInitialized.await();
            final Looper looper = Looper.myLooper();
            if (looper == null) {
                Lc.assertion("Looper of thread is null");
                return AndroidSchedulers.mainThread();
            }
            return AndroidSchedulers.from(looper);
        } catch (final InterruptedException e) {
            return AndroidSchedulers.mainThread();
        }
    }

    private RxAndroidUtils() {
    }

    private static class LooperThread extends Thread {

        private final CountDownLatch isHandlerInitialized = new CountDownLatch(1);

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            isHandlerInitialized.countDown();
            Looper.loop();
        }

    }

}
