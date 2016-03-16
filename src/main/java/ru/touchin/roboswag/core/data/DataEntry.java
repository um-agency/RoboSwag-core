package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public class DataEntry<T> {

    private final long providedTime;
    private final long expirationPeriod;
    @Nullable
    private final T data;
    @Nullable
    private final Throwable error;

    public DataEntry(final long providedTime, final long expirationPeriod, @NonNull final T data) {
        this.providedTime = providedTime;
        this.expirationPeriod = expirationPeriod;
        this.data = data;
        this.error = null;
    }

    public DataEntry(final long providedTime, final long expirationPeriod, @NonNull final Throwable error) {
        this.providedTime = providedTime;
        this.expirationPeriod = expirationPeriod;
        this.data = null;
        this.error = error;
    }

}
