package ru.touchin.roboswag.core.observables;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import rx.Completable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;

//TODO: errors/next/completion/single/observable/block next after first fail
public class SequenceObservableExecutor {

    @NonNull
    private final Scheduler sendingScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    public Completable execute(@NonNull final Completable completable) {
        final SubscriptionHolder subscriptionHolder = new SubscriptionHolder();
        return Completable
                .create(subscriber ->
                        subscriptionHolder.subscription = sendingScheduler.createWorker().schedule(() -> {
                            final CountDownLatch blocker = new CountDownLatch(1);
                            final Subscription sendSubscription = completable
                                    .doOnTerminate(blocker::countDown)
                                    .subscribe();
                            try {
                                blocker.await();
                            } catch (final InterruptedException exception) {
                                sendSubscription.unsubscribe();
                            }
                            subscriber.onCompleted();
                        }))
                .doOnUnsubscribe(() -> {
                    if (subscriptionHolder.subscription != null && !subscriptionHolder.subscription.isUnsubscribed()) {
                        subscriptionHolder.subscription.unsubscribe();
                    }
                });
    }

    private class SubscriptionHolder {

        @Nullable
        private Subscription subscription;

    }

}
