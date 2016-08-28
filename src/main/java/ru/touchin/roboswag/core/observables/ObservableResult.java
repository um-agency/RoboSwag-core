package ru.touchin.roboswag.core.observables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 21/05/2016.
 * Object represents Observable's execution result. Contains all items and errors emitted by Observable during subscription.
 */
public class ObservableResult<T> {

    @NonNull
    private final List<T> items = new LinkedList<>();
    @NonNull
    private final List<Throwable> errors = new LinkedList<>();

    /**
     * Passes item to collect.
     *
     * @param item Emitted item.
     */
    public void onNext(@Nullable final T item) {
        items.add(item);
    }

    /**
     * Passes error to collect.
     *
     * @param error Emitted error.
     */
    public void onError(@NonNull final Throwable error) {
        errors.add(error);
    }

    /**
     * Returns list of collected items.
     *
     * @return Items.
     */
    @NonNull
    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Returns list of collected errors.
     *
     * @return Errors.
     */
    @NonNull
    public List<Throwable> getErrors() {
        return new ArrayList<>(errors);
    }

}
