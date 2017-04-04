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

    @NonNull
    public Completable execute(@NonNull final Completable completable) {
        final Task task = new Task(completable);
        return Completable
                .create(task)
                .doOnUnsubscribe(task::cancel);
    }

    private class Task implements Completable.CompletableOnSubscribe {

        @NonNull
        private final Completable completable;
        @Nullable
        private Subscription subscription;

        public Task(@NonNull final Completable completable) {
            this.completable = completable;
        }

        @Override
        public void call(@NonNull final Completable.CompletableSubscriber subscriber) {
            subscription = sendingScheduler.createWorker().schedule(() -> {
                final CountDownLatch blocker = new CountDownLatch(1);
                final Subscription executeSubscription = completable
                        .doOnTerminate(blocker::countDown)
                        .subscribe(subscriber::onError, subscriber::onCompleted);
                try {
                    blocker.await();
                } catch (final InterruptedException exception) {
                    executeSubscription.unsubscribe();
                    subscriber.onError(exception);
                }
            });
        }

        public void cancel() {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }

    }

}
