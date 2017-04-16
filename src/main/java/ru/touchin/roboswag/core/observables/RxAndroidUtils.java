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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ServiceBinder;
import rx.Emitter;
import rx.Observable;
import rx.Scheduler;
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
                .just(new OnSubscribeServiceConnection<T>())
                .switchMap(onSubscribeServiceConnection -> Observable
                        .<T>create(emitter -> {
                            onSubscribeServiceConnection.emitter = emitter;
                            context.bindService(new Intent(context, serviceClass), onSubscribeServiceConnection, Context.BIND_AUTO_CREATE);
                        }, Emitter.BackpressureMode.LATEST)
                        .doOnUnsubscribe(() -> {
                            context.unbindService(onSubscribeServiceConnection);
                            onSubscribeServiceConnection.emitter = null;
                        }))
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    /**
     * Observes classic Android broadcast with {@link BroadcastReceiver} as source of Observable items and Intent as items.
     *
     * @param context      Context to register {@link BroadcastReceiver};
     * @param intentFilter {@link IntentFilter} to register {@link BroadcastReceiver};
     * @return Observable that observes Android broadcasts.
     */
    @NonNull
    public static Observable<Intent> observeBroadcastEvent(@NonNull final Context context, @NonNull final IntentFilter intentFilter) {
        return Observable
                .just(new OnSubscribeBroadcastReceiver())
                .switchMap(onOnSubscribeBroadcastReceiver -> Observable
                        .<Intent>create(emitter -> {
                            onOnSubscribeBroadcastReceiver.emitter = emitter;
                            context.registerReceiver(onOnSubscribeBroadcastReceiver, intentFilter);
                        }, Emitter.BackpressureMode.LATEST)
                        .doOnUnsubscribe(() -> {
                            context.unregisterReceiver(onOnSubscribeBroadcastReceiver);
                            onOnSubscribeBroadcastReceiver.emitter = null;
                        }))
                .share();
    }

    /**
     * Creating {@link Scheduler} that is scheduling work on specific thread with {@link Looper}.
     * Do not use it much times - it is creating endless thread every call.
     * It's good to use it only like a constant like:
     * private static final Scheduler SCHEDULER = RxAndroidUtils.createLooperScheduler();
     * IMPORTANT NOTE: looper thread will live forever! Do not create a lot of such Schedulers.
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

    private static class OnSubscribeServiceConnection<TService extends Service> implements ServiceConnection {
        @Nullable
        private Emitter<? super TService> emitter;

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(@NonNull final ComponentName name, @Nullable final IBinder service) {
            if (emitter == null) {
                return;
            }

            if (service instanceof ServiceBinder) {
                emitter.onNext((TService) ((ServiceBinder) service).getService());
            } else {
                Lc.assertion("IBinder should be instance of ServiceBinder.");
            }
        }

        @Override
        public void onServiceDisconnected(@NonNull final ComponentName name) {
            // service have been killed/crashed and destroyed. instead of emit null just wait service reconnection.
            // even if someone keeps reference to dead service it is problem of service object to work correctly after destroy.
        }

    }

    private static class OnSubscribeBroadcastReceiver extends BroadcastReceiver {

        @Nullable
        private Emitter<? super Intent> emitter;

        @Override
        public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            if (emitter != null) {
                emitter.onNext(intent);
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
