package ru.touchin.roboswag.core.data.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.exceptions.ConversionException;

/**
 * Created by Gavriil Sitnikov on 12/04/16.
 * TODO: description
 */
public class SameTypesConverter<T> implements Converter<T, T> {

    @Nullable
    @Override
    public T toStoreObject(@NonNull final Class<T> class1, @NonNull final Class<T> class2, @Nullable final T object)
            throws ConversionException {
        return object;
    }

    @Nullable
    @Override
    public T toObject(@NonNull final Class<T> class1, @NonNull final Class<T> class2, @Nullable final T object)
            throws ConversionException {
        return object;
    }

}
