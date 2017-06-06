/**
 * Copyright 2014 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.touchin.roboswag.core.observables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Returns an observable sequence that stays connected to the source as long as
 * there is at least one subscription to the observable sequence and also it stays connected
 * for cache time after everyone unsubscribe.
 *
 * @param <T> the value type
 */
@SuppressWarnings({"PMD.AvoidUsingVolatile", "PMD.CompareObjectsWithEquals"})
//AvoidUsingVolatile,CompareObjectsWithEquals: from OnSubscribeRefCount code
public final class OnSubscribeRefCountWithCacheTime<T> implements OnSubscribe<T> {

    @NonNull
    private final ConnectableObservable<? extends T> source;
    @NonNull
    private volatile CompositeSubscription baseSubscription = new CompositeSubscription();
    @NonNull
    private final AtomicInteger subscriptionCount = new AtomicInteger(0);

    @NonNull
    private final Scheduler scheduler = Schedulers.computation();
    private final long cacheTime;
    @NonNull
    private final TimeUnit cacheTimeUnit;
    @Nullable
    private Scheduler.Worker worker;

    /**
     * Use this lock for every subscription and disconnect action.
     */
    @NonNull
    private final ReentrantLock lock = new ReentrantLock();

    public OnSubscribeRefCountWithCacheTime(@NonNull final ConnectableObservable<? extends T> source,
                                            final long cacheTime, @NonNull final TimeUnit cacheTimeUnit) {
        this.source = source;
        this.cacheTime = cacheTime;
        this.cacheTimeUnit = cacheTimeUnit;
    }

    @Override
    public void call(@NonNull final Subscriber<? super T> subscriber) {

        lock.lock();
        if (subscriptionCount.incrementAndGet() == 1) {
            if (worker != null) {
                worker.unsubscribe();
                worker = null;
            }
            final AtomicBoolean writeLocked = new AtomicBoolean(true);

            try {
                // need to use this overload of connect to ensure that
                // baseSubscription is set in the case that source is a
                // synchronous Observable
                source.connect(onSubscribe(subscriber, writeLocked));
            } finally {
                // need to cover the case where the source is subscribed to
                // outside of this class thus preventing the Action1 passed
                // to source.connect above being called
                if (writeLocked.get()) {
                    // Action1 passed to source.connect was not called
                    lock.unlock();
                }
            }
        } else {
            try {
                // ready to subscribe to source so do it
                doSubscribe(subscriber, baseSubscription);
            } finally {
                // release the read lock
                lock.unlock();
            }
        }

    }

    @NonNull
    private Action1<Subscription> onSubscribe(@NonNull final Subscriber<? super T> subscriber,
                                              @NonNull final AtomicBoolean writeLocked) {
        return subscription -> {
            try {
                baseSubscription.add(subscription);
                // ready to subscribe to source so do it
                doSubscribe(subscriber, baseSubscription);
            } finally {
                // release the write lock
                lock.unlock();
                writeLocked.set(false);
            }
        };
    }

    private void doSubscribe(@NonNull final Subscriber<? super T> subscriber, @NonNull final CompositeSubscription currentBase) {
        subscriber.add(disconnect(currentBase));
        source.unsafeSubscribe(new Subscriber<T>(subscriber) {
            @Override
            public void onError(@NonNull final Throwable throwable) {
                cleanup();
                subscriber.onError(throwable);
            }

            @Override
            public void onNext(@Nullable final T item) {
                subscriber.onNext(item);
            }

            @Override
            public void onCompleted() {
                cleanup();
                subscriber.onCompleted();
            }

            private void cleanup() {
                // on error or completion we need to unsubscribe the base subscription and set the subscriptionCount to 0
                lock.lock();
                try {
                    if (baseSubscription == currentBase) {
                        cleanupWorker();
                        // backdoor into the ConnectableObservable to cleanup and reset its state
                        if (source instanceof Subscription) {
                            ((Subscription) source).unsubscribe();
                        }
                        baseSubscription.unsubscribe();
                        baseSubscription = new CompositeSubscription();
                        subscriptionCount.set(0);
                    }
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    @NonNull
    private Subscription disconnect(@NonNull final CompositeSubscription current) {
        return Subscriptions.create(() -> {
            lock.lock();
            try {
                if (baseSubscription == current && subscriptionCount.decrementAndGet() == 0) {
                    if (worker != null) {
                        worker.unsubscribe();
                    } else {
                        worker = scheduler.createWorker();
                    }
                    worker.schedule(() -> {
                        lock.lock();
                        try {
                            if (subscriptionCount.get() == 0) {
                                cleanupWorker();
                                // backdoor into the ConnectableObservable to cleanup and reset its state
                                if (source instanceof Subscription) {
                                    ((Subscription) source).unsubscribe();
                                }
                                baseSubscription.unsubscribe();
                                // need a new baseSubscription because once
                                // unsubscribed stays that way
                                baseSubscription = new CompositeSubscription();
                            }
                        } finally {
                            lock.unlock();
                        }
                    }, cacheTime, cacheTimeUnit);
                }
            } finally {
                lock.unlock();
            }
        });
    }

    private void cleanupWorker() {
        if (worker != null) {
            worker.unsubscribe();
            worker = null;
        }
    }

}