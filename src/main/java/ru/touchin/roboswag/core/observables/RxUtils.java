package ru.touchin.roboswag.core.observables;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 21/05/2016.
 * Some helper methods to work with JavaRx.
 */
public final class RxUtils {

    /**
     * Subscribes to specific {@link Observable} and waits for it's onCompleted event
     * and then returns {@link ObservableResult} with all collected items and errors during subscription.
     * You should NOT use such method normally. It is safer than {@link Observable#toBlocking()} but it is also like a hack.
     *
     * @param observable {@link Observable} to be executed;
     * @param <T>        Type of {@link Observable}'s items;
     * @return {@link ObservableResult} which contains all items and errors collected during execution.
     */
    @NonNull
    public static <T> ObservableResult<T> executeSync(@NonNull final Observable<T> observable) {
        final ObservableResult<T> result = new ObservableResult<>();
        final CountDownLatch waiter = new CountDownLatch(1);
        observable.subscribe(result::onNext, result::onError, waiter::countDown);
        try {
            waiter.await();
        } catch (final InterruptedException exception) {
            throw new ShouldNotHappenException(exception);
        }
        return result;
    }

    private RxUtils() {
    }

}
