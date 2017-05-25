package ru.touchin.roboswag.core.observables;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import ru.touchin.roboswag.core.utils.ProcessPriorityThreadFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Actions;
import rx.schedulers.Schedulers;

//TODO: errors/next/completion/single/observable/block next after first fail
public class SequenceObservableExecutor {

    private static void safeUnsubscribe(@Nullable final Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @NonNull
    private final Scheduler sendingScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    @NonNull
    private final Scheduler executeScheduler = Schedulers.from(Executors.newSingleThreadExecutor(
            new ProcessPriorityThreadFactory(Thread.MIN_PRIORITY)));

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
        private Subscription scheduleSubscription;
        @Nullable
        private Subscription executeSubscription;

        public Task(@NonNull final Observable<?> completable) {
            this.completable = completable;
        }

        @Override
        public void call(@NonNull final Subscriber subscriber) {
            scheduleSubscription = sendingScheduler.createWorker().schedule(() -> {
                final CountDownLatch blocker = new CountDownLatch(1);
                executeSubscription = completable
                        .subscribeOn(executeScheduler)
                        .doOnUnsubscribe(blocker::countDown)
                        .subscribe(Actions.empty(), subscriber::onError, subscriber::onCompleted);
                try {
                    blocker.await();
                } catch (final InterruptedException exception) {
                    safeUnsubscribe(executeSubscription);
                    subscriber.onError(exception);
                }
            });
        }

        public void cancel() {
            safeUnsubscribe(scheduleSubscription);
            safeUnsubscribe(executeSubscription);
        }

    }

}
