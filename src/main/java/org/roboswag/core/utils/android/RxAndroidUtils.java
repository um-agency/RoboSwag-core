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

package org.roboswag.core.utils.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.roboswag.core.log.Lc;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Gavriil Sitnikov on 10/01/2016.
 * Some utilities to help rx work in Android.
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
        return Observable.just(new SubscribeServiceConnection<T>())
                .switchMap(serviceConnection -> Observable.<T>create(subscriber -> {
                    serviceConnection.subscriber = subscriber;
                    context.bindService(new Intent(context, serviceClass), serviceConnection, Context.BIND_AUTO_CREATE);
                }).doOnUnsubscribe(() -> context.unbindService(serviceConnection)))
                .distinctUntilChanged()
                .replay(1)
                .refCount();
    }

    private RxAndroidUtils() {
    }

    private static class SubscribeServiceConnection<T> implements ServiceConnection {

        @Nullable
        private Subscriber<? super T> subscriber;

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
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
        public void onServiceDisconnected(final ComponentName name) {
            if (subscriber != null) {
                subscriber.onNext(null);
            }
        }

    }

}
