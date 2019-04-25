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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ServiceBinder;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Gavriil Sitnikov on 10/01/2016.
 * Some utilities to help work in Android with RXJava.
 */
public final class RxAndroidUtils {

    /**
     * Creates observable which is binding to service if anyone subscribe and unbind from service if no one subscribed.
     *
     * @param context      Context to create service from.
     * @param serviceClass Service class to create intent.
     * @param <T>          Type ofService class.
     * @return Returns observable to bind and wait while service will be available.
     */
    @NonNull
    public static <T extends Service> Observable<T> observeService(@NonNull final Context context, @NonNull final Class<T> serviceClass) {
        return Observable
                .just(new SubscribeServiceConnection<T>())
                .switchMap(serviceConnection -> Observable
                        .<T>create(subscriber -> {
                            serviceConnection.subscriber = subscriber;
                            context.bindService(new Intent(context, serviceClass), serviceConnection, Context.BIND_AUTO_CREATE);
                        })
                        .doOnUnsubscribe(() -> context.unbindService(serviceConnection)))
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    /**
     * Creating {@link Scheduler} that is scheduling work on specific thread with {@link Looper}.
     * Do not use it much times - it is creating endless thread every call.
     * It's good to use it only like a constant like:
     * private static final Scheduler SCHEDULER = RxAndroidUtils.createLooperScheduler();
     *
     * @return Looper thread based {@link Scheduler}.
     */
    @NonNull
    public static Scheduler createLooperScheduler() {
        final LooperThread thread = new LooperThread();
        thread.start();
        try {
            thread.isLooperInitialized.await();
            return AndroidSchedulers.from(thread.looper);
        } catch (final InterruptedException exception) {
            Lc.w(exception, "Interruption during looper creation");
            return AndroidSchedulers.mainThread();
        }
    }

    private RxAndroidUtils() {
    }

    private static class SubscribeServiceConnection<T> implements ServiceConnection {

        @Nullable
        private Subscriber<? super T> subscriber;

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(@NonNull final ComponentName name, @Nullable final IBinder service) {
            if (subscriber == null) {
                return;
            }

            if (service instanceof ServiceBinder) {
                subscriber.onNext((T) ((ServiceBinder) service).getService());
            } else {
                Lc.assertion("IBinder should be instance of ServiceBinder.");
            }
        }

        @Override
        public void onServiceDisconnected(@NonNull final ComponentName name) {
            if (subscriber != null) {
                subscriber.onNext(null);
            }
        }

    }

    private static class LooperThread extends Thread {

        @NonNull
        private final CountDownLatch isLooperInitialized = new CountDownLatch(1);
        private Looper looper;

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            looper = Looper.myLooper();
            isLooperInitialized.countDown();
            Looper.loop();
        }

    }

}
