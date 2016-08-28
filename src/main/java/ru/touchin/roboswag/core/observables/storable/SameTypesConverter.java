package ru.touchin.roboswag.core.observables.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Simple safe converter that is doing nothing on conversion.
 *
 * @param <T> Same type.
 */
public class SameTypesConverter<T> implements SafeConverter<T, T> {

    @Nullable
    @Override
    public T toStoreObject(@NonNull final Class<T> class1, @NonNull final Class<T> class2, @Nullable final T object) {
        return object;
    }

    @Nullable
    @Override
    public T toObject(@NonNull final Class<T> class1, @NonNull final Class<T> class2, @Nullable final T object) {
        return object;
    }

}