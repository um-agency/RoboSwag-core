package ru.touchin.roboswag.core.observables;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Actions;
import rx.schedulers.Schedulers;

//TODO: errors/next/completion/single/observable/block next after first fail
public class SequenceObservableExecutor {

    @NonNull
    private final Scheduler sendingScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    @NonNull
    public Observable<?> execute(@NonNull final Observable<?> completable) {
        final Task task = new Task(completable);
        return Observable
                .create(task)
                .doOnUnsubscribe(task::cancel);
    }

    private class Task implements Observable.OnSubscribe<Subscriber> {

        @NonNull
        private final Observable<?> completable;
        @Nullable
        private Subscription subscription;

        public Task(@NonNull final Observable<?> completable) {
            this.completable = completable;
        }

        @Override
        public void call(@NonNull final Subscriber subscriber) {
            subscription = sendingScheduler.createWorker().schedule(() -> {
                final CountDownLatch blocker = new CountDownLatch(1);
                final Subscription executeSubscription = completable
                        .doOnTerminate(blocker::countDown)
                        .subscribe(Actions.empty(), subscriber::onError, subscriber::onCompleted);
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
